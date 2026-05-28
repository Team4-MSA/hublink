package com.msa.delivery_service.infrastructure;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartialIndexInitializer {
    /*
        Partial unique index 생성을 위해 서버가 시작될 때 네이티브 쿼리 수행
        1. 배송 생성 제약
        2. 업체 배송 담당자 배정 제약
        3. 허브 배송 담당자 배정 제약
    */

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void initializeIndexes() {
        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_p_deliveries_active_order_id
                ON delivery_service.p_deliveries (order_id)
                WHERE deleted_at IS NULL
                """);

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_p_deliveries_active_company_delivery_manager
                ON delivery_service.p_deliveries (company_delivery_manager_id)
                WHERE deleted_at IS NULL
                  AND status NOT IN ('DELIVERED', 'CANCELLED')
                """);

        jdbcTemplate.execute("""
                CREATE UNIQUE INDEX IF NOT EXISTS uk_p_delivery_route_histories_active_delivery_manager
                ON delivery_service.p_delivery_route_histories (delivery_manager_id)
                WHERE deleted_at IS NULL
                  AND status NOT IN ('COMPLETED', 'SKIPPED', 'FAILED')
                """);
    }
}
