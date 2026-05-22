package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;
import java.util.UUID;

import static com.msa.hub_service.entity.QHubRouteEntity.hubRouteEntity;

@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<HubRouteEntity> searchHubRoutes(UUID departureHubId, UUID arrivalHubId, RouteType routeType, Pageable pageable) {

        JPAQuery<HubRouteEntity> query = queryFactory
                .selectFrom(hubRouteEntity)
                .where(
                        eqDepartureHubId(departureHubId),
                        eqArrivalHubId(arrivalHubId),
                        eqRouteType(routeType)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        // 정렬 조건
        for (Sort.Order o : pageable.getSort()) {
            PathBuilder<HubRouteEntity> pathBuilder = new PathBuilder<>(hubRouteEntity.getType(), hubRouteEntity.getMetadata());
            query.orderBy(new OrderSpecifier<>(
                    o.isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.getComparable(o.getProperty(), Comparable.class)
            ));
        }

        List<HubRouteEntity> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(hubRouteEntity.count())
                .from(hubRouteEntity)
                .where(
                        eqDepartureHubId(departureHubId),
                        eqArrivalHubId(arrivalHubId),
                        eqRouteType(routeType)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<HubRouteEntity> findByInvolvedHubId(UUID hubId) {
        return queryFactory
                .selectFrom(hubRouteEntity)
                .where(
                        hubRouteEntity.departureHub.hubId.eq(hubId)
                                .or(hubRouteEntity.arrivalHub.hubId.eq(hubId))
                )
                .fetch();
    }


    private BooleanExpression eqDepartureHubId(UUID departureHubId) {
        return departureHubId != null ? hubRouteEntity.departureHub.hubId.eq(departureHubId) : null;
    }

    private BooleanExpression eqArrivalHubId(UUID arrivalHubId) {
        return arrivalHubId != null ? hubRouteEntity.arrivalHub.hubId.eq(arrivalHubId) : null;
    }

    private BooleanExpression eqRouteType(RouteType routeType) {
        return routeType != null ? hubRouteEntity.routeType.eq(routeType) : null;
    }
}
