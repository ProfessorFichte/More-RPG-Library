package com.mrpg_lib.compat.player_animator.client;

import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

@Environment(EnvType.CLIENT)
public class GearSpeedModifier extends SpeedModifier {
    public record Gear(float time, float speed) {
    }

    private float elapsed;
    private List<Gear> gears = List.of();

    public void set(float speed, List<Gear> gears) {
        this.speed = speed;
        this.gears = gears;
        this.elapsed = 0;
    }

    @Override
    public void tick() {
        super.tick();
        elapsed += 1;
    }

    @Override
    public void setupAnim(float tickDelta) {
        float time = elapsed + tickDelta;
        for (Gear gear : gears) {
            if (time > gear.time()) {
                speed = gear.speed();
            }
        }
        super.setupAnim(tickDelta);
    }
}
