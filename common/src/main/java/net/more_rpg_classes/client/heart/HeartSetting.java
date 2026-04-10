package net.more_rpg_classes.client.heart;

import net.minecraft.util.Identifier;
import net.more_rpg_classes.MRPGCMod;

/**
 * Credits to Oth3r (More Heart Types) for the implementation reference!
 * <a href="https://modrinth.com/mod/more-heart-types">...</a>
 */

public class HeartSetting {
    private final String id;
    private Boolean enabled;
    private Boolean normal;
    private Boolean hardcore;
    private Boolean container;
    private Boolean blinking;


    private HeartSetting(Builder builder) {
        this.id = builder.id;
        this.enabled = builder.enabled;
        this.normal = builder.normal;
        this.hardcore = builder.hardcore;
        this.container = builder.container;
        this.blinking = builder.blinking;

    }

    public static class Builder {
        private final String id;
        private boolean enabled = true;
        private Boolean normal = true;
        private Boolean hardcore = null;
        private Boolean container = null;
        private Boolean blinking = null;


        public Builder(String id) {
            this.id = id;
        }

        public Builder enabled(boolean val) { this.enabled = val; return this; }
        public Builder normal(Boolean val) { this.normal = val; return this; }
        public Builder hardcore(Boolean val) { this.hardcore = val; return this; }
        public Builder container(Boolean val) { this.container = val; return this; }
        public Builder blinking() { this.blinking = true; return this; }

        public HeartSetting build() {
            return new HeartSetting(this);
        }
    }

    public String getId() {
        return id;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public boolean setEnabled(Boolean enabled) {
        if (this.enabled == null) return false;
        this.enabled = enabled;
        return true;
    }

    public Boolean getNormal() {
        return normal != null ? normal : false;
    }

    public boolean setNormal(Boolean normal) {
        this.normal = normal;
        return normal != null;
    }

    public Boolean getHardcore() {
        return hardcore != null ? hardcore : false;
    }

    public boolean setHardcore(Boolean hardcore) {
        this.hardcore = hardcore;
        return hardcore != null;
    }

    public Boolean getContainer() {
        return container != null ? container : false;
    }

    public boolean setContainer(Boolean container) {
        this.container = container;
        return container != null;
    }

    public Boolean getBlinking() {
        return blinking != null ? blinking : false;
    }

    public boolean setBlinking(Boolean blinking) {
        this.blinking = blinking;
        return blinking != null;
    }

    public Identifier getIdentifier(boolean blinking, boolean half, boolean hardcore, boolean container) {
        // can't display if not enabled
        if (!this.enabled || (!getHardcore() && !getEnabled())) return null;
        String heartPath = "hud/heart/" + id;
        if (container) {
            if (this.container == null || !this.container) return null;
            // add the container suffix, and if enabled and needed, add the blinking suffix
            return Identifier.of(MRPGCMod.MOD_ID, heartPath+"_container" + (blinking ? "_blinking" : "")); // containers always blink
        }

        // add hardcore if enabled
        if (getHardcore() && hardcore) heartPath += "_hardcore";
        // add half or full based on the heart
        heartPath += (half ? "_half" : "_full");
        // add blinking if enabled and needed
        if (getBlinking() && blinking) {
            heartPath += "_blinking";
        }

        // display the heart
        return Identifier.of(MRPGCMod.MOD_ID, heartPath);
    }
}