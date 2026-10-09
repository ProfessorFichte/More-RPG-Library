package com.mrpg_lib.compat.spell_engine;

import org.jetbrains.annotations.Nullable;

public record SpellTimings(
        @Nullable Float castSeconds,
        @Nullable Float channelSeconds,
        @Nullable Float cooldownSeconds,
        @Nullable Integer channelTicks) {

    public static final SpellTimings NONE = new SpellTimings(null, null, null, null);

    public static final int DEFAULT_CAST_TICKS = 40;

    public static Builder builder() {
        return new Builder();
    }

    public static SpellTimings cast(float seconds) {
        return builder().castSeconds(seconds).build();
    }

    public static SpellTimings channel(float seconds) {
        return builder().channelSeconds(seconds).build();
    }

    public static SpellTimings cooldown(float seconds) {
        return builder().cooldownSeconds(seconds).build();
    }

    public static int resolveCastTicks(@Nullable SpellTimings perSpell, @Nullable SpellTimings goalWide,
                                       boolean channel, @Nullable Float spellCastSeconds) {
        Float override = castOverride(perSpell, channel);
        if (override == null) override = castOverride(goalWide, channel);
        if (override != null) return Math.max(1, Math.round(override * 20F));
        return spellCastSeconds != null ? Math.max(1, Math.round(spellCastSeconds * 20F)) : DEFAULT_CAST_TICKS;
    }

    public static int resolveCooldownTicks(@Nullable SpellTimings perSpell, @Nullable SpellTimings goalWide,
                                           float spellCooldownSeconds) {
        Float seconds = perSpell != null ? perSpell.cooldownSeconds : null;
        if (seconds == null && goalWide != null) seconds = goalWide.cooldownSeconds;
        float resolved = seconds != null ? seconds : spellCooldownSeconds;
        return resolved > 0 ? Math.round(resolved * 20F) : 0;
    }

    public static int resolveChannelReleases(@Nullable SpellTimings perSpell, @Nullable SpellTimings goalWide,
                                             int spellChannelTicks) {
        if (spellChannelTicks <= 0) return spellChannelTicks;
        Integer override = perSpell != null ? perSpell.channelTicks : null;
        if (override == null && goalWide != null) override = goalWide.channelTicks;
        return override != null ? Math.max(1, override) : spellChannelTicks;
    }

    @Nullable
    private static Float castOverride(@Nullable SpellTimings timings, boolean channel) {
        if (timings == null) return null;
        if (channel && timings.channelSeconds != null) return timings.channelSeconds;
        return timings.castSeconds;
    }

    public static final class Builder {
        private Float castSeconds;
        private Float channelSeconds;
        private Float cooldownSeconds;
        private Integer channelTicks;

        public Builder castSeconds(float seconds) {
            this.castSeconds = seconds;
            return this;
        }

        public Builder channelSeconds(float seconds) {
            this.channelSeconds = seconds;
            return this;
        }

        public Builder cooldownSeconds(float seconds) {
            this.cooldownSeconds = seconds;
            return this;
        }

        public Builder channelTicks(int releases) {
            this.channelTicks = releases;
            return this;
        }

        public SpellTimings build() {
            return new SpellTimings(castSeconds, channelSeconds, cooldownSeconds, channelTicks);
        }
    }
}
