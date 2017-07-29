package com.alchemist.syncasts.data.model;

import java.util.ArrayList;
import java.util.List;

public class ItunesResult {

    private List<ItunesPodcast> results = new ArrayList<>();

    public ItunesResult(List<ItunesPodcast> results) {
        this.results = results;
    }

    public List<ItunesPodcast> getResults() {
        return results;
    }
}
