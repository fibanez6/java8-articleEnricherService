package com.fibanez.java8.models;

public class Video {
    private final String id;
    private final String caption;

    public Video(String id, String caption) {
        this.id = id;
        this.caption = caption;
    }

    public String getId() {
        return id;
    }

    public String getCaption() {
        return caption;
    }
}
