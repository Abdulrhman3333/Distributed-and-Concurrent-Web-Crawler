package com.mycompany.finalproject405;

import java.io.Serializable;
import java.util.ArrayList;

public class TaskInfo implements Serializable {
    private ArrayList<String> seedUrls;
    private boolean allowDuplicates;
    private int maxLevels;

    public TaskInfo(ArrayList<String> seedUrls, boolean allowDuplicates, int maxLevels) {
        this.seedUrls = seedUrls;
        this.allowDuplicates = allowDuplicates;
        this.maxLevels = maxLevels;
    }

    public ArrayList<String> getSeedUrls() {
        return seedUrls;
    }

    public boolean isAllowDuplicates() {
        return allowDuplicates;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    @Override
    public String toString() {
        return "TaskInfo{" +
                "seedUrls=" + seedUrls +
                ", allowDuplicates=" + allowDuplicates +
                ", maxLevels=" + maxLevels +
                '}';
    }
}
