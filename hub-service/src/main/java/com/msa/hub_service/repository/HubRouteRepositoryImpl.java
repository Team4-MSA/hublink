package com.msa.hub_service.repository;

import com.msa.hub_service.entity.HubRouteEntity;
import com.msa.hub_service.entity.QHubRouteEntity;
import com.msa.hub_service.entity.RouteType;
import com.querydsl.core.Tuple;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static com.msa.hub_service.entity.QHubRouteEntity.hubRouteEntity;

@RequiredArgsConstructor
public class HubRouteRepositoryImpl implements HubRouteRepositoryCustom {
    private final JPAQueryFactory queryFactory;
    private static final double H2H_DISTANCE_THRESHOLD_KM = 200.0;

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

    @Override
    public List<HubRouteEntity> findOptimalTransitRoute(UUID departureHubId, UUID arrivalHubId, BigDecimal directDistance) {
        QHubRouteEntity route1 = new QHubRouteEntity("route1"); // 출발 -> 중간
        QHubRouteEntity route2 = new QHubRouteEntity("route2"); // 중간 -> 도착

        double doubleDirectDistance = directDistance.setScale(2, RoundingMode.HALF_UP).doubleValue();

        Tuple result = queryFactory
                .select(route1, route2)
                .from(route1)
                .innerJoin(route2)
                .on(route1.arrivalHub.hubId.eq(route2.departureHub.hubId))
                .where(
                        // 1. 출발지 도착지 설정
                        route1.departureHub.hubId.eq(departureHubId),
                        route2.arrivalHub.hubId.eq(arrivalHubId),

                        // 2. 각 구간 거리는 200km 미만
                        route1.estimatedDistanceKm.lt(H2H_DISTANCE_THRESHOLD_KM),
                        route2.estimatedDistanceKm.lt(H2H_DISTANCE_THRESHOLD_KM),

                        // 3. 총 이동 거리는 직접 경로 거리의 1.5배 이하
                        route1.estimatedDistanceKm.add(route2.estimatedDistanceKm).loe(doubleDirectDistance * 1.5)
                )
                // 4. 총 이동 거리가 가장 짧은 순으로 정렬
                .orderBy(route1.estimatedDistanceKm.add(route2.estimatedDistanceKm).asc())
                .fetchFirst();

        // 조건을 만족하는 경로가 없으면 빈 리스트 반환
        if (result == null) {
            return Collections.emptyList();
        }

        HubRouteEntity transit1 = result.get(route1);
        HubRouteEntity transit2 = result.get(route2);

        if (transit1 == null || transit2 == null) {
            return Collections.emptyList();
        }

        // 조회된 2개의 엔티티를 리스트로 묶어서 반환
        return List.of(transit1, transit2);
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
