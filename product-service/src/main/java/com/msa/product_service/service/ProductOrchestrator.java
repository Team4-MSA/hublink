package com.msa.product_service.service;

import com.msa.core_common.auth.UserRole;
import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.client.StockClient;
import com.msa.product_service.client.StockRequestDto;
import com.msa.product_service.client.UserClient;
import com.msa.product_service.client.UserResponseDto;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.global.ProductErrorCode;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOrchestrator {

    private final ProductService productService;
    private final StockClient stockClient;
    private final CompanyClient companyClient;
    private final UserClient userClient;

    /**
     * 상품 상세 조회
     * @param productId
     * @param userId
     * @param userRole
     * @return
     */
    public ProductResponseDto getProduct(UUID productId, UUID userId, String userRole){
        //먼저 Product를 조회한다.
        Product getProduct = productService.getProduct(productId);
        //사용자의 권한이 허브 관리자인지 확인한다.
        if(UserRole.HUB_MANAGER.name().equals(userRole)){
            //외부 서비스를 호출하여 User 정보를 조회
            UserResponseDto userDto = userClient.getUser(userId);
            //그 유저의 hubId와 조회된 상품의 hubId가 일치하는지 비교
            if(userDto.getHubId() == null || !userDto.getHubId().equals(getProduct.getHubId())){
                //일치하지 않는다면, 이 유저가 관리할 수 있는 상품이 아니기 때문에, 접근 제한.
                throw new CustomException(ProductErrorCode.ACCESS_DENIED);
            }
        }
        //그 외의 사용자의 경우 조회된 상품을 ProductResponseDto로 변환하여 반환
        return ProductResponseDto.from(getProduct);
    }

    /**
     * 상품 조회 모든 권한 접근 가능 허브 관리자의 경우, 본인 허브만 접근 가능
     *
     * @param page
     * @param dto
     * @return
     */
    public PageRes<ProductResponseDto> getProducts(Pageable page, ProductSearchDto dto, UUID userId,
        String userRole) {
        //사용자가 허브 관리자라면
        if (UserRole.HUB_MANAGER.name().equals(userRole)) {
            //외부 호출로 사용자의 정보를 가져오고
            UserResponseDto userDto = userClient.getUser(userId);
            //그 사용자의 hubId를 검색 조건에 할당한다.
            dto.setHubId(userDto.getHubId());
        }
        return productService.getProducts(page,dto);
    }

    /**
     * 상품 삭제
     *
     * @param id
     * @param username
     * @param userRole
     * @return
     */
    public Product deleteProduct(UUID id, String username, String userRole, UUID userId) {
        Product product = productService.getProduct(id);

        //권한 검사 먼저 진행
        if (!UserRole.HUB_MANAGER.name().equals(userRole) && !UserRole.MASTER.name().equals(userRole)) {
            throw new CustomException(ProductErrorCode.ACCESS_DENIED);
        }
        // 허브 관리자의 경우 본인 허브에 대해서만 삭제 권한이 존재함.
        if (UserRole.HUB_MANAGER.name().equals(userRole)) {
            //응답 body를 Map으로 변환
            Map<String, Boolean> ishubManagerStr = userClient.isHubManager(userId,
                product.getHubId()).getData();
            // 전달 받은 응답 body null 검사
            if (ishubManagerStr == null) {
                //null인 경우 접근 제한
                throw new CustomException(ProductErrorCode.HUB_ACCESS_DENIED);
            }
            //응답 body에서 우리가 원하는 데이터 추출.
            Boolean ishubManager = ishubManagerStr.get("verified");
            //추출한 데이터에 대한 null 검사 및 false인 경우
            if (ishubManager == null || !ishubManager) {
                // 접근 제한
                throw new CustomException(ProductErrorCode.HUB_ACCESS_DENIED);
            }
        }
        //상품을 삭제
        Product deleteProdcut = productService.deleteProduct(id, username);
        return deleteProdcut;
    }

    /**
     * 상품 수정. 수정 전 권한별 접근 확인
     *
     * @param dto
     * @param userRole
     * @param userId
     * @param username
     * @return
     */
    public Product modifyProduct(ProductRequestDto dto, UUID id, String userRole, UUID userId,
        String username) {
        //권한 검사
        checkPermission(userRole, userId, username, dto.getHubId(), dto.getCompanyId());
        //상품 수정
        return productService.modifyProduct(dto,id);
    }

    /**
     * 상품 생성 로직 상품 생성 -> 재고 수량 증가 -> 재고 이력 남기기
     *
     * @param userRole
     * @param dto
     * @param userId
     */
    public Product createProductFlow(String userRole, ProductRequestDto dto, UUID userId,
        String username) {
        //권한 검사
        checkPermission(userRole, userId, username, dto.getHubId(), dto.getCompanyId());

        //업체가 허브에 속해 있는지 확인.
        isCompanyInHub(dto.getCompanyId(), dto.getHubId());

        //전달 받은 상품 데이터를 DB에 저장.
        Product newProduct = productService.createProduct(dto);

        //재고 서비스와 통신하다가 끊길 경우, 미리 저장한 Product를 지워야 하므로 try-catch로 처리
        try {
            StockRequestDto stockRequestDto = StockRequestDto.builder().
                productId(newProduct.getId()).
                hubId(newProduct.getHubId()).
                quantity(dto.getQuantity()).
                build();

            //이 상품에 대한 재고와 재고 이력을 DB에 저장.
            stockClient.createStock(stockRequestDto);
        } catch (Exception e) {
            //재고 서비스에 문제가 발생 시, DB에 저장된 상품을 삭제.
            productService.deleteProduct(newProduct.getId(), username);
            throw new CustomException(ProductErrorCode.STOCK_SERVICE_FAILED);
        }
        return newProduct;
    }

    /**
     * 특정 업체가 특정 허브에 속해 있는지 확인.
     *
     * @param companyId
     * @param hubId
     */
    private void isCompanyInHub(UUID companyId, UUID hubId) {
        CompanyResponseDto company = companyClient.getCompany(companyId);
        if (!company.getHubId().equals(hubId)) {
            throw new CustomException(ProductErrorCode.COMPANY_NOT_IN_HUB);
        }
    }

    /**
     * 권한 검사 하는 메서드 - 상품 생성 및 수정 할 때 사용
     *
     * @param userRole
     * @param userId
     * @param username
     * @param hubId     : 상품의 hubId
     * @param companyId : 상품의 companyId
     */
    private void checkPermission(String userRole, UUID userId, String username, UUID hubId,
        UUID companyId) {
        //이 3가지 권한이 아닐 경우, 모두 접근 제한
        if (!UserRole.MASTER.name().equals(userRole) && !UserRole.HUB_MANAGER.name().equals(userRole) && !UserRole.COMPANY_MANAGER.name().equals(userRole)) {
            throw new CustomException(ProductErrorCode.ACCESS_DENIED);
        }
        //허브 관리자의 경우 본인 허브인지 확인.
        if (UserRole.HUB_MANAGER.name().equals(userRole)) {
            //전달 받은 데이터를 Map으로 변환
            Map<String, Boolean> ishubManagerStr = userClient.isHubManager(userId, hubId).getData();
            //전달 받은 데이터가 없으면
            if (ishubManagerStr == null) {
                //접근 제한
                throw new CustomException(ProductErrorCode.HUB_ACCESS_DENIED);
            }
            //전달 받은 데이터가 잘 들어있으면, 추출
            Boolean ishubManager = ishubManagerStr.get("verified");
            //추출했는데 없거나, fasle이면
            if (ishubManager == null || !ishubManager) {
                //접근 제한
                throw new CustomException(ProductErrorCode.HUB_ACCESS_DENIED);
            }
        }
        //업체 관리자의 경우, 본인 업체인지 확인
        if (UserRole.COMPANY_MANAGER.name().equals(userRole)) {
            //전달 받은 데이터 Map으로 변환
            Map<String, Boolean> isCompanyManagerStr = userClient.isCompanyManager(userId,
                companyId).getData();
            //전달 받은 데이터가 비어 있으면
            if (isCompanyManagerStr == null) {
                //접근 제한
                throw new CustomException(ProductErrorCode.COMPANY_ACCESS_DENIED);
            }
            //전달 받은 데이터가 잘 들어 있으면 우리가 원하는 데이터 추출.
            Boolean isCompanyManager = isCompanyManagerStr.get("verified");
            //그 데이터가 없거나, false인 경우
            if (isCompanyManager == null || !isCompanyManager) {
                // 접근 제한
                throw new CustomException(ProductErrorCode.COMPANY_ACCESS_DENIED);
            }
        }

    }
}
