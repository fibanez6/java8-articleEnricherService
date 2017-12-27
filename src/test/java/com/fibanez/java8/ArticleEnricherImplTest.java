package com.fibanez.java8;

import com.fibanez.java8.clients.ArticleRepositoryClient;
import com.fibanez.java8.clients.AssetsServiceClient;
import com.fibanez.java8.models.ArticleReference;
import com.fibanez.java8.models.Image;
import com.fibanez.java8.models.RichArticle;
import com.fibanez.java8.models.Video;
import com.fibanez.java8.utils.StringUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * @author fibanez
 */
@RunWith(PowerMockRunner.class)
public class ArticleEnricherImplTest {

    @Mock
    private ArticleRepositoryClient repositoryClient;

    @Mock
    private AssetsServiceClient assetsServiceClient;

    @InjectMocks
    private ArticleEnricherImpl enricher;

    private static String referenceImageUrl;

    private static List<String> videoUrls;

    private static int numberOfProcessors;

    private static ScheduledExecutorService scheduler;

    Random rnd = new Random();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        referenceImageUrl = "http://www.linktoimage/idimage1.jpg?param1=value1&param2=value2";
        videoUrls = Arrays.asList(
                "http://www.linktovideo/idvideo1.mov",
                "http://www.linktovideo/idvideo2.mov?param1=value1",
                "http://www.linktovideo/idvideo3.mov?param1=value1&param2=value2"
        );

