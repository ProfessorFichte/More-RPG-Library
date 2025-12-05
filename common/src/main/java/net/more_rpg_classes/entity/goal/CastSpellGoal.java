package net.more_rpg_classes.entity.goal;

import net.more_rpg_classes.MRPGCMod;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.Identifier;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.TargetHelper;
import net.spell_power.api.SpellPower;

import java.util.EnumSet;
import java.util.List;

/**
 * Makes entities cast SpellEngine spells.
 * <p>Usage example:</p>
 * <pre>{@code
 * public class MySpellcaster extends ZombieEntity implements ISpellCasterEntity {
 *     // Implement interface methods...
 *
 *     @Override
 *     protected void initGoals() {
 *         this.goalSelector.add(1, new CastSpellGoal(this, Identifier.of("mymod", "fireball")));
 *         this.goalSelector.add(2, new CastSpellGoal(this, Identifier.of("mymod", "ice_lance")));
 *     }
 * }
 * }</pre>
 */
public class CastSpellGoal extends Goal {
    private final ISpellCasterEntity caster;
    private final Identifier spellId;
    private final List<CastSpellGoal> otherSpellGoals;

    private int spellCooldown = 0;
    private int castingTime = 0;
    private int totalCastTime = 0;
    private boolean isChanneling = false;
    private int channelingInterval = 0;
    private int nextChannelingImpact = 0;

    /**
     * @param caster The entity that will cast the spell (must implement ISpellCasterEntity)
     * @param spellId The identifier of the spell to cast
     */
    public CastSpellGoal(ISpellCasterEntity caster, Identifier spellId) {
        this(caster, spellId, null);
    }

    /**
     * Create a spell casting goal with awareness of other spell goals.
     * This prevents multiple spells from casting simultaneously.
     *
     * @param caster The entity that will cast the spell (must implement ISpellCasterEntity)
     * @param spellId The identifier of the spell to cast
     * @param otherSpellGoals List of other CastSpellGoals on this entity (for mutual exclusion)
     */
    public CastSpellGoal(ISpellCasterEntity caster, Identifier spellId, List<CastSpellGoal> otherSpellGoals) {
        this.caster = caster;
        this.spellId = spellId;
        this.otherSpellGoals = otherSpellGoals;
        this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
    }

    @Override
    public boolean canStart() {
        MobEntity entity = caster.asMobEntity();
        LivingEntity target = entity.getTarget();

        if (target == null || !target.isAlive()) {
            return false;
        }
        if (caster.isSpellcasting()) {
            return false;
        }
        if (spellCooldown > 0) {
            return false;
        }

        // Prevent starting if any other spell goal is currently active
        if (otherSpellGoals != null) {
            for (CastSpellGoal otherGoal : otherSpellGoals) {
                if (otherGoal != this && otherGoal.isActive()) {
                    return false;
                }
            }
        }

        double range = caster.getSpellRange(spellId);
        double distance = Math.sqrt(entity.squaredDistanceTo(target));

        return distance <= range;
    }

    @Override
    public boolean shouldContinue() {
        MobEntity entity = caster.asMobEntity();
        LivingEntity target = entity.getTarget();
        return target != null && target.isAlive() && castingTime > 0;
    }

    @Override
    public void start() {
        castingTime = caster.getCastTime(spellId);
        totalCastTime = castingTime;
        ISpellCasterEntity.SpellType spellType = caster.getSpellType(spellId);
        ISpellCasterEntity.CastingType castingType = caster.getCastingType(spellId);

        // Set up channeling if this is a channeling spell
        if (castingType == ISpellCasterEntity.CastingType.CHANNELING) {
            isChanneling = true;
            RegistryEntry<Spell> spellEntry = SpellRegistry.from(caster.asMobEntity().getWorld()).getEntry(spellId).orElse(null);
            if (spellEntry != null && spellEntry.value().active.cast != null) {
                int channelTicks = spellEntry.value().active.cast.channel_ticks;
                channelingInterval = (channelTicks > 0) ? channelTicks : 20;
            } else {
                channelingInterval = 20;
            }
            nextChannelingImpact = 0; // Fire first impact immediately
        } else {
            isChanneling = false;
        }

        caster.startSpellCast(castingTime);

        RegistryEntry<Spell> spellEntry = SpellRegistry.from(caster.asMobEntity().getWorld()).getEntry(spellId).orElse(null);
        if (spellEntry != null && spellEntry.value().active.cast != null) {
            ParticleHelper.sendBatches(caster.asMobEntity(), spellEntry.value().active.cast.particles);
        }
    }

