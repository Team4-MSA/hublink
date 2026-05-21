package com.msa.product_service.controller;

import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.service.ProductOrchestrator;
import com.msa.product_service.service.ProductService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductOrchestrator productOrchestrator;

    /**
     * 상품 생성
     * @param userRole
     * @param userId
     * @param username
     * @param dto
     * @return
     */
    @PostMapping
    public ProductResponseDto createProduct(@RequestHeader("X-User-Role")String userRole,
                                                                 @RequestHeader("X-User-Id") UUID userId,
                                                                 @RequestHeader("X-User-username") String username,
                                                                 @RequestBody ProductRequestDto dto){
        Product newProduct = productOrchestrator.createProductFlow(userRole,dto,userId,username);
        return ProductResponseDto.from(newProduct);
    }
}
