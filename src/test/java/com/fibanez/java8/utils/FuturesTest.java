package com.fibanez.java8.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

/**
 * @author fibanez
 */
public class FuturesTest {

    @Test
    public void when_future_expeted_completableFuture() throws Exception {
        Future future = mock(Future.class);
        Executor executor = mock(Executor.class);

        CompletableFuture completableFuture = Futures.toCompletableFuture(future, executor);
        assertThat(completableFuture, is(notNullValue()));
    }

    @Test
    public void when_stream3Futures_expeted_completableFutureWithListSize3() throws Exception {
        ArrayList<CompletableFuture<String>> futures = new ArrayList<CompletableFuture<String>>() {{
            add(CompletableFuture.completedFuture(new String("future1")));
            add(CompletableFuture.completedFuture(new String("future2")));
            add(CompletableFuture.completedFuture(new String("future3")));
        }};

        CompletableFuture<List<String>> result = Futures.joinFutures(futures.stream());
        assertThat(result.get(), hasSize(3));
    }

    @Test
    public void when_exceptionMessage_expeted_isCompletedExceptionally() throws Exception {
        CompletableFuture result = Futures.generateFutureException("error");
        assertTrue(result.isCompletedExceptionally());
    }

}