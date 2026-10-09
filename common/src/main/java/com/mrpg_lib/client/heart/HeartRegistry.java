package com.mrpg_lib.client.heart;

import java.util.ArrayList;

/**
 * Credits to Oth3r (More Heart Types) for the implementation reference!
 * <a href="https://modrinth.com/mod/more-heart-types">...</a>
 */

public class HeartRegistry {
    private static final ArrayList<HeartSetting> heartRegistry = new ArrayList<>();

    public static void register(HeartSetting heartSetting) {
        if (heartSetting == null || heartSetting.getId() == null || heartSetting.getId().isEmpty()) {
            throw new IllegalArgumentException("HeartSetting and its ID must not be null or empty");
        }
        if (getHeartSetting(heartSetting.getId()) != null) {
            throw new IllegalArgumentException("HeartSetting with ID '" + heartSetting.getId() + "' is already registered");
        }
        heartRegistry.add(heartSetting);
    }

    public static HeartSetting getHeartSetting(String id) {
        return heartRegistry.stream().filter(heartSetting -> heartSetting.getId().equals(id)).findFirst().orElse(null);
    }

    public static ArrayList<HeartSetting> getHeartSettings() {
        return new ArrayList<>(heartRegistry);
    }
}
