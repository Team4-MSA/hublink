package com.msa.delivery_service.service;

import com.msa.core_common.error.exception.CustomException;
import com.msa.delivery_service.domain.enums.DeliveryErrorCode;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class DeliveryAssignmentLockService {

    private static final long WAIT_SECOND = 3L;

    private final RedissonClient redissonClient;

    /*
        諛곗넚湲곗궗 諛곗젙???꾪븳 Lock ?ㅼ젙
        諛곗넚 1嫄댁뿉 媛?諛곗넚 寃쎈줈?ㅼ쓽 諛곗넚 湲곗궗 N紐낆쓣 諛곗젙?섍린 ?뚮Ц??hub_id瑜?湲곗??쇰줈 Lock???띾뱷?섍퀬
        紐⑤뱺 Lock???띾뱷??寃쎌슦??function???몄텧?댁꽌 泥섎━
    */
    public <T> T executeWithLocks(List<String> lockKeys, Supplier<T> function) {
        // ?띾뱷??Lock??紐⑥븘?먭린 ?꾪븳 List
        List<RLock> acquiredLocks = new ArrayList<>();

        try {
            // 以묐났 ???쒓굅
            // ???뺣젹???듯빐 ???띾뱷 ?쒖꽌 ?듭씪 -> ?곕뱶??諛⑹?
            List<String> sortedKeys = lockKeys.stream()
                    .distinct()
                    .sorted()
                    .toList();
            for (String key : sortedKeys) {
                RLock lock = redissonClient.getLock(key);

                // leaseTime??二쇱??딄퀬 watchdog ?쒖꽦??
                boolean locked = lock.tryLock(WAIT_SECOND, TimeUnit.SECONDS);

                // Lock???띾뱷?섏? 紐삵븷 寃쎌슦 ?묒뾽 ?ㅽ뙣
                // finally?먯꽌 Lock ?꾨? ?댁젣
                if (!locked) throw new CustomException(DeliveryErrorCode.DELIVERY_ASSIGNMENT_LOCK_TIMEOUT);

                acquiredLocks.add(lock);
            }
            return function.get();
        } catch (InterruptedException e) {
            // interrupt ?곹깭 蹂듦뎄
            Thread.currentThread().interrupt();
            throw new CustomException(DeliveryErrorCode.DELIVERY_ASSIGNMENT_LOCK_TIMEOUT);
        } finally {
            // ??닚?쇰줈 Lock ?댁젣
            for (int i = acquiredLocks.size() - 1; i >= 0; i--) {
                RLock lock = acquiredLocks.get(i);
                if (lock.isHeldByCurrentThread()) lock.unlock();
            }
        }
    }
}
