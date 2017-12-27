package com.fibanez.java8.clients;

import com.fibanez.java8.models.Image;
import com.fibanez.java8.models.Video;

import java.util.concurrent.Future;

public interface AssetsServiceClient {
    Future<Image> getImageById(String id);

    Future<Video> getVideoById(String id);
}
