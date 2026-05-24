CREATE SCHEMA IF NOT EXISTS user_service;

-- 허브별 배송 순번 유니크 제약조건 (동시 INSERT 시 중복 방지)
ALTER TABLE user_service.p_delivery_managers
    ADD CONSTRAINT uk_hub_delivery_sequence UNIQUE (hub_id, delivery_sequence);
