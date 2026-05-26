package com.msa.product_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.product_service.dto.ProductResponseDto;
import com.msa.product_service.dto.ProductSearchDto;
import com.msa.product_service.entity.Product;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static com.msa.product_service.entity.QProduct.product;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public PageRes<ProductResponseDto> searchProduct(ProductSearchDto dto, Pageable pageable) {

        List<OrderSpecifier<?>> orderSpecifiers = getAllOrderSpecifiers(pageable);

        QueryResults<Product> result = queryFactory
            .selectFrom(product)
            .where(
                productNameContains(dto.getProductName()),
                hubIdEq(dto.getHubId()),
                priceGoe(dto.getMinPrice()),
                priceLoe(dto.getMaxPrice()),
                isDeleted()
            )
            .orderBy(orderSpecifiers.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<ProductResponseDto> content = result.getResults().stream()
            .map(ProductResponseDto::from)
            .collect(Collectors.toList());

        long total = result.getTotal();

        // 1. 먼저 표준 Page 객체를 만들고
        Page<ProductResponseDto> page = new PageImpl<>(content, pageable, total);

        // 2. 그 Page를 PageRes 생성자에 넘긴다
        return new PageRes<>(page);
    }

    // 상품명 부분 검색 (LIKE %productName%)
    private BooleanExpression productNameContains(String productName) {
        return productName != null ? product.name.contains(productName) : null;
    }

    //hubId 포함
    private BooleanExpression hubIdEq(UUID hubId) {
        return hubId != null ? product.hubId.eq(hubId) : null;
    }

    // 최소 가격 이상 (price >= minPrice)
    private BooleanExpression priceGoe(Integer minPrice) {
        return minPrice != null ? product.price.goe(minPrice) : null;
    }

    // 최대 가격 이하 (price <= maxPrice)
    private BooleanExpression priceLoe(Integer maxPrice) {
        return maxPrice != null ? product.price.loe(maxPrice) : null;
    }

    private BooleanExpression isDeleted() {
        return product.deletedAt.isNull();
    }

    /**
     * Pageable 객체에서 정렬 조건을 가져온다.
     * 현재는 createdAt 말곤 없지만, 추후 추가될 것을 염두하여 구현.
     */
    List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orderSpecifiers = new ArrayList<>();

        for (Sort.Order order : pageable.getSort()) {
            Order direction = order.isAscending() ? Order.ASC : Order.DESC;
            switch (order.getProperty()) {
                case "createdAt":
                    orderSpecifiers.add(new OrderSpecifier<>(direction, product.createdAt));
                    break;
                default:
                    break;
            }
        }

        return orderSpecifiers;
    }
}