package com.msa.product_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.global.ProductErrorCode;
import com.msa.product_service.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * 상품 리스트를 반환.
     * @return
     */
    @Transactional(readOnly = true)
    public PageRes<ProductResponseDto> getProducts(Pageable pageable, ProductSearchDto searchDto) {
        //검색 조건 및 정렬 조건에 맞게 상품 리스트를 반환.
        return productRepository.searchProduct(searchDto, pageable);
    }

    /**
     * 특정 상품 1개를 조회.
     * @param productId
     * @return
     */
    @Transactional(readOnly = true)
    public Product getProduct(UUID  productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new CustomException(
            ProductErrorCode.PRODUCT_NOT_FOUND));
        return  product;
    }

    /**
     * 상품 수정
     * @param dto
     * @return
     */
    @Transactional
    public Product modifyProduct(ProductRequestDto dto,UUID id){
        Product modifyProduct = productRepository.findById(id).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));
        return modifyProduct.modifyProduct(dto);
    }


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
    public Product deleteProduct(UUID productId, String username) {
        Product deletedProduct = productRepository.findById(productId).orElseThrow(() -> new CustomException(ProductErrorCode.PRODUCT_NOT_FOUND));

        deletedProduct.delete(username);
        return deletedProduct;
    }

    /**
     * 상품 아이디로 상품 리스트 가져오기
     *
     * @param productIdList
     * @return
     */
    public List<ProductResponseDto> getProductsById(List<UUID> productIdList) {
        //아이디에 해당하는 상품 목록을 가져온다.
        List<Product> productList = productRepository.findAllById(productIdList);
        //반환할 ProductResponseDto 리스트를 만든다.
        List<ProductResponseDto> productResponseDtoList = new ArrayList<>();

        //상품 목록을 반복하여
        for (Product product : productList) {
            //상품 목록을 ProductResponseDto로 만든다.
            ProductResponseDto productResponseDto = ProductResponseDto.from(product);
            //만든 ProductResponseDto를 ProductResponseDto 리스트에 추가한다.
            productResponseDtoList.add(productResponseDto);
        }
        return productResponseDtoList;
    }



}
