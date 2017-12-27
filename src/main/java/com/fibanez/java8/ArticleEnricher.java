package com.fibanez.java8;

import com.fibanez.java8.models.RichArticle;

import java.util.concurrent.Future;

public interface ArticleEnricher {
    Future<RichArticle> enrichArticleWithId(String articleId);
}
