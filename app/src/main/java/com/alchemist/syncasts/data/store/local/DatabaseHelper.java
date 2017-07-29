package com.alchemist.syncasts.data.store.local;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.Podcast;
import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.schedulers.Schedulers;
import timber.log.Timber;

import static com.alchemist.syncasts.data.store.local.Db.PodcastTable.COLUNM_FEED_LINK;

@Singleton
public class DatabaseHelper {

    private final BriteDatabase mDb;

    @Inject
    public DatabaseHelper(DbOpenHelper dbOpenHelper) {
        SqlBrite.Builder briteBuilder = new SqlBrite.Builder()
                .logger(message -> Timber.tag("Database").v(message));
        mDb = briteBuilder.build().wrapDatabaseHelper(dbOpenHelper, Schedulers.io());
        mDb.setLoggingEnabled(true);
    }

    public List<Episode> getEpisodes(Podcast podcast) {
        return getEpisodes(podcast, 0);
    }

    public List<Episode> getEpisodes(final Podcast podcast, int numEpisodes) {
        String query = "SELECT * FROM " + Db.EpisodesTable.TABLE_NAME + " WHERE "
                + Db.EpisodesTable.COLUMN_FK_PODCAST_ID + " = ? ORDER BY "
                + Db.EpisodesTable.COLUMN_PLAYED + ", "
                + Db.EpisodesTable.COLUMN_PUB_DATE + " DESC";
        if (numEpisodes > 0) {
            query += " LIMIT " + numEpisodes;
        }
        List<Episode> episodes = new ArrayList<>();
        try (Cursor cursor = mDb.query(query, String.valueOf(podcast.getId()))) {
            while (cursor.moveToNext()) {
                episodes.add(Db.EpisodesTable.parseCursor(cursor, podcast));
            }
            return episodes;
        }
    }

    public void updateEpisodes(List<Episode> episodes) {
        try (BriteDatabase.Transaction transaction = mDb.newTransaction()) {
            for (Episode episode : episodes) {
                updateEpisode(episode);
            }
            transaction.markSuccessful();
        }
    }

    public void updateEpisode(Episode episode) {
        mDb.update(Db.EpisodesTable.TABLE_NAME,
                Db.EpisodesTable.toContentValues(episode, episode.getPodcast().getId()),
                SQLiteDatabase.CONFLICT_REPLACE,
                Db.EpisodesTable.COLUMN_MEDIA_URL + " = ?;",
                episode.getMediaUrl());
    }

    public void insertEpisodes(List<Episode> episodes, long podcastId) {
        try (BriteDatabase.Transaction transaction = mDb.newTransaction()) {
            for (Episode episode : episodes) {
                insertEpisode(episode, podcastId);
            }
            transaction.markSuccessful();
        }
    }

    public void insertEpisode(Episode episode, long podcastId) {
        mDb.insert(Db.EpisodesTable.TABLE_NAME,
                Db.EpisodesTable.toContentValues(episode, podcastId),
                SQLiteDatabase.CONFLICT_IGNORE);
    }

    public void insertPodcastAndEpisodes(Podcast podcast) {
        try (BriteDatabase.Transaction transaction = mDb.newTransaction()) {
            long id = mDb.insert(Db.PodcastTable.TABLE_NAME,
                    Db.PodcastTable.toContentValues(podcast),
                    SQLiteDatabase.CONFLICT_IGNORE);
            insertEpisodes(podcast.getEpisodes(), id);
            transaction.markSuccessful();
        }
    }

    public Observable<List<Podcast>> getPodcasts() {
        String query = "SELECT * FROM " + Db.PodcastTable.TABLE_NAME;
        return mDb.createQuery(Db.PodcastTable.TABLE_NAME, query)
                .mapToList(Db.PodcastTable::parseCursor);
    }

    public Podcast getPodcast(String feedLink) {
        String query = "SELECT * FROM " + Db.PodcastTable.TABLE_NAME +
                " WHERE " + COLUNM_FEED_LINK + " = ?";
        try (Cursor cursor = mDb.query(query, feedLink)) {
            if (cursor.moveToFirst()) {
                return Db.PodcastTable.parseCursor(cursor);
            }
            return null;
        }
    }

    public int countUnplayedEpsiodes(Podcast podcast) {
        String query = "SELECT COUNT(*) FROM " + Db.EpisodesTable.TABLE_NAME + " WHERE "
                + Db.EpisodesTable.COLUMN_PLAYED + " = 0 AND "
                + Db.EpisodesTable.COLUMN_FK_PODCAST_ID + " = ?;";
        try (Cursor cursor = mDb.query(query, String.valueOf(podcast.getId()))) {
            if (cursor.moveToFirst()) {
                return cursor.getInt(0);
            }
            return 0;
        }
    }
}
