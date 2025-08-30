package com.wellcome.WellcomeBE.global;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.Callable;

@Slf4j
@Component
public class RetryWithBackoff {

    public <T> T execute(Callable<T> task, int maxAttempts, long initialDelayMillis) throws Exception {
        int attempt = 0;
        long delay = initialDelayMillis;

        while (true) {
            try {
                return task.call();
            } catch (Exception e) {
                attempt++;
                if (attempt >= maxAttempts) {
                    throw e;
                }
                log.warn("[재시도] {}번째 시도 실패, {}ms 후 재시도: {}", attempt, delay, e.getMessage());
                Thread.sleep(delay);
                delay *= 2; // 지수 백오프: 2배씩 증가
            }
        }
    }
}
