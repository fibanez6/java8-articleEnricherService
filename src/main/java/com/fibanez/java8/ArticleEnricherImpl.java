package com.fibanez.java8;

import com.fibanez.java8.clients.ArticleRepositoryClient;
import com.fibanez.java8.clients.AssetsServiceClient;
import com.fibanez.java8.models.ArticleReference;
import com.fibanez.java8.models.Image;
import com.fibanez.java8.models.RichArticle;
import com.fibanez.java8.models.Video;
import com.fibanez.java8.utils.Futures;
import com.fibanez.java8.utils.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * This implementation uses CompletableFutures.
 *
 * In order to retrieve the image and video ids from urls. It has been considered the last part of the
 * URL path, which is the name of the content with the extension, as the id of the content. i.e:
 *
 * if the URL looks like:
 *  https://ichef-1.bbci.co.uk/news/1024/cpsprodpb/8F1A/production/_98943663_de27-1.jpg
 * then the id content is:
 *  _98943663_de27-1.jpg
 *
 * @author fibanez
 * @see "https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletableFuture.html"
 */
public class ArticleEnricherImpl implements ArticleEnricher {

    private ArticleRepositoryClient articleRepositoryClient; //Inject or constructor

    private AssetsServiceClient assetsServiceClient; //Inject or constructor

    private ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public Future<RichArticle> enrichArticleWithId(String articleId) {

        CompletableFuture<ArticleReference> articleReferenceFuture = getArticleReferenceFuture(articleId);

        return articleReferenceFuture
                .thenCompose(articleReference -> getImageFutureFromUrl(articleReference.getHeroImageUrl())
                    .thenCombine(getVideoFuturesFromUrls(articleReference.getVideoUrls())
                        ,(image,videos) -> combine(articleReference, image, videos)
        ));
    }

    /**
     * Returns a richArticle
     *
     * @param reference
     * @param image
     * @param videos
     * @return RichArticle
     */
    public RichArticle combine(ArticleReference reference, Image image, List<Video> videos) {
        return new RichArticle(reference.getId()
                , reference.getName()
                , image
                , videos.parallelStream()
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList()));
    }

    /**
     * Given a articleID, returns asynchronously an article future from articleRepositoryClient.
     *
     * @param articleId
     * @return CompletableFuture<ArticleReference> only if the articles is not blank, else
     * @exception CompletableFuture which throws a runtimeException.
     *
     */
    public CompletableFuture<ArticleReference> getArticleReferenceFuture(String articleId) {
        if (StringUtils.isBlank(articleId)) {
            return Futures.generateFutureException("No article found");
        }
        return Futures.toCompletableFuture(articleRepositoryClient.getArticleReferenceForId(articleId), executor);
    }

    /**
     * Given an imageId, retrieves asynchronously the image object from assetsServiceClient.
     *
     * @param imageId
     * @return CompletableFuture<Image> only if the image id is not blank, else
     * returns a CompletableFuture which null value.
     */
    public CompletableFuture<Image> getImageFutureFromClient(String imageId) {
        if (StringUtils.isBlank(imageId)) {
            return CompletableFuture.completedFuture(null);
        }
        return Futures.toCompletableFuture(assetsServiceClient.getImageById(imageId), executor)
                .exceptionally(e -> null);
    }

    /**
     * Given an image url with image identifier, retrieves asynchronously the image object from
     * assetsServiceClient.
     *
     * @param imageUrl
     * @return CompletableFuture<Image> only if the imageUrl is not blank, else
     * returns a CompletableFuture which null value.
     * */
    public CompletableFuture<Image> getImageFutureFromUrl(String imageUrl) {
        String imageId = StringUtils.getLastPathPart(imageUrl);
        return getImageFutureFromClient(imageId);
    }

    /**
     * Given a videoId, retrieves asynchronously the video object from assetsServiceClient.
     *
     * @param videoId
     * @return CompletableFuture<Video> only if the video id is not blank, else
     * returns a CompletableFuture which null value.
     */
    public CompletableFuture<Video> getVideoFutureFromClient(String videoId) {
        if (StringUtils.isBlank(videoId)) {
            return CompletableFuture.completedFuture(null);
        }
        return Futures.toCompletableFuture(assetsServiceClient.getVideoById(videoId), executor)
                .exceptionally(e -> null);
    }

    /**
     * Given a collection of video urls with identifier, returns a collection of video objects
     * from assetsServiceClient.
     *
     * @param videoUrls
     * @return CompletableFuture<List<Video>> only if the collection is not empty and valid urls, else
     * returns a CompletableFuture which a empty list.
     */
    public CompletableFuture<List<Video>> getVideoFuturesFromUrls(Collection<String> videoUrls) {
        if (videoUrls.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> Collections.EMPTY_LIST);
        }
        List<CompletableFuture<Video>> videoFutures = videoUrls.stream()
                .map(videoUrl -> StringUtils.getLastPathPart(videoUrl)) // gets video ids from url
                .filter(id -> !StringUtils.isBlank(id))  // removes null and empty
                .map(id -> getVideoFutureFromClient(id)) // generates a future per videoId
                .collect(Collectors.toList());
        return Futures.joinFutures(videoFutures.stream());
    }

}
