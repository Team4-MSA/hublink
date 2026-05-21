package com.msa.product_service.service;

import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.client.StockClient;
import com.msa.product_service.client.StockRequestDto;
import com.msa.product_service.client.UserClient;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.entity.Product;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductOrchestrator {

    private ProductService productService;
    private StockClient stockClient;
    private CompanyClient companyClient;
    private UserClient  userClient;

    /**
     * 상품 생성 로직 상품 생성 -> 재고 수량 증가 -> 재고 이력 남기기
     *
     * @param userRole
     * @param dto
     * @param userId
     */
    public Product createProductFlow(String userRole, ProductRequestDto dto, UUID userId,
        String username) {
        //Spring Security 기능이 구현되면 변경할 예정
        if(!userRole.equals("MASTER") && !userRole.equals("HUB_MANAGER") && !userRole.equals("COMPANY_MANAGER")) {
            throw new  IllegalArgumentException("접근 권한이 없습니다.");
        }

        //HUB_MANAGER는 본인 허브인지 확인.
        if(userRole.equals("HUB_MANAGER")){
            userClient.IsHubManager(userId,dto.getHubId());
        }

        //COMPANY_MANAGER는 본인 업체인지 확인.
        if(userRole.equals("COMPANY_MANAGER")) {
            userClient.IsCompanyManager(userId,dto.getCompanyId());
        }

        //업체가 허브에 속해 있는지 확인.
        isCompanyInHub(dto.getCompanyId(), dto.getHubId());

        //전달 받은 상품 데이터를 DB에 저장.
        Product newProduct = productService.createProduct(dto);

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


}
