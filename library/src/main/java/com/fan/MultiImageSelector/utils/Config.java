package com.fan.MultiImageSelector.utils;

public class Config {
    public int maxNum = 9;
    public int minMum = 0;
    public boolean isOpenCamera = true;
    public float ratio;

    private static Config sConfig;
    private static Object lock = new Object();

    private Config() {

    }

    public void reset() {
        maxNum = 0;
        minMum = 0;
        ratio = 0;
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

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;

    }

    public void setMinMum(int minMum) {
        this.minMum = minMum;
    }

}
