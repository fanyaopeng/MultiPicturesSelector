package com.fan.library.info;

import java.util.List;

public class Folder {
    private List<ImageInfo> imageInfos;
    private String path;
    private ImageInfo preview;

    public Folder(String path, ImageInfo preview, List<ImageInfo> imageInfos) {
        this.imageInfos = imageInfos;
        this.path = path;
        this.preview = preview;
    }

    public List<ImageInfo> getImageInfos() {
        return imageInfos;
    }

    public String getPath() {
        return path;
    }

    public ImageInfo getPreview() {
        return preview;
    }
}
