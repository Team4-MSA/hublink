CREATE SCHEMA IF NOT EXISTS user_service;;

-- 허브별 배송 순번 유니크 제약조건
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'uk_hub_delivery_sequence'
    ) THEN
        ALTER TABLE user_service.p_delivery_managers
            ADD CONSTRAINT uk_hub_delivery_sequence UNIQUE (hub_id, delivery_sequence);
    END IF;
END $$;;
