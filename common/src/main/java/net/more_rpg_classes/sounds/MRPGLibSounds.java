package net.more_rpg_classes.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;

public class MRPGLibSounds {
    public static final class Entry {
        private final Identifier id;
        private final SoundEvent soundEvent;
        private RegistryEntry<SoundEvent> entry;
        private int variants = 1;

        public Entry(Identifier id, SoundEvent soundEvent) {
            this.id = id;
            this.soundEvent = soundEvent;
        }

        public Entry(String name) {
            this(new Identifier(MOD_ID, name));
        }

        public Entry(Identifier id) {
            this(id, SoundEvent.of(id));
        }

        public Entry travelDistance(float distance) {
            return new Entry(id, SoundEvent.of(id, distance));
        }

        public Entry variants(int variants) {
            this.variants = variants;
            return this;
        }

        public Identifier id() { return id; }
        public SoundEvent soundEvent() { return soundEvent; }
        public RegistryEntry<SoundEvent> entry() { return entry; }
        public int variants() { return variants; }
    }

    public static final List<Entry> entries = new ArrayList<>();

    public static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }

    public static final Entry ICICLE_CRASH = add(new Entry("icicle_crash"));
    public static final Entry STRONG_ATTACK = add(new Entry("strong_attack"));
    public static final Entry CRIPPLING_STRIKE = add(new Entry("crippling_strike"));
    public static final Entry WATER_MAGIC_IMPACT_1 = add(new Entry("water_magic_impact1"));
    public static final Entry WATER_BUBBLES = add(new Entry("water_bubbles")).variants(3);
    public static final Entry WATER_BUBBLE_EXPLODE = add(new Entry("water_bubble_explode"));
    public static final Entry WATER_RELEASE_1 = add(new Entry("water_release_1"));
    public static final Entry WATER_RELEASE_2 = add(new Entry("water_release_2"));
    public static final Entry WATER_WAVE_RELEASE_1 = add(new Entry("water_wave_release_1"));
    public static final Entry EARTH_MAGIC_IMPACT_1 = add(new Entry("earth_magic_impact1"));
    public static final Entry EARTH_MAGIC_IMPACT_2 = add(new Entry("earth_magic_impact2"));
    public static final Entry EARTH_MAGIC_IMPACT_3 = add(new Entry("earth_magic_impact3"));
    public static final Entry AIR_MAGIC_IMPACT_1 = add(new Entry("air_magic_impact1"));
    public static final Entry AIR_MAGIC_IMPACT_2 = add(new Entry("air_magic_impact2"));
    public static final Entry AIR_MAGIC_IMPACT_3 = add(new Entry("air_magic_impact3"));
    public static final Entry AIR_MAGIC_CAST_1 = add(new Entry("air_magic_cast1"));
    public static final Entry AIR_CHANNEL_CAST_1 = add(new Entry("air_magic_channel_1"));
    public static final Entry EARTH_MAGIC_CAST_1 = add(new Entry("earth_magic_cast1"));
    public static final Entry AIR_EXPLOSION = add(new Entry("air_explosion"));
    public static final Entry FROST_CRACKLE = add(new Entry("frost_crackle"));
    public static final Entry ARCANE_STRONG_IMPACT = add(new Entry("arcane_strong_impact"));
    public static final Entry HOLY_RELEASE = add(new Entry("holy_release"));
    public static final Entry FROST_CRACKLE_LONG = add(new Entry("frost_crackle_long"));
    public static final Entry NATURE_RELEASE_1 = add(new Entry("nature_release_1"));
    public static final Entry NATURE_RELEASE_2 = add(new Entry("nature_release_2"));
    public static final Entry NATURE_IMPACT_1 = add(new Entry("nature_impact_1"));
    public static final Entry NATURE_IMPACT_2 = add(new Entry("nature_impact_2"));
    public static final Entry NATURE_IMPACT_3 = add(new Entry("nature_impact_3"));
    public static final Entry NATURE_IMPACT_4 = add(new Entry("nature_impact_4"));
    public static final Entry NATURE_CAST_1 = add(new Entry("nature_cast_1"));
    public static final Entry CARVE = add(new Entry("carve"));
    public static final Entry DECAPITATE_IMPACT = add(new Entry("decapitate_impact"));
    public static final Entry DECAPITATE_RELEASE = add(new Entry("decapitate_release"));
    public static final Entry DECAPITATE_SWING = add(new Entry("decapitate_swing"));
    public static final Entry FIST_ATTACK = add(new Entry("fist_attack"));
    public static final Entry PUNCTURE_CHARGE = add(new Entry("puncture_charge"));
    public static final Entry PUNCTURE_IMPACT = add(new Entry("puncture_impact"));
    public static final Entry STEALTH_VANISH = add(new Entry("stealth_vanish"));

    /// Creation only — the sound events keyed by the id they register under. Forge iterates this from its
    /// `SOUND_EVENT` `RegisterEvent` window, then calls {@link #linkEntries()}: the event's helper returns
    /// void where `Registry.registerReference` returns the {@link RegistryEntry} that {@link Entry#entry()}
    /// exposes. Skips ids already present, so it is idempotent.
    public static Map<Identifier, SoundEvent> soundsToRegister() {
        var toRegister = new LinkedHashMap<Identifier, SoundEvent>();
        for (var entry : entries) {
            if (entry.entry != null || Registries.SOUND_EVENT.containsId(entry.id())) { continue; }
            toRegister.put(entry.id(), entry.soundEvent());
        }
        return Collections.unmodifiableMap(toRegister);
    }

    /// Reads every `Entry#entry` back out of the registry. Call right after registering through a
    /// loader-specific helper; {@link #register()} already fills them in. Throws naming the id if a sound
    /// never reached the registry.
    public static void linkEntries() {
        for (var entry : entries) {
            if (entry.entry != null) { continue; }
            entry.entry = Registries.SOUND_EVENT.getEntry(RegistryKey.of(RegistryKeys.SOUND_EVENT, entry.id()))
                    .orElseThrow(() -> new IllegalStateException(
                            "Sound event " + entry.id() + " is not in the registry — register it first"));
        }
    }

    /// The vanilla registration path, used on Fabric.
    public static void register() {
        for (var entry : entries) {
            entry.entry = Registry.registerReference(Registries.SOUND_EVENT, entry.id(), entry.soundEvent());
        }
    }
}