    @Override
    public void tick() {
        MobEntity entity = caster.asMobEntity();
        LivingEntity target = entity.getTarget();

        if (target != null) {
            entity.getLookControl().lookAt(target, 30.0F, 30.0F);
        }

        castingTime--;

        if (castingTime % 5 == 0) {
        }

        if (isChanneling) {
            // CHANNELING: Spawn spell casting particles every tick
            RegistryEntry<Spell> spellEntry = SpellRegistry.from(entity.getWorld()).getEntry(spellId).orElse(null);
            if (spellEntry != null && spellEntry.value().active.cast != null) {
                ParticleHelper.sendBatches(entity, spellEntry.value().active.cast.particles);
            }

            // Fire impacts at regular intervals during cast time
            nextChannelingImpact--;
            if (nextChannelingImpact <= 0) {
                faceTarget(target);
                castSpell();
                nextChannelingImpact = channelingInterval;
            }
        } else {

            if (castingTime == totalCastTime / 2) {
                faceTarget(target);
                castSpell();
            }
        }
    }

    private void faceTarget(LivingEntity target) {
        MobEntity entity = caster.asMobEntity();
        if (target != null) {
            double deltaX = target.getX() - entity.getX();
            double deltaY = target.getEyeY() - entity.getEyeY();
            double deltaZ = target.getZ() - entity.getZ();
            double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

            float targetYaw = (float)(Math.atan2(deltaZ, deltaX) * (180.0 / Math.PI)) - 90.0F;
            float targetPitch = (float)(-(Math.atan2(deltaY, horizontalDistance) * (180.0 / Math.PI)));

            entity.setYaw(targetYaw);
            entity.setPitch(targetPitch);
            entity.headYaw = targetYaw;
            entity.bodyYaw = targetYaw;
        }
    }

    @Override
    public void stop() {
        caster.stopSpellCast();
        spellCooldown = caster.getSpellCooldown(spellId);

        castingTime = 0;
        totalCastTime = 0;
        isChanneling = false;
        channelingInterval = 0;
        nextChannelingImpact = 0;
    }

    private void castSpell() {
        MobEntity entity = caster.asMobEntity();
        if (entity.getWorld().isClient()) {
            return;
        }

        LivingEntity target = entity.getTarget();
        if (target == null) {
            return;
        }

        ISpellCasterEntity.SpellType spellType = caster.getSpellType(spellId);
        RegistryEntry<Spell> spellEntry = SpellRegistry.from(entity.getWorld()).getEntry(spellId).orElse(null);
        if (spellEntry == null) {
            MRPGCMod.LOGGER.warn("Spell not found: " + spellId);
            return;
        }

        try {
            switch (spellType) {
                case METEOR -> castMeteorSpell(target, spellEntry);
                case PROJECTILE -> castProjectileSpell(target, spellEntry);
                case CLOUD -> castCloudSpell(target, spellEntry);
                case DIRECTIMPACT -> castDirectImpactSpell(target, spellEntry);
                case AREAIMPACT -> castAreaImpactSpell(target, spellEntry);
            }

        } catch (Exception e) {
            MRPGCMod.LOGGER.error("Failed to cast spell " + spellId + " for spell caster", e);
        }
    }

    private void castMeteorSpell(LivingEntity target, RegistryEntry<Spell> spellEntry) {
        MobEntity entity = caster.asMobEntity();
        Spell spell = spellEntry.value();
        SpellPower.Result power = SpellPower.getSpellPower(caster.getSpellSchool(), entity);

        ISpellCasterEntity.PlacementTarget placementTarget = caster.getPlacementTarget(spellId);
        var position = (placementTarget == ISpellCasterEntity.PlacementTarget.TARGET) ?
            target.getPos() : entity.getPos();

        SpellHelper.ImpactContext context = new SpellHelper.ImpactContext()
                .power(SpellPower.getSpellPower(spell.school, entity))
                .position(position)
                .target(SpellTarget.FocusMode.AREA);
        ParticleHelper.sendBatches(entity, spell.release.particles);

        try {
            SpellHelper.fallProjectile(entity.getWorld(), entity, target, position, spellEntry, context);
        } catch (Exception e) {
            SpellHelper.performImpacts(
                    entity.getWorld(),
                    entity,
                    target,
                    entity,
                    spellEntry,
                    spell.impacts,
                    context
            );
        }
    }

