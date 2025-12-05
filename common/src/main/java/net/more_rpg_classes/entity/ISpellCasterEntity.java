package net.more_rpg_classes.entity;

import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.util.Identifier;
import net.spell_power.api.SpellSchool;

/**
 * Interface for "MobEntity"-classes that can cast spells created with Spell Engine.
 * Implement this interface on your mob entity to use CastSpellGoal.
 */
public interface ISpellCasterEntity {

    /**Defining the "delivery"-type of the spell.*/
    enum SpellType {
        METEOR,
        PROJECTILE,
        DIRECTIMPACT,
        CLOUD,
        AREAIMPACT
    }

    /**
     * Defining if the Spell Delivery
     */
    enum CastingType {
        CASTING,
        CHANNELING
    }

    /**
     * Enum defining where cloud/meteor spells are placed
     */
    enum PlacementTarget {
        /** Place at caster's position */
        CASTER,
        /** Place at target's position */
        TARGET
    }

    /**
     * Get the spell type (how the spell is delivered: projectile, meteor, impact, cloud, area impact).
     * This determines which casting method will be used.
     */
    SpellType getSpellType(Identifier spellId);

    /**
     * Get the casting type (when impacts are delivered: casting or channeling).
     * CASTING: Impact delivered once at halfway point.
     * CHANNELING: Impacts delivered repeatedly at intervals.
     */
    CastingType getCastingType(Identifier spellId);

    /**
     * Get the placement target for cloud/meteor spells.
     * Only applies to CLOUD and METEOR spell types.
     * CASTER: Place at caster's position.
     * TARGET: Place at target's position.
     */
    PlacementTarget getPlacementTarget(Identifier spellId);

    /**
     * Get the range for this spell in blocks.
     * The goal will only attempt to cast if target is within range.
     */
    double getSpellRange(Identifier spellId);

    /**
     * Get the cast time for this spell in ticks.
     * This is how long the casting animation and process takes.
     */
    int getCastTime(Identifier spellId);

    /**
     * Get the cooldown for this spell in ticks.
     * After casting, the spell cannot be cast again until cooldown expires.
     */
    int getSpellCooldown(Identifier spellId);

    /**
     * Get the particle effect to display during casting.
     * These particles spawn around the caster during the cast animation.
     */
    ParticleEffect getCastingParticle();

    /**
     * Get the spell school for this caster.
     * Determines which spell power attribute is used for damage calculation.
     */
    SpellSchool getSpellSchool();

    /**
     * Start the spell casting animation.
     * This should trigger visual effects (like raised arms for evokers).
     * @param ticks Duration of the casting animation in ticks
     */
    void startSpellCast(int ticks);

    /**
     * Stop the spell casting animation.
     * Called when casting is complete or interrupted.
     */
    void stopSpellCast();

    /**
     * Check if this entity is currently casting a spell.
     * Used to prevent multiple spells from casting simultaneously.
     * @return true if currently in casting animation
     */
    boolean isSpellcasting();

    /**
     * Get the entity as a MobEntity for goal operations.
     * @return The entity instance
     */
    MobEntity asMobEntity();
}
