package com.alchemist.syncasts.data.store.local;

import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.Podcast;

import java.util.Date;

public final class Db {

    public abstract static class PodcastTable implements BaseColumns {

        public static final String TABLE_NAME = "podcasts";

        public static final String COLUNM_AUTHOR = "author";
        public static final String COLUNM_SUBTITLE = "subtitle";
        public static final String COLUNM_FEED_LINK = "feed_link";
        public static final String COLUNM_IMAGE_DIR = "image_dir";
        public static final String COLUNM_IMAGE_LINK = "image_link";

        public static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUNM_AUTHOR + " TEXT, " +
                        COLUNM_SUBTITLE + " TEXT, " +
                        COLUNM_FEED_LINK + " TEXT UNIQUE NOT NULL, " +
                        COLUNM_IMAGE_DIR + " TEXT, " +
                        COLUNM_IMAGE_LINK + " TEXT" +
                ");";

        public static ContentValues toContentValues(Podcast podcast) {
            ContentValues values = new ContentValues();
            values.put(COLUNM_FEED_LINK, podcast.getFeedUrl());
            if (podcast.getAuthor() != null) values.put(COLUNM_AUTHOR, podcast.getAuthor());
            if (podcast.getTitle() != null) values.put(COLUNM_SUBTITLE, podcast.getTitle());
            if (podcast.getImageLink() != null) {
                values.put(COLUNM_IMAGE_LINK, podcast.getImageLink());
            }
            return values;
        }

        public static Podcast parseCursor(Cursor cursor) {
            return new Podcast.Builder(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUNM_FEED_LINK)))
                    .setId(cursor.getLong(cursor.getColumnIndexOrThrow(_ID)))
                    .setAuthor(cursor.getString(cursor.getColumnIndexOrThrow(COLUNM_AUTHOR)))
                    .setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUNM_SUBTITLE)))
                    .setImageLink(cursor.getString(cursor.getColumnIndexOrThrow(COLUNM_IMAGE_LINK)))
                    .build();
        }
    }

    public abstract static class EpisodesTable implements BaseColumns {

        public static final String TABLE_NAME = "episodes";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PUB_DATE = "pub_date";
        public static final String COLUMN_DURATION = "duration";
        public static final String COLUMN_PROGRESS = "progress";
        public static final String COLUMN_PLAYED = "played";
        public static final String COLUMN_MEDIA_URL = "media_url";
        public static final String COLUMN_LOCAL_FILE_NAME = "local_file_name";
        public static final String COLUMN_FK_PODCAST_ID = "fk_podcast_id";

        public static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_TITLE + " TEXT, " +
                        COLUMN_DESCRIPTION + " TEXT, " +
                        COLUMN_PUB_DATE + " DATE, " +
                        COLUMN_DURATION + " INTEGER, " +
                        COLUMN_PROGRESS + " INTEGER, " +
                        COLUMN_PLAYED + " BOOLEAN NOT NULL CHECK (" + COLUMN_PLAYED + " IN (0,1)) DEFAULT 1, " +
                        COLUMN_MEDIA_URL + " TEXT NOT NULL UNIQUE, " +
                        COLUMN_LOCAL_FILE_NAME + " TEXT, " +
                        COLUMN_FK_PODCAST_ID + " INTEGER NOT NULL, " +
                        "FOREIGN KEY(" + COLUMN_FK_PODCAST_ID + ") REFERENCES " +
                        PodcastTable.TABLE_NAME + "(" + PodcastTable._ID +
                        "));";

        public static ContentValues toContentValues(Episode episode, long podcastId) {
            ContentValues values = new ContentValues();
            if (episode.getTitle() != null) values.put(COLUMN_TITLE, episode.getTitle());
            if (episode.getDuration() != null) values.put(COLUMN_DURATION, episode.getDuration());
            if (episode.getProgress() != null) values.put(COLUMN_PROGRESS, episode.getProgress());
            if (episode.getDescription() != null) {
                values.put(COLUMN_DESCRIPTION, episode.getDescription());
            }
            if (episode.getPubDate() != null) {
                values.put(COLUMN_PUB_DATE, episode.getPubDate().getTime());
            }
            if (episode.getLocalDir() != null) {
                values.put(COLUMN_LOCAL_FILE_NAME, episode.getLocalDir());
            }
            values.put(COLUMN_MEDIA_URL, episode.getMediaUrl());
            values.put(COLUMN_PLAYED, episode.isPlayed());
            values.put(COLUMN_FK_PODCAST_ID, podcastId);
            return values;
        }

        public static Episode parseCursor(Cursor cursor, Podcast podcast) {
            long id = cursor.getLong(cursor.getColumnIndex(_ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
            long dateLong = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_PUB_DATE));
            Integer duration = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_DURATION));
            Integer progress = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PROGRESS));
            String mediaUrl = cursor.getString(cursor.getColumnIndex(COLUMN_MEDIA_URL));
            String localFileName = cursor.getString(cursor.getColumnIndex(COLUMN_LOCAL_FILE_NAME));
            boolean played = (cursor.getInt(cursor.getColumnIndex(COLUMN_PLAYED)) == 1);
            return new Episode.Builder(podcast)
                    .setId(id)
                    .setTitle(title)
                    .setDescription(description)
                    .setPubDate(new Date(dateLong))
                    .setDuration(duration)
                    .setProgress(progress)
                    .setMediaUrl(mediaUrl)
                    .setPlayed(played)
                    .setLocalDir(localFileName)
                    .build();
        }
    }
}