    private void castProjectileSpell(LivingEntity target, RegistryEntry<Spell> spellEntry) {
        MobEntity entity = caster.asMobEntity();
        Spell spell = spellEntry.value();
        SpellPower.Result power = SpellPower.getSpellPower(caster.getSpellSchool(), entity);
        SpellHelper.ImpactContext context = new SpellHelper.ImpactContext()
                .power(power)
                .position(entity.getEyePos())
                .target(SpellHelper.focusMode(spell));

        ParticleHelper.sendBatches(entity, spell.release.particles);

        try {
            SpellHelper.shootProjectile(
                    entity.getWorld(),
                    entity,
                    target,
                    spellEntry,
                    context,
                    0
            );
        } catch (Exception e) {
            SpellHelper.performImpacts(
                    entity.getWorld(),
                    entity,
                    target,
                    entity,
                    spellEntry,
                    spell.impacts,
                    context
            );
        }
    }

    private void castDirectImpactSpell(LivingEntity target, RegistryEntry<Spell> spellEntry) {
        MobEntity entity = caster.asMobEntity();
        Spell spell = spellEntry.value();
        SpellPower.Result power = SpellPower.getSpellPower(caster.getSpellSchool(), entity);
        ParticleHelper.sendBatches(entity, spell.active.cast.particles);
        ParticleHelper.sendBatches(entity, spell.release.particles);
        SpellHelper.ImpactContext context = new SpellHelper.ImpactContext()
                .power(power)
                .position(entity.getEyePos())
                .target(SpellHelper.focusMode(spell));
        SpellHelper.performImpacts(
                entity.getWorld(),
                entity,
                target,
                entity,
                spellEntry,
                spell.impacts,
                context
        );
    }

    private void castAreaImpactSpell(LivingEntity target, RegistryEntry<Spell> spellEntry) {
        MobEntity entity = caster.asMobEntity();
        Spell spell = spellEntry.value();
        SpellPower.Result power = SpellPower.getSpellPower(caster.getSpellSchool(), entity);
        if (!entity.getWorld().isClient() && target != null){
            ParticleHelper.sendBatches(entity, spell.release.particles);
            ParticleHelper.sendBatches(entity, spell.release.particles_scaled_with_ranged);
            for(Entity targetEntity : TargetHelper.targetsFromArea(entity, spell.range, spell.target.area, e -> e != entity)) {
                SpellHelper.performImpacts(entity.getWorld(), entity, targetEntity, entity, spellEntry,
                        spell.impacts, new SpellHelper.ImpactContext().power(power).position(entity.getPos()));
                ParticleHelper.sendBatches(targetEntity, spell.impacts.get(0).particles);
            }
        }
    }

    private void castCloudSpell(LivingEntity target, RegistryEntry<Spell> spellEntry) {
        MobEntity entity = caster.asMobEntity();
        Spell spell = spellEntry.value();
        SpellPower.Result power = SpellPower.getSpellPower(caster.getSpellSchool(), entity);

        // Determine placement position based on configuration
        ISpellCasterEntity.PlacementTarget placementTarget = caster.getPlacementTarget(spellId);
        var position = (placementTarget == ISpellCasterEntity.PlacementTarget.TARGET) ?
            target.getPos() : entity.getPos();

        SpellHelper.ImpactContext context = new SpellHelper.ImpactContext()
                .power(power)
                .position(entity.getEyePos())
                .target(SpellTarget.FocusMode.AREA);

        ParticleHelper.sendBatches(entity, spell.release.particles);

        SpellHelper.placeCloud(entity.getWorld(), entity, target, position, spellEntry , context);
    }

    /**
     * Check if this goal is currently active (casting)
     */
    public boolean isActive() {
        return castingTime > 0;
    }

    /**
     * Called every tick to update cooldown
     */
    public void updateCooldown() {
        if (spellCooldown > 0) {
            spellCooldown--;
        }
    }
}
