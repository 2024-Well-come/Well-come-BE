package com.wellcome.WellcomeBE.global.p6spy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.List;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class QueryLoggingAspect {

    private final QueryLogProperties queryLogProperties;
    private final SlowQueryLogRepository slowQueryLogRepository;

    @Around("execution(* com.wellcome.WellcomeBE.domain.Article.controller..*(..)) || execution(* com.wellcome.WellcomeBE.domain.community.controller..*(..))")
    public Object logQueryInfo(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = joinPoint.getSignature().toShortString();
        CustomP6SpyLogger.setEndpoint(endpoint);
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();  // 실제 메서드 실행

        long totalElapsed = System.currentTimeMillis() - start;
        log.info("📌 API: {} 실행 시간: {} ms", endpoint, totalElapsed);

        List<CustomP6SpyLogger.QueryInfo> queries = CustomP6SpyLogger.getAndClearQueryInfo();

        for (CustomP6SpyLogger.QueryInfo query : queries) {
            log.info("[SQL][{}ms] {}", query.elapsedTime(), query.sql());
            if (query.elapsedTime() > queryLogProperties.getSlowThreshold()) {
                log.warn("🛑 SLOW QUERY [{}ms]: {}", query.elapsedTime(), query.sql());
                SlowQueryLog slowQuery = new SlowQueryLog();
                slowQuery.setSql(query.sql());
                slowQuery.setElapsedTime(query.elapsedTime());
                slowQuery.setEndpoint(query.endpoint());
                slowQueryLogRepository.save(slowQuery);
            }
        }

        return result;
    }

}

