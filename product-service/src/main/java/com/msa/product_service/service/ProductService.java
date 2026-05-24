package com.msa.product_service.service;

import com.msa.product_service.client.CompanyClient;
import com.msa.product_service.client.CompanyResponseDto;
import com.msa.product_service.dto.ProductModifyDto;
import com.msa.product_service.dto.ProductRequestDto;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.entity.Product;
import com.msa.product_service.repository.ProductRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Product getProduct(UUID  productId) {
        Product product = productRepository.findById(productId).orElseThrow(() -> new IllegalArgumentException("상품이 없음."));
        return  product;
    }

    /**
     * 상품 수정
     * @param dto
     * @return
     */
    @Transactional
    public Product modifyProduct(ProductRequestDto dto,UUID id){
        Product modifyProduct = productRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("상품이 존재하지 않습니다."));
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
        Product deletedProduct = productRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("상품이 없음"));
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
