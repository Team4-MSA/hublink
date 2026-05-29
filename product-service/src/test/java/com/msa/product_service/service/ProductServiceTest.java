package com.msa.product_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @InjectMocks
    private ProductService productService;

    @Mock
    private ProductRepository productRepository;

    private ProductRequestDto requestDto;
    private Product mockProduct;
    private UUID productId;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();

        requestDto = ProductRequestDto.builder()
            .companyId(UUID.randomUUID())
            .hubId(UUID.randomUUID())
            .name("테스트 상품")
            .description("설명")
            .price(1000)
            .build();

        mockProduct = Product.create(requestDto.getCompanyId(), requestDto.getHubId(), requestDto.getName(), requestDto.getPrice(), requestDto.getDescription());
    }

    @Nested
    @DisplayName("상품 생성 테스트")
    class CreateProductTest {

        @Test
        @DisplayName("성공: 상품 생성 후 저장된 객체 반환")
        void createProduct_Success() {
            when(productRepository.save(any(Product.class))).thenReturn(mockProduct);

            Product result = productService.createProduct(requestDto);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("테스트 상품");
            verify(productRepository, times(1)).save(any(Product.class));
        }
    }

    @Nested
    @DisplayName("상품 단건 조회 테스트")
    class GetProductTest {

        @Test
        @DisplayName("성공: 존재하는 상품 ID로 조회")
        void getProduct_Success() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

            Product result = productService.getProduct(productId);

            assertThat(result).isNotNull();
            verify(productRepository, times(1)).findById(productId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 ID로 조회 시 예외 발생")
        void getProduct_Fail_NotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(CustomException.class, () -> productService.getProduct(productId));
        }
    }

    @Nested
    @DisplayName("상품 수정 테스트")
    class ModifyProductTest {

        @Test
        @DisplayName("성공: 상품 정보 수정")
        void modifyProduct_Success() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

            Product result = productService.modifyProduct(requestDto, productId);

            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("테스트 상품");
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 수정 시 예외 발생")
        void modifyProduct_Fail_NotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(CustomException.class,
                () -> productService.modifyProduct(requestDto, productId));
        }
    }

    @Nested
    @DisplayName("상품 삭제 테스트")
    class DeleteProductTest {

        @Test
        @DisplayName("성공: 상품 소프트 딜리트")
        void deleteProduct_Success() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(mockProduct));

            Product result = productService.deleteProduct(productId, UUID.randomUUID());

            assertThat(result).isNotNull();
            verify(productRepository, times(1)).findById(productId);
        }

        @Test
        @DisplayName("실패: 존재하지 않는 상품 삭제 시 예외 발생")
        void deleteProduct_Fail_NotFound() {
            when(productRepository.findById(productId)).thenReturn(Optional.empty());

            assertThrows(CustomException.class,
                () -> productService.deleteProduct(productId, UUID.randomUUID()));
        }
    }
}