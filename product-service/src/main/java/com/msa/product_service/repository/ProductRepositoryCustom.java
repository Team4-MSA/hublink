package com.msa.product_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import com.msa.product_service.entity.Product;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    Page<Product> searchProduct(String productName, Integer minPrice, Integer maxPrice, UUID hubId, Pageable pageable);
}
