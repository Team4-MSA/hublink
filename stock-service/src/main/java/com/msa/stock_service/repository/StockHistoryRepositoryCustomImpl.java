package com.msa.stock_service.repository;

import com.msa.core_common.response.paging.PageRes;
import com.msa.stock_service.dto.StockHistorySearchResponseDto;
import com.msa.stock_service.entity.StockHistory;
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

import static com.msa.stock_service.entity.QStockHistory.stockHistory;
@RequiredArgsConstructor
public class StockHistoryRepositoryCustomImpl implements StockHistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public PageRes<StockHistorySearchResponseDto> searchHistoriesByproductId(UUID productId, Pageable pageable) {
        List<OrderSpecifier<?>> orders = getAllOrderSpecifiers(pageable);
        QueryResults<StockHistory> result = queryFactory
            .selectFrom(stockHistory)
            .where(productIdEq(productId), isDeleted())
            .orderBy(orders.toArray(new OrderSpecifier[0]))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetchResults();

        List<StockHistorySearchResponseDto> stockResponseDtos = result.getResults().stream().map(StockHistorySearchResponseDto::from).collect(
            Collectors.toList());

        long total = result.getTotal();

        Page<StockHistorySearchResponseDto> page = new PageImpl<>(stockResponseDtos, pageable, total);

        return new PageRes<>(page);
    }
    //productId는 반드시 필요.
    private BooleanExpression productIdEq(UUID  productId) {
        return stockHistory.productId.eq(productId);
    }

    private BooleanExpression isDeleted() {
        return stockHistory.deletedAt.isNull();
    }

    private List<OrderSpecifier<?>> getAllOrderSpecifiers(Pageable pageable) {
        List<OrderSpecifier<?>> orders = new ArrayList<>();

        for(Sort.Order sortOrder : pageable.getSort()) {
            Order direction = sortOrder.isAscending() ? Order.ASC : Order.DESC;
            switch (sortOrder.getProperty()) {
                case "createdAt":
                    orders.add(new OrderSpecifier<>(direction,stockHistory.createdAt));
                    break;
                default:
                    break;
            }
        }
        return orders;
    }
}
