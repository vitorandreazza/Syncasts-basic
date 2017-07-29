package com.alchemist.syncasts.data.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.Date;

public class Episode implements Parcelable, Serializable {
    private long id;
    private String title, description, localDir;
    private boolean played = true;
    private Date pubDate;
    private Integer duration, progress;
    @NonNull private String mediaUrl;
    @NonNull private Podcast podcast;

    public Episode() {
    }

    private Episode(Episode.Builder builder) {
        podcast = builder.podcast;
        id = builder.id;
        title = builder.title;
        description = builder.description;
        mediaUrl = builder.mediaUrl;
        played = builder.played;
        pubDate = builder.pubDate;
        duration = builder.duration;
        progress = builder.progress;
        localDir = builder.localDir;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    @NonNull
    public String getMediaUrl() {
        return mediaUrl;
    }

    public String getDescription() {
        return description;
    }

    public boolean isPlayed() {
        return played;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public Integer getDuration() {
        return duration;
    }

    public Integer getProgress() {
        return progress;
    }

    @NonNull
    public Podcast getPodcast() {
        return podcast;
    }

    public String getLocalDir() {
        return localDir;
    }

    public void setProgress(Integer progress) {
        this.progress = progress;
    }

    public void setLocalDir(String localDir) {
        this.localDir = localDir;
    }

    public void setPodcast(@NonNull Podcast podcast) {
        this.podcast = podcast;
    }

    public void setPlayed(boolean played) {
        this.played = played;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Episode episode = (Episode) o;

        return mediaUrl.equals(episode.mediaUrl);
    }

    @Override
    public int hashCode() {
        return mediaUrl.hashCode();
    }

    @Override
    public String toString() {
        return "Episode{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", mediaUrl=" + mediaUrl +
                ", played=" + played +
                ", pubDate=" + pubDate +
                ", duration=" + duration +
                ", progress=" + progress +
                ", localDir=" + localDir +
                ", podcast=" + podcast +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeString(this.title);
        dest.writeString(this.description);
        dest.writeString(this.localDir);
        dest.writeString(this.mediaUrl);
        dest.writeByte(this.played ? (byte) 1 : (byte) 0);
        dest.writeLong(this.pubDate != null ? this.pubDate.getTime() : -1);
        dest.writeValue(this.duration);
        dest.writeValue(this.progress);
        dest.writeParcelable(this.podcast, flags);
    }

    protected Episode(Parcel in) {
        this.id = in.readLong();
        this.title = in.readString();
        this.description = in.readString();
        this.localDir = in.readString();
        this.mediaUrl = in.readString();
        this.played = in.readByte() != 0;
        long tmpPubDate = in.readLong();
        this.pubDate = tmpPubDate == -1 ? null : new Date(tmpPubDate);
        this.duration = (Integer) in.readValue(Integer.class.getClassLoader());
        this.progress = (Integer) in.readValue(Integer.class.getClassLoader());
        this.podcast = in.readParcelable(Podcast.class.getClassLoader());
    }

    public static final Creator<Episode> CREATOR = new Creator<Episode>() {
        @Override
        public Episode createFromParcel(Parcel source) {
            return new Episode(source);
        }

        @Override
        public Episode[] newArray(int size) {
            return new Episode[size];
        }
    };

    public static class Builder {
        private long id;
        private String title, description, localDir;
        private boolean played = true;
        private Date pubDate;
        private Integer duration, progress;
        @NonNull private String mediaUrl;
        @NonNull private Podcast podcast;

        public Builder(@NonNull Podcast podcast) {
            this.podcast = podcast;
        }

        public Builder setId(long id) {
            this.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        public Builder setDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder setMediaUrl(@NonNull String mediaUrl) {
            this.mediaUrl = mediaUrl;
            return this;
        }

        public Builder setPlayed(boolean played) {
            this.played = played;
            return this;
        }

        public Builder setPubDate(Date pubDate) {
            this.pubDate = pubDate;
            return this;
        }

        public Builder setDuration(Integer duration) {
            this.duration = duration;
            return this;
        }

        public Builder setProgress(Integer progress) {
            this.progress = progress;
            return this;
        }

        public Builder setLocalDir(String localDir) {
            this.localDir = localDir;
            return this;
        }

        public Episode build() {
            return new Episode(this);
        }
    }
}
