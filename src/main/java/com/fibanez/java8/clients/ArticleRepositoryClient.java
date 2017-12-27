package com.fibanez.java8.clients;

import com.fibanez.java8.models.ArticleReference;

import java.util.concurrent.Future;

public interface ArticleRepositoryClient {
    Future<ArticleReference> getArticleReferenceForId(String articleId);
}
