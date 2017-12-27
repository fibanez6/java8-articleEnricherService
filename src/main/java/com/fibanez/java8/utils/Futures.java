package com.fibanez.java8.utils;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author fibanez
 */
public class Futures {

    /**
     * Wrap a sync or async future object into a completableFuture object
     *
     * @param future
     * @param executor
     * @param <T>
     * @return CompletableFuture<T>
     */
    public static <T> CompletableFuture<T> toCompletableFuture(Future<T> future, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return future.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }, executor);
    }

    /**
     * Given a stream of futures, return a future with a list of objects
     *
     * @param futures
     * @param <T>
     * @return CompletableFuture<List<T>>
     */
    public static <T> CompletableFuture<List<T>> joinFutures(Stream<CompletableFuture<T>> futures) {
        List<CompletableFuture<T>> futureList = futures
                .filter(f -> f != null)
                .collect(Collectors.toList());

        CompletableFuture<Void> allDoneFuture =CompletableFuture.allOf(
                futureList.toArray(new CompletableFuture[futureList.size()]));

        return allDoneFuture.thenApply(v ->
                futureList.stream().map(future -> future.join()).collect(Collectors.toList()));
    }

    /**
     * Returns a completableFuture with an exception
     * @param mensage
     * @return CompletableFuture which throws an exception.
     */
    public static CompletableFuture generateFutureException(String mensage) {
        CompletableFuture future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException(mensage));
        return future;
    }
}