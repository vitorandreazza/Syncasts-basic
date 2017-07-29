package com.alchemist.syncasts.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.List;

public class Podcast implements Parcelable, Serializable {

    private long id;
    @NonNull private String feedUrl;
    private String author, title, imageLink;
    private transient List<Episode> episodes;

    public Podcast() {
    }

    private Podcast(Podcast.Builder builder) {
        id = builder.id;
        author = builder.author;
        title = builder.title;
        feedUrl = builder.feedUrl;
        imageLink = builder.imageLink;
        episodes = builder.episodes;
    }

    public long getId() {
        return id;
    }

    public String getAuthor() {
        return author;
    }

    public String getTitle() {
        return title;
    }

    public String getImageLink() {
        return imageLink;
    }

    @NonNull
    public String getFeedUrl() {
        return feedUrl;
    }

    public List<Episode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<Episode> mEpisodes) {
        this.episodes = mEpisodes;
    }

    @Override
    public String toString() {
        return "Podcast{" +
                "id='" + id + '\'' +
                ", feedUrl='" + feedUrl + '\'' +
                ", author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", imageLink='" + imageLink + '\'' +
                ", episodes=" + episodes +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.feedUrl);
        dest.writeString(this.author);
        dest.writeString(this.title);
        dest.writeString(this.imageLink);
}

    protected Podcast(Parcel in) {
        this.id = in.readLong();
        this.feedUrl = in.readString();
        this.author = in.readString();
        this.title = in.readString();
        this.imageLink = in.readString();
    }

    public static final Parcelable.Creator<Podcast> CREATOR = new Parcelable.Creator<Podcast>() {
        @Override
        public Podcast createFromParcel(Parcel source) {
            return new Podcast(source);
        }

        @Override
        public Podcast[] newArray(int size) {
            return new Podcast[size];
        }
    };

    public static class Builder {
        private long id;
        private String author, title, imageLink;
        @NonNull private String feedUrl;
        private List<Episode> episodes;

        public Builder(@NonNull String feedUrl) {
            this.feedUrl = feedUrl;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setAuthor(String author) {
            this.author = author;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setImageLink(String imageLink) {
            this.imageLink = imageLink;
            return this;
        }

        public Builder setEpisodes(List<Episode> episodes) {
            this.episodes = episodes;
            return this;
        }

        public Podcast build() {
            return new Podcast(this);
        }
    }
}
