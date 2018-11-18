package com.fan.MultiImageSelector.info;

public class ImageInfo {
    private int date;
    private String path;


    public ImageInfo(int date, String path) {
        this.date = date;
        this.path = path;
    }

    public int getDate() {
        return date;
    }

    public String getPath() {
        return path;
    }

}
