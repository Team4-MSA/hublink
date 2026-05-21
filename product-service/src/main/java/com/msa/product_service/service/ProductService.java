package com.msa.product_service.service;

import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.repository.ProductRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    /**
     * 상품 생성
     *
     * @param dto
     * @return
     */
    @Transactional
    public Product createProduct(ProductRequestDto dto) {
        //전달 받은 값으로 Product 객체를 생성 한 후
        Product newProduct = Product.create(dto);
        //DB에 저장.
        productRepository.save(newProduct);
        return newProduct;
    }

    /**
     * 상품 삭제
     *
     * @param productId
     * @param username
     */
    @Transactional
    public void deleteProduct(UUID productId, String username) {
        Product deletedProduct = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 없음"));
        deletedProduct.delete(username);
    }

}
