package com.msa.product_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import org.springframework.data.domain.Pageable;

public interface ProductRepositoryCustom {
    PageRes<ProductResponseDto> searchProduct(ProductSearchDto dto, Pageable pageable);
}
