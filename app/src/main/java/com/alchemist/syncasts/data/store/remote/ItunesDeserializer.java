package com.alchemist.syncasts.data.store.remote;

import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.ItunesResult;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItunesDeserializer implements JsonDeserializer<ItunesResult> {

    @Override
    public ItunesResult deserialize(JsonElement json,
                                    Type typeOfT,
                                    JsonDeserializationContext context) throws JsonParseException {
        List<ItunesPodcast> podcasts;

        JsonObject objJson = json.getAsJsonObject();
        if (objJson.has("results")) {
            //Itunes search deserialization
            podcasts = searchDeserialization(objJson);
        } else {
            //Itunes top podcasts deserialization
            podcasts = topPodcastsDeserialization(objJson);
        }
        return new ItunesResult(podcasts);
    }

    private List<ItunesPodcast> topPodcastsDeserialization(JsonObject objJson) {
        List<ItunesPodcast> podcasts = new ArrayList<>();
        JsonObject feed = objJson.getAsJsonObject("feed");
        JsonArray entrys = feed.getAsJsonArray("entry");
        String title, imgUrl, owner, summary = null;
        int id;
        for (JsonElement entry : entrys) {
            JsonObject jsonPodcast = entry.getAsJsonObject();
            title = jsonPodcast.get("title").getAsJsonObject().get("label").getAsString();
            owner = jsonPodcast.get("im:artist").getAsJsonObject().get("label").getAsString();
            JsonArray images = jsonPodcast.get("im:image").getAsJsonArray();
            imgUrl = null;
            for (JsonElement image : images) {
                JsonObject jsonImage = image.getAsJsonObject();
                int height = jsonImage.get("attributes")
                        .getAsJsonObject().get("height").getAsInt();
                if (height >= 100) {
                    imgUrl = jsonImage.get("label").getAsString();
                    break;
                }
            }
            id = jsonPodcast.get("id").getAsJsonObject().get("attributes").getAsJsonObject().get("im:id").getAsInt();
            JsonElement elemSummary = jsonPodcast.get("summary");
            if (elemSummary != null) {
                summary = elemSummary.getAsJsonObject().get("label").getAsString();
            }

            ItunesPodcast podcast = new ItunesPodcast(title, imgUrl, id, owner, summary);
            podcasts.add(podcast);
        }
        return podcasts;
    }

    private List<ItunesPodcast> searchDeserialization(JsonObject objJson) {
        List<ItunesPodcast> podcasts = new ArrayList<>();
        JsonArray results = objJson.getAsJsonArray("results");
        String title, imgUrl, feedUrl, owner;
        for (JsonElement result : results) {
            JsonObject jsonPodcast = result.getAsJsonObject();
            title = jsonPodcast.get("collectionName").getAsString();
            imgUrl = jsonPodcast.get("artworkUrl100").getAsString();
            feedUrl = jsonPodcast.get("feedUrl").getAsString();
            owner = jsonPodcast.get("artistName").getAsString();
            ItunesPodcast podcast = new ItunesPodcast(title, imgUrl, feedUrl, owner);
            podcasts.add(podcast);
        }
        return podcasts;
    }
}
