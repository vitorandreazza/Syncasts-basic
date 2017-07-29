package com.alchemist.syncasts.data.model;

public class BasicModel {

    private String title;
    private String subtitle;

    public BasicModel(String title, String subtitle) {
        this.title = title;
        this.subtitle = subtitle;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicModel that = (BasicModel) o;

        return title.equals(that.title) && subtitle.equals(that.subtitle);

    }

    @Override
    public int hashCode() {
        int result = title.hashCode();
        result = 31 * result + subtitle.hashCode();
        return result;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    @Override
    public String toString() {
        return "BasicModel{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                '}';
    }
}
