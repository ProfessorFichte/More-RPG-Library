package com.mrpg_lib.compat.spell_engine;

import com.google.common.base.Suppliers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.MathHelper;
import net.spell_engine.Platform;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.internals.delivery.arrow.ArrowExtension;
import net.spell_engine.internals.delivery.arrow.ArrowHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class MobArrowContext {
    private static final Map<LivingEntity, State> STATES = new WeakHashMap<>();

    private record Timed(RegistryEntry<Spell> spell, long expiresAt) { }

    private static final class State {
        @Nullable Timed spellShot;
        final List<Timed> affectNextArrow = new ArrayList<>();
    }

    private MobArrowContext() {
    }

    public static void beginSpellShot(LivingEntity shooter, RegistryEntry<Spell> spell, int ticks) {
        STATES.computeIfAbsent(shooter, s -> new State()).spellShot = new Timed(spell, shooter.getWorld().getTime() + ticks);
    }

    public static void affectNextArrow(LivingEntity shooter, RegistryEntry<Spell> spell, int ticks) {
        STATES.computeIfAbsent(shooter, s -> new State()).affectNextArrow.add(new Timed(spell, shooter.getWorld().getTime() + ticks));
    }

    public static void onArrowCreated(LivingEntity shooter, @Nullable ProjectileEntity projectile, boolean viaShootAll) {
        if (shooter instanceof PlayerEntity || !(shooter.getWorld() instanceof ServerWorld world)) return;
        if (!(projectile instanceof PersistentProjectileEntity arrow) || !(arrow instanceof ArrowExtension extension)) return;

        long now = world.getTime();
        State state = STATES.get(shooter);
        RegistryEntry<Spell> spellShot = null;
        List<RegistryEntry<Spell>> affecting = List.of();
        if (state != null) {
            if (state.spellShot != null) {
                if (state.spellShot.expiresAt() >= now) {
                    spellShot = viaShootAll ? state.spellShot.spell() : null;
                } else {
                    state.spellShot = null;
                }
            }
            if (!state.affectNextArrow.isEmpty()) {
                affecting = state.affectNextArrow.stream().filter(t -> t.expiresAt() >= now).map(Timed::spell).toList();
                state.affectNextArrow.clear();
            }
            if (state.spellShot == null && state.affectNextArrow.isEmpty()) {
                STATES.remove(shooter);
            }
        }

        boolean firedBySpell = spellShot != null;
        if (firedBySpell && shooter instanceof MobEntity mob && mob.getTarget() != null) {
            aimAt(mob, mob.getTarget());
        }

        MobStashHelper.onMobArrowShot(shooter, arrow, firedBySpell);

        var trackers = Suppliers.memoize(() -> Platform.tracking(shooter));
        if (spellShot != null) {
            ArrowHelper.onArrowShot(extension, shooter, spellShot, trackers);
        }
        for (var spell : affecting) {
            ArrowHelper.onArrowShot(extension, shooter, spell, trackers);
        }
    }

    private static void aimAt(MobEntity mob, LivingEntity target) {
        double dx = target.getX() - mob.getX();
        double dz = target.getZ() - mob.getZ();
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double dy = target.getBodyY(0.5) - mob.getEyeY() + hDist * 0.05;
        float yaw = (float) (MathHelper.atan2(dz, dx) * (180.0 / Math.PI)) - 90.0F;
        float pitch = (float) (-(MathHelper.atan2(dy, hDist) * (180.0 / Math.PI)));
        mob.setYaw(yaw);
        mob.setPitch(pitch);
        mob.setHeadYaw(yaw);
        mob.setBodyYaw(yaw);
        mob.prevYaw = yaw;
        mob.prevPitch = pitch;
    }
}