        Runtime runtime = Runtime.getRuntime();
        numberOfProcessors = runtime.availableProcessors();
        scheduler = Executors.newScheduledThreadPool(numberOfProcessors * 2);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        referenceImageUrl = null;
        videoUrls = null;
    }

    @Test
    public void when_combine_expect_richArticle() throws Exception {
        ArticleReference reference = new ArticleReference("arefId","refName",referenceImageUrl, videoUrls);
        Image image = new Image("idImage1.jpg","altText");
        List<Video> videos = Arrays.asList( new Video("videoId", "caption"));

        RichArticle richArticle = enricher.combine(reference, image, videos);
        assertThat(richArticle, is(notNullValue()));
    }

    @Test
    public void when_combine_expect_richArticleWithData() throws Exception {
        String referenceId = "arefId";
        String referenceName = "refName";

        ArticleReference reference = new ArticleReference(referenceId,referenceName,referenceImageUrl, videoUrls);
        Image image = new Image("idImage1.jpg","altText");
        List<Video> videos = Arrays.asList( new Video("videoId", "caption"));

        RichArticle richArticle = enricher.combine(reference, image, videos);
        assertThat(richArticle.getId(), is(referenceId));
        assertThat(richArticle.getName(), is(referenceName));
        assertThat(richArticle.getHeroImage(), is(image));
        assertThat(richArticle.getVideos(), is(videos));
    }
    
    @Test
    public void when_imageId_expect_imageFutureFromClient() throws Exception {
        Image imageTest = new Image("imageId","alt");
        Future mockFuture = getMockedFutureWithReturn(imageTest);

        when(assetsServiceClient.getImageById(anyString())).thenReturn(mockFuture);
        CompletableFuture<Image> futureResult = enricher.getImageFutureFromClient("imageId");
        assertThat(futureResult.get(), is(imageTest));
    }

    @Test
    public void when_noImageId_expect_nullFutureFromClient() throws Exception {
        CompletableFuture<Image> futureResult = enricher.getImageFutureFromClient(StringUtils.EMPTY);
        assertThat(futureResult.get(), is(nullValue()));
    }

    @Test
    public void when_noImageId_verify_assetServiceIsNotCalled() throws Exception {
        CompletableFuture<Image> futureResult = enricher.getImageFutureFromClient(StringUtils.EMPTY);
        verify(assetsServiceClient, times(0)).getImageById(anyString());
    }

    @Test
    public void when_assetsServiceFails_expect_nullImageFuture() throws Exception {
        CompletableFuture<Image> future = getFailedCompletableFuture();

        when(assetsServiceClient.getImageById(anyString())).thenReturn(future);
        CompletableFuture<Image> futureResult = enricher.getImageFutureFromClient("imageId");
        assertThat(futureResult.get(), is(nullValue()));
    }


    @Test
    public void when_assetsServiceFails_expect_assetServiceIsCalledOnce() throws Exception {
        CompletableFuture<Image> future = getFailedCompletableFuture();

        when(assetsServiceClient.getImageById(anyString())).thenReturn(future);
        enricher.getImageFutureFromClient("imageId");
        verify(assetsServiceClient, times(1)).getImageById(anyString());
    }

    @Test
    public void when_imageURL_expect_imageFuture() throws Exception {
        String imageId = "idimage1.jpg";
        Image imageTest = new Image(imageId,"alt");
        Future mockFuture = getMockedFutureWithReturn(imageTest);

        when(assetsServiceClient.getImageById(anyString())).thenReturn(mockFuture);
        CompletableFuture<Image> futureResult = enricher.getImageFutureFromUrl(referenceImageUrl);
        assertThat(futureResult.get(), is(imageTest));
    }

    @Test
    public void when_imageURL_verify_assetServiceIsCalledOnce() throws Exception {
        String imageId = "idimage1.jpg";
        Image imageTest = new Image(imageId,"alt");
        Future mockFuture = getMockedFutureWithReturn(imageTest);

        when(assetsServiceClient.getImageById(anyString())).thenReturn(mockFuture);
        enricher.getImageFutureFromUrl(referenceImageUrl);
        verify(assetsServiceClient, times(1)).getImageById(imageId);
    }

    @Test
    public void when_wrongImageURL_expect_nullFuture() throws Exception {
        String wrongImageUrl = "http://www.wrongImage.com";

        CompletableFuture<Image> futureResult = enricher.getImageFutureFromUrl(wrongImageUrl);
        assertThat(futureResult.get(), is(nullValue()));
    }

    @Test
    public void when_wrongImageURL_verify_assetServiceIsNotCalled() throws Exception {
        String wrongImageUrl = "http://www.wrongImage.com";

        CompletableFuture<Image> futureResult = enricher.getImageFutureFromUrl(wrongImageUrl);
        verify(assetsServiceClient, times(0)).getImageById(anyString());
    }

    @Test
    public void when_videoId_expect_videoFutureFromClient() throws Exception {
        Video videoTest = new Video("id","caption");
        Future mockFuture = getMockedFutureWithReturn(videoTest);

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(mockFuture);
        CompletableFuture<Video> futureResult = enricher.getVideoFutureFromClient("videoId");
        assertThat(futureResult.get(), is(videoTest));
    }

    @Test
    public void when_assetsServiceFails_expect_nullVideoFuture() throws Exception {
        CompletableFuture failedFuture = getFailedCompletableFuture();

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(failedFuture);
        CompletableFuture<Video> futureResult = enricher.getVideoFutureFromClient("videoId");
        assertThat(futureResult.get(), is(nullValue()));
    }

    @Test
    public void when_assetsServiceFails_verify_assetServiceIsCalledOnce() throws Exception {
        CompletableFuture failedFuture = getFailedCompletableFuture();

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(failedFuture);
        enricher.getVideoFutureFromClient("videoId");
        verify(assetsServiceClient, times(1)).getVideoById(anyString());
    }

    @Test
    public void when_collectionVideoUrls_expect_collectionVideos() throws Exception {
        Video video1Test = new Video("idvideo1.mov","caption1");
        Video video2Test = new Video("idvideo2.mov","caption2");
        Video video3Test = new Video("idvideo3.mov","caption3");
        Future mockFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(mockFuture);
        CompletableFuture<List<Video>> futureResult = enricher.getVideoFuturesFromUrls(videoUrls);
        assertThat(futureResult.get(), containsInAnyOrder(video1Test,video2Test,video3Test));
    }

    @Test
    public void when_collectionVideoUrls_verify_assetServiceIsCalled3Times() throws Exception {
        Video video1Test = new Video("idvideo1.mov","caption1");
        Video video2Test = new Video("idvideo2.mov","caption2");
        Video video3Test = new Video("idvideo3.mov","caption3");

        Future mockFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);
        when(assetsServiceClient.getVideoById(anyString())).thenReturn(mockFuture);
        enricher.getVideoFuturesFromUrls(videoUrls);
        verify(assetsServiceClient, times(3)).getVideoById(anyString());
    }

    @Test
    public void when_collectionVideoUrlsAndInvalidUrl_expect_collectionValidVideos() throws Exception {
        Video video1Test = new Video("idvideo1.mov","caption1");
        Video video2Test = new Video("idvideo2.mov","caption2");
        Video video3Test = new Video("idvideo3.mov","caption3");

        List<String> listWithWrongUrls = new ArrayList<>(videoUrls);
        listWithWrongUrls.add("http://www.noidurl.com");
        listWithWrongUrls.add("IAmNotaUrl");

        Future mockFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(mockFuture);
        CompletableFuture<List<Video>> futureResult = enricher.getVideoFuturesFromUrls(listWithWrongUrls);
        assertThat(futureResult.get(), containsInAnyOrder(video1Test,video2Test,video3Test));
    }

    @Test
    public void when_collectionVideoUrlsAndInvalidUrl_verify_assetServiceIsCalled3Times() throws Exception {
        Video video1Test = new Video("idvideo1.mov","caption1");
        Video video2Test = new Video("idvideo2.mov","caption2");
        Video video3Test = new Video("idvideo3.mov","caption3");

        List<String> listWithWrongUrls = new ArrayList<>(videoUrls);
        listWithWrongUrls.add("http://www.noidurl.com");
        listWithWrongUrls.add("IAmNotaUrl");

        Future mockFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(assetsServiceClient.getVideoById(anyString())).thenReturn(mockFuture);
        enricher.getVideoFuturesFromUrls(listWithWrongUrls);
        verify(assetsServiceClient, times(3)).getVideoById(anyString());
    }

    @Test
    public void when_emptyCollectionVideos_expect_collectionEmpty() throws Exception {
        CompletableFuture<List<Video>> futureResult = enricher.getVideoFuturesFromUrls(Collections.EMPTY_LIST);
        assertThat(futureResult.get(), hasSize(0));
    }

    @Test
    public void when_emptyCollectionVideos_verify_assetServiceIsNotCalled() throws Exception {
        enricher.getVideoFuturesFromUrls(Collections.EMPTY_LIST);
        verify(assetsServiceClient, times(0)).getVideoById(anyString());
    }

    @Test
    public void when_articleId_expect_ArticleReferenceFuture() throws Exception {
        ArticleReference referenceTest = new ArticleReference("id","alt", "imageUrl", Collections.EMPTY_LIST);
        Future mockFuture = getMockedFutureWithReturn(referenceTest);

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(mockFuture);
        CompletableFuture<ArticleReference> futureResult = enricher.getArticleReferenceFuture("refId");
        assertThat(futureResult.get(), is(referenceTest));
    }

    @Test(expected = ExecutionException.class)
    public void when_repositoryClientFails_expect_executionException() throws Exception {
        CompletableFuture failedFuture = getFailedCompletableFuture();

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(failedFuture);
        CompletableFuture<ArticleReference> futureResult = enricher.getArticleReferenceFuture("WrongId");
        futureResult.get();
    }

    @Test(expected = ExecutionException.class)
    public void when_noArticleReferenceId_expect_executionException() throws Exception {
        CompletableFuture<ArticleReference> futureResult = enricher.getArticleReferenceFuture(StringUtils.EMPTY);
        futureResult.get();
    }
    
    @Test
    public void when_articleId_expect_richArticleFuture() throws Exception {
        String articleId = "articleId";
        ArticleReference reference = new ArticleReference(articleId, "articleName", referenceImageUrl, videoUrls);
        Image imageTest = new Image("idimage1.jpg", "alt");
        Video video1Test = new Video("idvideo1.mov", "caption1");
        Video video2Test = new Video("idvideo2.mov", "caption2");
        Video video3Test = new Video("idvideo3.mov", "caption3");

        Future referenceFuture = getMockedFutureWithReturn(reference);
        Future imageFuture = getMockedFutureWithReturn(imageTest);
        Future videoFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(referenceFuture);
        when(assetsServiceClient.getImageById(anyString())).thenReturn(imageFuture);
        when(assetsServiceClient.getVideoById(anyString())).thenReturn(videoFuture);

        Future<RichArticle> futureResult = enricher.enrichArticleWithId(reference.getId());
        assertThat(futureResult.get(), is(notNullValue()));
    }

    @Test
    public void when_articleId_expect_richArticleFutureWithData() throws Exception {
        String articleId = "articleId";
        String articleName = "articleName";
        ArticleReference reference = new ArticleReference(articleId, articleName, referenceImageUrl, videoUrls);
        Image imageTest = new Image("idimage1.jpg", "alt");
        Video video1Test = new Video("idvideo1.mov", "caption1");
        Video video2Test = new Video("idvideo2.mov", "caption2");
        Video video3Test = new Video("idvideo3.mov", "caption3");

        Future referenceFuture = getMockedFutureWithReturn(reference);
        Future imageFuture = getMockedFutureWithReturn(imageTest);
        Future videoFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(referenceFuture);
        when(assetsServiceClient.getImageById(anyString())).thenReturn(imageFuture);
        when(assetsServiceClient.getVideoById(anyString())).thenReturn(videoFuture);

        Future<RichArticle> futureResult = enricher.enrichArticleWithId(reference.getId());
        RichArticle richArticle = futureResult.get();

        assertThat(richArticle.getId(), is(articleId));
        assertThat(richArticle.getName(), is(articleName));
        assertThat(richArticle.getHeroImage(), is(imageTest));
        assertThat(richArticle.getVideos(), containsInAnyOrder(video1Test, video2Test, video3Test));
    }

    @Test
    public void when_articleIdAndAssetsSClientPartiallyFails_expect_richArticleWithoutImage() throws Exception {
        String articleId = "articleId";
        String articleName = "articleName";
        Video video1Test = new Video("idvideo1.mov", "caption1");
        Video video2Test = new Video("idvideo2.mov", "caption2");
        Video video3Test = new Video("idvideo3.mov", "caption3");

        ArticleReference reference = new ArticleReference(articleId, articleName, referenceImageUrl, videoUrls);
        CompletableFuture<Image> failedFuture = getFailedCompletableFuture();
        Future referenceFuture = getMockedFutureWithReturn(reference);
        Future videoFuture = getMockedFutureWithReturn(video1Test, video2Test, video3Test);

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(referenceFuture);
        when(assetsServiceClient.getImageById(anyString())).thenReturn(failedFuture);
        when(assetsServiceClient.getVideoById(anyString())).thenReturn(videoFuture);

        Future<RichArticle> futureResult = enricher.enrichArticleWithId(reference.getId());
        RichArticle richArticle = futureResult.get();

        assertThat(richArticle.getId(), is(articleId));
        assertThat(richArticle.getName(), is(articleName));
        assertThat(richArticle.getHeroImage(), is(nullValue()));
        assertThat(richArticle.getVideos(), hasSize(3));
    }

    @Test
    public void when_articleIdAndAssetsSClientFails_expect_richArticleWithoutImageAndVideos() throws Exception {
        String articleId = "articleId";
        String articleName = "articleName";

        ArticleReference reference = new ArticleReference(articleId, articleName, referenceImageUrl, videoUrls);
        Future referenceFuture = getMockedFutureWithReturn(reference);
        CompletableFuture failedFuture = getFailedCompletableFuture();

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(referenceFuture);
        when(assetsServiceClient.getImageById(anyString())).thenReturn(failedFuture);
        when(assetsServiceClient.getVideoById(anyString())).thenReturn(failedFuture);

        Future<RichArticle> futureResult = enricher.enrichArticleWithId(reference.getId());
        RichArticle richArticle = futureResult.get();

        assertThat(richArticle.getId(), is(articleId));
        assertThat(richArticle.getName(), is(articleName));
        assertThat(richArticle.getHeroImage(), is(nullValue()));
        assertThat(richArticle.getVideos(), hasSize(0));
    }

    @Test
    public void when_numServiceCallshigherThanNumProcessors_expect_collectionVideos() throws Exception {
        int totalVideosUrls = numberOfProcessors * 2;

        List<String> listVideoUrls = new ArrayList();
        String videoId;
        for (int i = 0; i < totalVideosUrls; i++) {
            videoId = "video"+i+".mov";
            listVideoUrls.add("http://www.videourl.com/today/"+videoId);
            when(assetsServiceClient.getVideoById(videoId))
                    .thenReturn(getDelayedFutureSuccess(new Video(videoId, "caption"), randomBetweenRange(500,1000), TimeUnit.MICROSECONDS));
        }

        Future<List<Video>> futureResult = enricher.getVideoFuturesFromUrls(listVideoUrls);
        assertThat(futureResult.get(), hasSize(totalVideosUrls));
    }

    @Test
    public void when_numServiceCallshigherThanNumProcessors_expect_enrichArticle() throws Exception {
        String articleId = "articleId";
        String articleName = "articleName";
        Image imageTest = new Image("idimage1.jpg", "alt");
        List<String> listVideoUrls = new ArrayList();
        ArticleReference reference = new ArticleReference(articleId, articleName, referenceImageUrl, listVideoUrls);
        int totalVideosUrls = numberOfProcessors * 2;

        String videoId;
        for (int i = 0; i < totalVideosUrls; i++) {
            videoId = "video"+i+".mov";
            listVideoUrls.add("http://www.videourl.com/today/"+videoId);
            when(assetsServiceClient.getVideoById(videoId))
                    .thenReturn(getDelayedFutureSuccess(new Video(videoId, "caption"), randomBetweenRange(500,1000), TimeUnit.MICROSECONDS));
        }

        Future referenceFuture = getMockedFutureWithReturn(reference);
        Future imageFuture = getMockedFutureWithReturn(imageTest);

        when(repositoryClient.getArticleReferenceForId(anyString())).thenReturn(referenceFuture);
        when(assetsServiceClient.getImageById(anyString())).thenReturn(imageFuture);

        Future<RichArticle> futureResult = enricher.enrichArticleWithId(reference.getId());
        RichArticle richArticle = futureResult.get();

        assertThat(richArticle.getId(), is(articleId));
        assertThat(richArticle.getName(), is(articleName));
        assertThat(richArticle.getHeroImage(), is(imageTest));
        assertThat(richArticle.getVideos(), hasSize(totalVideosUrls));
    }

    private <T> Future<T> getMockedFutureWithReturn(T dataToReturn) throws Exception {
        Future future = mock(Future.class);
        when(future.get()).thenReturn(dataToReturn);
        return future;
    }

    private <T> Future<T> getMockedFutureWithReturn(T dataToReturn1, T... dataToReturn2) throws Exception {
        Future future = mock(Future.class);
        when(future.get()).thenReturn(dataToReturn1, dataToReturn2);
        return future;
    }

    private CompletableFuture getFailedCompletableFuture() {
        CompletableFuture future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("HTTP call failed!"));
        return future;
    }

    private <T> CompletableFuture<T> getDelayedFutureSuccess(T value, int delay, TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<T>();
        ScheduledFuture<Boolean> task = scheduler.schedule(() -> future.complete(value), delay, unit);
        future.whenComplete((t, ex) -> {
            if (future.isCancelled())
                task.cancel(true);
        });
        return future;
    }

    private int randomBetweenRange(int low, int hign) {
        return rnd.nextInt(hign-low) + low;
    }
}
