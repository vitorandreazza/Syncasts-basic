package com.alchemist.syncasts.data.model;

public class ItunesPodcast {

    private Integer id;
    private String subtitle, imgUrl, feedUrl, author, summary;

    public ItunesPodcast(String subtitle, String imgUrl, Integer id, String author,
                         String summary) {
        this.subtitle = subtitle;
        this.imgUrl = imgUrl;
        this.id = id;
        this.author = author;
        this.summary = summary;
    }

    public ItunesPodcast(String subtitle, String imgUrl, String feedUrl, String author) {
        this.subtitle = subtitle;
        this.imgUrl = imgUrl;
        this.feedUrl = feedUrl;
        this.author = author;
    }

    public Integer getId() {
        return id;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public String getAuthor() {
        return author;
    }

    public String getSummary() {
        return summary;
    }
}
