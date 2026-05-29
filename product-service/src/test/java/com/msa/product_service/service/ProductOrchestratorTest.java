package com.msa.product_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.GlobalResponse;
import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.client.StockClient;
import com.msa.product_service.client.UserClient;
import com.msa.product_service.client.UserResponseDto;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductOrchestratorTest {

    @InjectMocks
    private ProductOrchestrator productOrchestrator;

    @Mock
    private ProductService productService;
    @Mock
    private StockClient stockClient;
    @Mock
    private CompanyClient companyClient;
    @Mock
    private UserClient userClient;

    private UUID productId;
    private UUID userId;
    private UUID hubId;
    private UUID companyId;
    private Product mockProduct;
    private ProductRequestDto requestDto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        userId    = UUID.randomUUID();
        hubId     = UUID.randomUUID();
        companyId = UUID.randomUUID();

        requestDto = ProductRequestDto.builder()
            .hubId(hubId)
            .companyId(companyId)
            .name("테스트 상품")
            .price(1000)
            .quantity(10)
            .build();

        mockProduct = Product.builder()
            .companyId(companyId)
            .hubId(hubId)
            .name("테스트 상품")
            .price(1000)
            .build();
    }

    // ───────────────────────────────────────────
    // 상품 상세 조회
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("getProduct - 상품 상세 조회")
    class GetProductTest {

        @Test
        @DisplayName("성공: MASTER 권한은 바로 조회 가능")
        void getProduct_Master_Success() {
            when(productService.getProduct(productId)).thenReturn(mockProduct);

            ProductResponseDto result = productOrchestrator.getProduct(productId, userId, "MASTER");

            assertThat(result).isNotNull();
            verify(userClient, never()).getUser(any()); // user-service 호출 안 함
        }

        @Test
        @DisplayName("성공: HUB_MANAGER 권한이고 본인 허브 상품 조회")
        void getProduct_HubManager_SameHub_Success() {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setHubId(hubId); // 상품의 hubId와 동일

            when(productService.getProduct(productId)).thenReturn(mockProduct);
            when(userClient.getUser(userId)).thenReturn(userDto);

            ProductResponseDto result = productOrchestrator.getProduct(productId, userId, "HUB_MANAGER");

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패: HUB_MANAGER 권한이지만 다른 허브 상품 조회 시 예외")
        void getProduct_HubManager_DifferentHub_Fail() {
            UserResponseDto userDto = new UserResponseDto();
            userDto.setHubId(UUID.randomUUID()); // 다른 hubId

            when(productService.getProduct(productId)).thenReturn(mockProduct);
            when(userClient.getUser(userId)).thenReturn(userDto);

            assertThrows(CustomException.class,
                () -> productOrchestrator.getProduct(productId, userId, "HUB_MANAGER"));
        }
    }

    // ───────────────────────────────────────────
    // 상품 생성
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("createProductFlow - 상품 생성")
    class CreateProductFlowTest {

        @Test
        @DisplayName("성공: MASTER 권한으로 상품 생성 및 재고 생성")
        void createProduct_Master_Success() {
            CompanyResponseDto companyDto = new CompanyResponseDto();
            companyDto.setHubId(hubId); // 상품 hubId와 동일

            when(companyClient.getCompany(companyId)).thenReturn(companyDto);
            when(productService.createProduct(requestDto)).thenReturn(mockProduct);
            doNothing().when(stockClient).createStock(any());

            Product result = productOrchestrator.createProductFlow("MASTER", requestDto, userId);

            assertThat(result).isNotNull();
            verify(stockClient, times(1)).createStock(any());
        }

        @Test
        @DisplayName("실패: hubId와 companyId 둘 다 null이면 예외")
        void createProduct_NullHubAndCompany_Fail() {
            ProductRequestDto noIdDto = ProductRequestDto.builder()
                .name("상품")
                .price(1000)
                .build(); // hubId, companyId 없음

            assertThrows(CustomException.class,
                () -> productOrchestrator.createProductFlow("MASTER", noIdDto, userId));
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자(DELIVERY_MANAGER)가 생성 시 예외")
        void createProduct_NoPermission_Fail() {
            assertThrows(CustomException.class,
                () -> productOrchestrator.createProductFlow("DELIVERY_MANAGER", requestDto, userId));
        }

        @Test
        @DisplayName("실패: 업체가 해당 허브 소속이 아닐 때 예외")
        void createProduct_CompanyNotInHub_Fail() {
            CompanyResponseDto companyDto = new CompanyResponseDto();
            companyDto.setHubId(UUID.randomUUID()); // 다른 허브 소속

            when(companyClient.getCompany(companyId)).thenReturn(companyDto);

            assertThrows(CustomException.class,
                () -> productOrchestrator.createProductFlow("MASTER", requestDto, userId));
        }

        @Test
        @DisplayName("실패: stock-service 통신 실패 시 상품 롤백 후 예외")
        void createProduct_StockServiceFail_Rollback() {
            CompanyResponseDto companyDto = new CompanyResponseDto();
            companyDto.setHubId(hubId);

            when(companyClient.getCompany(companyId)).thenReturn(companyDto);
            when(productService.createProduct(requestDto)).thenReturn(mockProduct);
            doThrow(new RuntimeException("stock-service 연결 실패"))
                .when(stockClient).createStock(any());

            assertThrows(CustomException.class,
                () -> productOrchestrator.createProductFlow("MASTER", requestDto, userId));

            // 상품 롤백(삭제) 호출 여부 검증
            verify(productService, times(1)).deleteProduct(any(), eq(userId));
        }
    }

    // ───────────────────────────────────────────
    // 상품 삭제
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("deleteProduct - 상품 삭제")
    class DeleteProductTest {

        @Test
        @DisplayName("성공: MASTER 권한으로 삭제")
        void deleteProduct_Master_Success() {
            when(productService.getProduct(productId)).thenReturn(mockProduct);
            when(productService.deleteProduct(productId, userId)).thenReturn(mockProduct);

            Product result = productOrchestrator.deleteProduct(productId, "MASTER", userId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("성공: HUB_MANAGER 권한이고 본인 허브 상품 삭제")
        void deleteProduct_HubManager_Success() {
            GlobalResponse<Map<String, Boolean>> response =
                GlobalResponse.success(200, Map.of("verified", true));

            when(productService.getProduct(productId)).thenReturn(mockProduct);
            when(userClient.isHubManager(userId, hubId)).thenReturn(response);
            when(productService.deleteProduct(productId, userId)).thenReturn(mockProduct);

            Product result = productOrchestrator.deleteProduct(productId, "HUB_MANAGER", userId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자(COMPANY_MANAGER)가 삭제 시 예외")
        void deleteProduct_NoPermission_Fail() {
            when(productService.getProduct(productId)).thenReturn(mockProduct);

            assertThrows(CustomException.class,
                () -> productOrchestrator.deleteProduct(productId, "COMPANY_MANAGER", userId));
        }

        @Test
        @DisplayName("실패: HUB_MANAGER 권한이지만 다른 허브 상품 삭제 시 예외")
        void deleteProduct_HubManager_DifferentHub_Fail() {
            GlobalResponse<Map<String, Boolean>> response =
                GlobalResponse.success(200, Map.of("verified", false));

            when(productService.getProduct(productId)).thenReturn(mockProduct);
            when(userClient.isHubManager(userId, hubId)).thenReturn(response);

            assertThrows(CustomException.class,
                () -> productOrchestrator.deleteProduct(productId, "HUB_MANAGER", userId));
        }
    }

    // ───────────────────────────────────────────
    // 상품 수정
    // ───────────────────────────────────────────
    @Nested
    @DisplayName("modifyProduct - 상품 수정")
    class ModifyProductTest {

        @Test
        @DisplayName("성공: MASTER 권한으로 수정")
        void modifyProduct_Master_Success() {
            when(productService.modifyProduct(requestDto, productId)).thenReturn(mockProduct);

            Product result = productOrchestrator.modifyProduct(requestDto, productId, "MASTER", userId);

            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("실패: 권한 없는 사용자가 수정 시 예외")
        void modifyProduct_NoPermission_Fail() {
            assertThrows(CustomException.class,
                () -> productOrchestrator.modifyProduct(requestDto, productId, "DELIVERY_MANAGER", userId));
        }
    }
}