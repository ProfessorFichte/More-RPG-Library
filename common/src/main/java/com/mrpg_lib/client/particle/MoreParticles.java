package com.mrpg_lib.client.particle;

import com.mojang.serialization.MapCodec;
import com.mrpg_lib.MRPGCMod;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class MoreParticles {

    public static final SimpleParticleType RAINBOW_MUSIC_NOTE = new SimpleParticleType(false) {};
    public static final SimpleParticleType FADING_MOTE = new SimpleParticleType(false) {};
    public static ParticleType<PopupParticleEffect> POPUP;
    public static ParticleType<PopupParticleEffect> SPELL_STOLEN_POPUP;

    public static void register() {
        POPUP = Registry.register(
            Registries.PARTICLE_TYPE,
            Identifier.of(MRPGCMod.MOD_ID, "popup"),
            new ParticleType<PopupParticleEffect>(false) {
                @Override
                public MapCodec<PopupParticleEffect> getCodec() {
                    return PopupParticleEffect.createCodec(this);
                }
                @Override
                public PacketCodec<? super RegistryByteBuf, PopupParticleEffect> getPacketCodec() {
                    return PopupParticleEffect.createPacketCodec(this);
                }
            }
        );
        SPELL_STOLEN_POPUP = Registry.register(
                Registries.PARTICLE_TYPE,
                MRPGCMod.id("spell_stolen_popup"),
                new ParticleType<PopupParticleEffect>(false) {
                    @Override
                    public MapCodec<PopupParticleEffect> getCodec() {
                        return PopupParticleEffect.createCodec(this);
                    }
                    @Override
                    public PacketCodec<? super RegistryByteBuf, PopupParticleEffect> getPacketCodec() {
                        return PopupParticleEffect.createPacketCodec(this);
                    }
                }
        );
        Registry.register(Registries.PARTICLE_TYPE,
                Identifier.of(MRPGCMod.MOD_ID, "rainbow_music_note"), RAINBOW_MUSIC_NOTE);
        Registry.register(Registries.PARTICLE_TYPE, MRPGCMod.id("fading_mote"), FADING_MOTE);
    }
}
