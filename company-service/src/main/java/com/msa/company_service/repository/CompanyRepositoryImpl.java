package com.msa.company_service.repository;

import com.msa.company_service.entity.CompanyEntity;
import com.msa.company_service.entity.CompanyType;
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
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

import static com.msa.company_service.entity.QCompanyEntity.companyEntity;

@RequiredArgsConstructor
public class CompanyRepositoryImpl implements CompanyRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<CompanyEntity> searchCompanies(UUID hubId, String name, CompanyType type, String address, Pageable pageable) {
        JPAQuery<CompanyEntity> query = queryFactory
                .selectFrom(companyEntity)
                .where(
                        eqHubId(hubId),
                        containsName(name),
                        eqType(type),
                        containsAddress(address)
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize());

        for (Sort.Order o : pageable.getSort()) {
            PathBuilder<CompanyEntity> pathBuilder = new PathBuilder<>(companyEntity.getType(), companyEntity.getMetadata());
            query.orderBy(new OrderSpecifier<>(
                    o.isAscending() ? Order.ASC : Order.DESC,
                    pathBuilder.getComparable(o.getProperty(), Comparable.class)
            ));
        }

        List<CompanyEntity> content = query.fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(companyEntity.count())
                .from(companyEntity)
                .where(
                        eqHubId(hubId),
                        containsName(name),
                        eqType(type),
                        containsAddress(address)
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqHubId(UUID hubId) {
        return hubId != null ? companyEntity.hubId.eq(hubId) : null;
    }

    private BooleanExpression containsName(String name) {
        return StringUtils.hasText(name) ? companyEntity.name.contains(name) : null;
    }

    private BooleanExpression eqType(CompanyType type) {
        return type != null ? companyEntity.type.eq(type) : null;
    }

    private BooleanExpression containsAddress(String address) {
        return StringUtils.hasText(address) ? companyEntity.address.contains(address) : null;
    }
}
