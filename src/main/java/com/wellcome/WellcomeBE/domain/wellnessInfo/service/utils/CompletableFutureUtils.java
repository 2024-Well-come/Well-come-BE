package com.wellcome.WellcomeBE.domain.wellnessInfo.service.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;

public class CompletableFutureUtils {

    // 모든 Future 성공 시 결과를 합산하여 반환, 첫번째 실패가 감지되면 즉시 예외 반환
    public static CompletableFuture<Integer> sumOrFirstFail(List<CompletableFuture<Integer>> futures) {
        CompletableFuture<Integer> allSuccess =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                        .thenApply(v -> futures.stream().mapToInt(CompletableFuture::join).sum());

        CompletableFuture<Integer> firstFailure = new CompletableFuture<>();
        for (CompletableFuture<Integer> f : futures) {
            f.whenComplete((r, ex) -> {
                if (ex != null) {
                    Throwable cause = (ex instanceof CompletionException) ? ex.getCause() : ex;
                    firstFailure.completeExceptionally(cause);
                }
            });
        }

        return allSuccess.applyToEither(firstFailure, Function.identity());
    }
}
