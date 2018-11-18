package com.fan.MultiImageSelector.utils;

public class Config {
    public int maxNum = 9;
    public int minMum = 0;
    public boolean isOpenClip = true;
    public boolean isOpenCamera = true;
    private static Config sConfig;
    private static Object lock = new Object();

    private Config() {

    }

    public static Config get() {
        if (sConfig == null) {
            synchronized (lock) {
                if (sConfig == null) {
                    sConfig = new Config();
                }
            }
        }
        return sConfig;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;

    }

    public void setMinMum(int minMum) {
        this.minMum = minMum;
    }

    public void setOpenClip(boolean openClip) {
        isOpenClip = openClip;
    }
}
