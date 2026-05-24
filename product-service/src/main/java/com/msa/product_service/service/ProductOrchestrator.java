package com.msa.product_service.service;

import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.client.StockClient;
import com.msa.product_service.client.StockRequestDto;
import com.msa.product_service.client.UserClient;
import com.msa.product_service.dto.ProductModifyDto;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.entity.Product;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
     * 상품 수정. 수정 전 권한별 접근 확인
     *
     * @param dto
     * @param userRole
     * @param userId
     * @param username
     * @return
     */
    public Product modifyProduct(ProductRequestDto dto,UUID id, String userRole, UUID userId,String username) {
        //권한 검사
        checkPermission(userRole,userId,username,dto.getHubId(),dto.getCompanyId());
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
    public Product createProductFlow(String userRole, ProductRequestDto dto, UUID userId, String username) {
        //권한 검사
        checkPermission(userRole,userId,username,dto.getHubId(),dto.getCompanyId());

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
            throw new RuntimeException("재고 시스템 연동 실패로 상품 등록 취소처리.");
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
            throw new IllegalArgumentException("해당 업체는 지정된 허브에 속해있지 않음.");
        }
    }

    /**
     * 권한 검사 하는 메서드 - 상품 생성 및 수정 할 때 사용
     * @param userRole
     * @param userId
     * @param username
     * @param hubId     : 상품의 hubId
     * @param companyId : 상품의 companyId
     */
    private void checkPermission(String userRole, UUID userId, String username, UUID hubId, UUID companyId) {
        //이 3가지 권한이 아닐 경우, 모두 접근 제한
        if (!userRole.equals("MASTER") && !userRole.equals("HUB_MANAGER") && !userRole.equals("COMPANY_MANAGER")) {
            throw new IllegalArgumentException("접근 권한이 없습니다.");
        }
        //허브 관리자의 경우 본인 허브인지 확인.
        if (userRole.equals("HUB_MANAGER")) {
            Map<String, Boolean> ishubManagerStr = userClient.isHubManager(userId, hubId)
                .getData();
            Boolean ishubManager = ishubManagerStr.get("verified");
            if (ishubManager == null || !ishubManager) {
                throw new IllegalArgumentException("해당 허브에 대한 관리 권한이 없음.");
            }
        }
        //업체 관리자의 경우, 본인 업체인지 확인
        if (userRole.equals("COMPANY_MANAGER")) {
            Map<String, Boolean> isCompanyManagerStr = userClient.isCompanyManager(userId, companyId).getData();
            Boolean isCompanyManager = isCompanyManagerStr.get("verified");
            if (isCompanyManager == null || !isCompanyManager) {
                throw new IllegalArgumentException("해당 업체에 대한 권한이 업음.");
            }
        }

    }


}
