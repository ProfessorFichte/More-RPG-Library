package net.more_rpg_classes.sounds;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

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
            this(Identifier.of(MOD_ID, name));
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

    public static void register() {
        for (var entry : entries) {
            entry.entry = Registry.registerReference(Registries.SOUND_EVENT, entry.id(), entry.soundEvent());
        }
    }
}
