package com.msa.product_service.controller;

import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.service.ProductOrchestrator;
import com.msa.product_service.service.ProductService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductOrchestrator productOrchestrator;
    private final ProductService productService;

    /**
     * 상품 목록 조회
     * @param pageable
     * @param dto
     * @return
     */
    public PageRes<ProductResponseDto> getProducts(@PageableDefault
                                                  (page = 0, size = 10, sort = "createdAt",
                                                  direction = Direction.DESC) Pageable pageable,
                                                  ProductSearchDto dto,
                                                @RequestHeader("X-User-Id") UUID userId,
                                                @RequestHeader("X-User-Role") String userRole) {
       return  productOrchestrator.getProducts(pageable, dto, userId, userRole);
    }


    /**
     * 상품 삭제
     * @param id
     * @param userRole
     * @param username
     * @param userId
     * @return
     */
    @PatchMapping("/{productId}")
    public UUID deleteProduct(@PathVariable("productId") UUID id,
                              @RequestHeader("X-User-Role") String userRole,
                              @RequestHeader("X-User-username") String username,
                              @RequestHeader("X-User-Id")UUID userId) {
        Product deleteProduct = productOrchestrator.deleteProduct(id,username, userRole,userId);
        return  deleteProduct.getId();
    }

    /**
     * 상품 수정하기
     * @param dto
     * @return
     */
    @PutMapping("/{productId}")
    public UUID modifyProduct(@Valid @RequestBody ProductRequestDto dto,
                              @PathVariable("productId") UUID id,
                              @RequestHeader("X-User-Role")String userRole,
                              @RequestHeader("X-User-Id") UUID userId,
                              @RequestHeader("X-User-username") String username) {
        Product modifyProduct = productOrchestrator.modifyProduct(dto,id,userRole, userId, username);
        return modifyProduct.getId();
    }


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

    /**
     * productId 리스트로 product 리스트 가져오기
     * @param productIdList
     * @return
     */
    @PostMapping("/byIdList")
    public List<ProductResponseDto> getProductsById(@RequestBody  List<UUID> productIdList) {
        return productService.getProductsById(productIdList);
    }



}
