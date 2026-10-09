package com.mrpg_lib.client.heart;

import java.util.ArrayList;

/**
 * Credits to Oth3r (More Heart Types) for the implementation reference!
 * <a href="https://modrinth.com/mod/more-heart-types">...</a>
 */

public class HeartTypes {
    public static final String FATAL_POISON_ID = "fatal_poison";

    public static ArrayList<HeartSetting> getHeartTypes() {
        ArrayList<HeartSetting> heartSettings = new ArrayList<>();
        heartSettings.add(new HeartSetting.Builder(FATAL_POISON_ID).container(true).blinking().build());

        return heartSettings;
    }

}
