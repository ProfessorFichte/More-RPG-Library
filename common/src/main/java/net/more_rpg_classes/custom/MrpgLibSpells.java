package net.more_rpg_classes.custom;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;
import net.more_rpg_classes.effect.MRPGCEffects;
import net.more_rpg_classes.sounds.MRPGLibSounds;
import net.spell_engine.api.datagen.SpellBuilder;
import net.spell_engine.api.entity.SpellEntityPredicates;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.fx.PlayerAnimation;
import net.spell_engine.api.spell.fx.Sound;
import net.spell_engine.api.util.TriState;
import net.spell_engine.client.gui.SpellTooltip;
import net.spell_engine.client.util.Color;
import net.spell_engine.fx.SpellEngineParticles;
import net.spell_engine.fx.SpellEngineSounds;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_power.api.SpellSchools;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static net.more_rpg_classes.MRPGCMod.MOD_ID;
import static net.more_rpg_classes.custom.SpellBuilderHelper.*;

public class MrpgLibSpells {
    public enum Category {
        MELEE, RANGED, SPELL, HEAL, SHIELD
    }
    public record Entry(Identifier id, Spell spell, String title, String description,
                        @Nullable SpellTooltip.DescriptionMutator mutator, 
                        @Nullable Category categories) {
        
        public Entry(Identifier id, Spell spell, String title, String description) {
            this(id, spell, title, description, null,null);
        }
        public Entry mutator(SpellTooltip.DescriptionMutator mutator) {
            return new Entry(id, spell, title, description, mutator,categories);
        }
        public Entry categories(Category  categories) {
            return new Entry(id, spell, title, description, mutator,categories);
        }
    }


    public static final List<Entry> entries = new ArrayList<>();

    private static Entry add(Entry entry) {
        entries.add(entry);
        return entry;
    }
    /// WEAPON SKILLS
    public static Entry decapitate = add(decapitate());
    private static Entry decapitate() {
        var id = Identifier.of(MOD_ID, "decapitate");
        var title = "Decapitate";
        var description = "Delivers a heavy blow with forward momentum that disables shield and item usage of target.";
        var spell = SpellBuilder.createSpellActive();
        spell.tier = 1;
        spell.school = MoreSpellSchools.RAGE_MELEE;
        spell.range = 0.0F;
        spell.range_mechanic = Spell.RangeMechanic.MELEE;

        SpellBuilder.Casting.cast(spell, 0.75F);
        spell.active.cast.animation = PlayerAnimation.of("more_rpg_classes:decapitate_charge");
        spell.active.cast.animation.speed = 1.5F;
        spell.active.cast.animation_pitch = false;
        spell.release.sound = new Sound(MRPGLibSounds.DECAPITATE_RELEASE.id());

        SpellBuilder.Target.none(spell);

        var cut_1 = new Spell.Delivery.Melee.Attack();
        cut_1.attack_speed_multiplier = 1.25F;
        cut_1.delay = 0.1F;
        cut_1.hitbox = new Spell.Delivery.Melee.HitBox();
        cut_1.hitbox.arc = 180;
        cut_1.hitbox.height = 0.7F;
        cut_1.hitbox.roll = 15F;
        cut_1.damage_bonus = 0.25F;
        cut_1.forward_momentum = 1.5F;
        cut_1.swing_sound = new Sound(MRPGLibSounds.DECAPITATE_SWING.id());
        cut_1.impact_sound = new Sound(MRPGLibSounds.DECAPITATE_IMPACT.id());
        cut_1.impact_sound_cap = 1;
        cut_1.animation = PlayerAnimation.of("more_rpg_classes:decapitate_release");

        SpellBuilder.Deliver.melee(spell, List.of(cut_1));

        var disrupt = SpellBuilder.Impacts.disrupt(true, 2F);
        spell.impacts = List.of(disrupt);

        SpellBuilder.Cost.cooldownGroupWeapon(spell);
        SpellBuilder.Cost.cooldown(spell, 15);
        spell.cost.cooldown.attempt_duration = 1F;

        return new Entry(id, spell, title, description);
    }
    public static final Entry burstcrack = add(burstcrack());
    private static Entry burstcrack() {
        var id = Identifier.of(MOD_ID, "burstcrack");
        var title = "Burstcrack";
        var description = "Unleashes a shockwave around you, knocking up enemies and dealing physical {damage_1} & {damage_2} arcane-damage.";
        var spell = SpellBuilder.createWeaponSpell();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.range = 5.5F;

        spell.active.cast.movement_speed = 0.25F;
        spell.active.cast.duration = 0.5F;
        spell.active.cast.animation = PlayerAnimation.of("more_rpg_classes:burstcrack_cast");

        spell.release.animation = PlayerAnimation.of("more_rpg_classes:burstcrack_release");
        spell.release.sound = Sound.withVolume(Identifier.of("entity.generic.explode"), 0.4F);
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.smoke_medium.id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        50, 0.2F, 0.3F)
        };
        spell.release.particles_scaled_with_ranged = new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.area_effect_658.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.GROUND,
                        1, 0, 0)
                        .scale(0.5F)
                        .color(ORANGE_COLOR.toRGBA())
        };

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.angle_degrees = 360;

        var damage = SpellBuilder.Impacts.damage(0.5F, 0F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(SpellEngineParticles.smoke_medium.id().toString(),
                        ParticleBatch.Shape.PIPE, ParticleBatch.Origin.CENTER,
                        20, 0.1F, 3.0F)
        };
        damage.sound = new Sound(MRPGLibSounds.FIST_ATTACK.id().toString());

        var custom = new Spell.Impact();
        custom.target_modifiers = List.of(
                SpellBuilderHelper.targetModifier("#c:bosses",TriState.DENY)
        );
        custom.action = new Spell.Impact.Action();
        custom.action.custom = new Spell.Impact.Action.Custom();
        custom.action.type = Spell.Impact.Action.Type.CUSTOM;
        custom.action.custom.intent = SpellTarget.Intent.HARMFUL;
        custom.action.custom.handler = "more_rpg_classes:knock_up";

        spell.impacts = List.of(damage, custom);
        SpellBuilder.Cost.cooldown(spell, 15);
        spell.cost.cooldown.attempt_duration = 1.5F;

        return new Entry(id, spell, title, description);
    }
    public static Entry puncture = add(puncture());
    private static Entry puncture() {
        var id = Identifier.of(MOD_ID, "puncture");
        var title = "Puncture";
        var description = "Charges in a designated direction, striking all enemies.";
        var spell = SpellBuilder.createMeleeSpell();

        SpellBuilder.Casting.cast(spell, 0.7F, "more_rpg_classes:puncture_charge");
        spell.active.cast.movement_speed = 0.0F;

        SpellBuilder.Target.none(spell);

        var attack = new Spell.Delivery.Melee.Attack();
        attack.attack_speed_multiplier = 1F;
        attack.hitbox = new Spell.Delivery.Melee.HitBox();
        attack.hitbox.arc = 80;
        attack.hitbox.length = 1.0F;
        attack.hitbox.height = 0.2F;
        attack.forward_momentum = 2.0F;
        attack.movement_slipperiness = 0.4F;
        attack.delay = 0.1F;
        attack.damage_bonus = 0.2F;
        attack.additional_strikes = 5;
        attack.additional_strike_delay = 0.15F;
        attack.additional_hits_on_same_target = false;
        attack.animation =  PlayerAnimation.of("more_rpg_classes:puncture_release");
        attack.animation.speed = 1F;
        attack.swing_sound = Sound.of(MRPGLibSounds.PUNCTURE_CHARGE.id());
        attack.impact_sound = Sound.of(MRPGLibSounds.PUNCTURE_IMPACT.id());

        SpellBuilder.Deliver.melee(spell, List.of(attack));
        spell.deliver.melee.allow_airborne = false;

        spell.impacts = List.of();

        SpellBuilder.Cost.cooldown(spell, 10);

        return new Entry(id, spell, title, description);
    }
    ///PASSIVES
    //MELEE
    public static Entry carve_melee = add(carve_melee());
    private static Entry carve_melee() {
        var id = Identifier.of(MOD_ID, "carve");
        var title = "Carve";
        var description = "On melee hit: {trigger_chance} chance to stack armor reduction by {bonus2} and increasing incoming damage by {bonus} for {effect_amplifier_cap} times.";
        var spell = SpellBuilder.createSpellPassive();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        var debuffEffect = MRPGCEffects.CARVE;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = debuffEffect.config().attributes().get(1);
            var modifier2 = debuffEffect.config().attributes().get(0);
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            var bonus2 = SpellTooltip.bonus(modifier2.value, modifier2.operation);
            return args.description()
                    .replace("{bonus}", bonus)
                    .replace("{bonus2}", bonus2);
        };

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        trigger.chance = 0.2F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var debuff = SpellBuilder.Impacts.effectAdd(debuffEffect.id.toString(), 7,1,5);
        debuff.action.status_effect.apply_mode = Spell.Impact.Action.StatusEffect.ApplyMode.ADD;
        debuff.action.status_effect.show_particles = false;
        debuff.action.status_effect.amplifier = 1;
        debuff.action.status_effect.amplifier_cap = 5;
        debuff.action.status_effect.duration = 8;
        debuff.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.2F, 0.25F)
                        .color(Color.RAGE.toRGBA())
        };
        debuff.sound = new Sound(MRPGLibSounds.CARVE.id().toString());
        spell.impacts = List.of(debuff);

        SpellBuilder.Cost.cooldown(spell, 3);
        spell.cost.batching = true;

        return new Entry(id, spell, title, description).mutator(mutator).categories(Category.MELEE);
    }
    public static Entry lightning_strike_melee = add(lightning_strike_melee());
    private static Entry lightning_strike_melee() {
        var id = Identifier.of(MOD_ID, "lightning_strike_melee");
        var title = "Lightning Strike";
        var description = "On melee hit: {trigger_chance} chance to spawn a lightning strike.";
        var spell = SpellBuilder.createSpellPassive();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        trigger.chance = 0.15F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var custom = new Spell.Impact();
        custom.action = new Spell.Impact.Action();
        custom.action.custom = new Spell.Impact.Action.Custom();
        custom.action.type = Spell.Impact.Action.Type.CUSTOM;
        custom.action.custom.intent = SpellTarget.Intent.HARMFUL;
        custom.action.custom.handler = "more_rpg_classes:lightning";
        spell.impacts = List.of(custom);

        SpellBuilder.Cost.cooldown(spell, 5);
        spell.cost.batching = true;

        return new Entry(id, spell, title, description).categories(Category.MELEE);
    }

    public static Entry dragonclaw_melee = add(dragonclaw_melee());
    private static Entry dragonclaw_melee() {
        var id = Identifier.of(MOD_ID, "dragonclaw_melee");
        var title = "Dragonclaw";
        var description = "On melee hit: {trigger_chance} chance to deal extra {damage} to the target damage and heals the user for {heal} hearts.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.ARCANE;
        spell.tier = 8;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.chance = 0.35F;
        trigger.chance_batching = true;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var damage = SpellBuilder.Impacts.damage(0.6F, 0F);
        damage.attribute = EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString();
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "spell_engine:magic_arcane_impact_burst",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        30, 0.2F, 0.7F),
                new ParticleBatch(
                        "more_rpg_classes:dragon_claw",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        2, 0.2F, 0.5F)
        };

        var heal = SpellBuilder.Impacts.heal(0.025F);
        heal.attribute_from_target = true;
        heal.attribute = EntityAttributes.GENERIC_MAX_HEALTH.getIdAsString();
        heal.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.ARCANE,
                                SpellEngineParticles.MagicParticles.Motion.ASCEND
                        ).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        1, 0.05F, 0.1F).color(Color.ARCANE.toRGBA())
        };

        spell.impacts = List.of(damage, heal);
        SpellBuilder.Cost.cooldown(spell, 5.0F);

        return new Entry(id, spell, title, description);
    }

    public static Entry waterbomb_melee = add(waterbomb_melee());
    private static Entry waterbomb_melee() {
        var id = Identifier.of(MOD_ID, "waterbomb_melee");
        var title = "Waterbomb";
        var description = "On melee hit: {trigger_chance} chance to deal {damage} damage around the target.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = MoreSpellSchools.WATER;
        spell.range = 7.5F;
        spell.tier = 8;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        trigger.chance = 0.4F;
        spell.passive.triggers = List.of(trigger);

        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:big_splash",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        30, 0.5F, 0.75F),
                new ParticleBatch(
                        "more_rpg_classes:water_circle",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        1, 0.2F, 1.0F)
        };

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.distance_dropoff = Spell.Target.Area.DropoffCurve.NONE;
        spell.target.area.angle_degrees = 360.0F;
        spell.target.area.horizontal_range_multiplier = 1.0F;

        var damage = SpellBuilder.Impacts.damage( 0.4F,0);
        damage.attribute = EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString();
        damage.sound = Sound.withVolume(Identifier.of("more_rpg_classes:water_magic_impact1"), 0.4F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:big_splash",
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        20, 0.05F, 0.2F),
                new ParticleBatch(
                        "more_rpg_classes:splash",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        15, 0.05F, 0.2F),
                new ParticleBatch(
                        "more_rpg_classes:splash",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 1.0F, 1.2F)
        };

        spell.impacts = List.of(damage);
        SpellBuilder.Cost.cooldown(spell, 5.0F);

        return new Entry(id, spell, title, description);
    }

    public static Entry wither_pulse_melee = add(wither_pulse_melee());
    private static Entry wither_pulse_melee() {
        var id = Identifier.of(MOD_ID, "wither_pulse_melee");
        var title = "Wither Pulse";
        var description = "On melee hit: {trigger_chance} inflicts targets in a 90 degree radius with Wither and dealing {damage} damage.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.SOUL;
        spell.range = 5.0F;
        spell.tier = 8;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.chance = 0.3F;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.angle_degrees = 90.0F;
        spell.target.area.horizontal_range_multiplier = 1.0F;

        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "smoke",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        15, 0.05F, 0.5F)
        };

        var damage = SpellBuilder.Impacts.damage(0.5F,0);
        damage.attribute = EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString();
        damage.sound = new Sound(Identifier.of("spell_engine:generic_soul_impact"));
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.2F, 0.5F)
                        .color(858993663)
        };

        var witherEffect = SpellBuilder.Impacts.effectSet("wither",8.0F,1);
        witherEffect.action.status_effect.amplifier_cap = 10;
        witherEffect.action.status_effect.amplifier_power_multiplier = 0.15F;
        witherEffect.action.status_effect.show_particles = true;

        spell.impacts = List.of(damage, witherEffect);
        SpellBuilder.Cost.cooldown(spell, 10.0F);

        return new Entry(id, spell, title, description);
    }
    public static Entry avalanche_melee = add(avalanche_melee());
    private static Entry avalanche_melee() {
        var id = Identifier.of(MOD_ID, "avalanche_melee");
        var title = "Avalanche";
        var description = "On melee hit: {trigger_chance} chance to spawn a small avalanche, dealing {damage} damage and inflicting freezing for {effect_duration}.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.FROST;
        spell.tier = 8;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.MELEE_IMPACT;
        trigger.chance = 0.4F;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.METEOR;
        spell.deliver.meteor = new Spell.Delivery.Meteor();
        spell.deliver.meteor.launch_height = 4.0F;
        spell.deliver.meteor.launch_radius = 2.0F;
        spell.deliver.meteor.launch_properties.velocity = 0.5F;
        spell.deliver.meteor.launch_properties.extra_launch_count = 3;
        spell.deliver.meteor.launch_properties.extra_launch_delay = 2;
        var projectile = new Spell.ProjectileData();
        projectile.client_data = new Spell.ProjectileData.Client();
        projectile.client_data.light_level = 12;
        projectile.client_data.travel_particles = new ParticleBatch[]{
                new ParticleBatch(
                        "snowflake",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        3, 0.0F, 0.1F)
                        .rotate(ParticleBatch.Rotation.LOOK)
        };
        projectile.client_data.model = new Spell.ProjectileModel();
        projectile.client_data.model.model_id = "more_rpg_classes:spell_projectile/small_avalanche";
        spell.deliver.meteor.projectile = projectile;

        var freezingEffect = SpellBuilder.Impacts.effectAdd("more_rpg_classes:frosted", 10.0F,1,3);
        freezingEffect.action.status_effect.refresh_duration = false;
        freezingEffect.action.status_effect.show_particles = false;
        freezingEffect.target_modifiers = List.of(
                SpellBuilderHelper.targetModifier("#minecraft:freeze_immune_entity_types", TriState.DENY)
        );
        var damage = SpellBuilder.Impacts.damage(0.3F);
        damage.attribute = EntityAttributes.GENERIC_ATTACK_DAMAGE.getIdAsString();
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15, 0.2F, 0.4F).color(Color.FROST.toRGBA())
        };
        damage.sound = new Sound(Identifier.of("spell_engine", "generic_frost_impact"));

        spell.impacts = List.of(freezingEffect, damage);

        spell.area_impact = new Spell.AreaImpact();
        spell.area_impact.radius = 2.0F;
        spell.area_impact.area = new Spell.Target.Area();
        spell.area_impact.area.distance_dropoff = Spell.Target.Area.DropoffCurve.SQUARED;
        spell.area_impact.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "snowflake",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        10, 0.5F, 2.0F)
        };

        SpellBuilder.Cost.cooldown(spell, 8.0F);

        return new Entry(id, spell, title, description);
    }
    public static Entry duelists_focus = add(duelists_focus());
    private static Entry duelists_focus() {
        var id = Identifier.of(MOD_ID, "duelists_focus");
        var title = "Duelist's Focus";
        var description = "On melee hit: {trigger_chance} to mark the target, all other entities deal 25%% reduced damage on you and only you deal 25%% increased damage on your marked target.";
        var spell = SpellBuilder.createSpellPassive();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var triggers =  SpellBuilder.Triggers.withConditionMustWield(
                List.of(SpellBuilder.Triggers.meleeAttackImpact())
        );
        triggers.forEach(trigger -> {
            trigger.chance_batching = true;
            trigger.chance = 0.1F;
        });
        spell.passive.triggers = triggers;

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var focusTarget = SpellBuilder.Impacts.effectSet(MRPGCEffects.DUELISTS_FOCUS_TARGET.id.toString(), 5,0);
        var focusOwner = SpellBuilder.Impacts.effectSet(MRPGCEffects.DUELISTS_FOCUS_OWNER.id.toString(), 5,0);
        focusOwner.action.apply_to_caster = true;
        spell.impacts = List.of(focusTarget,focusOwner);

        SpellBuilder.Cost.cooldown(spell, 20.0F);
        spell.cost.batching = true;

        return new Entry(id, spell, title, description, null, Category.MELEE);
    }
    //RANGED
    public static Entry lightning_strike_ranged = add(lightning_strike_ranged());
    private static Entry lightning_strike_ranged() {
        var id = Identifier.of(MOD_ID, "lightning_strike_ranged");
        var title = "Lightning Shot";
        var description = "On arrow hit: {trigger_chance} chance to spawn a lightning strike.";
        var spell = SpellBuilder.createSpellPassive();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;

        var trigger = SpellBuilder.Triggers.arrowHit();
        trigger.chance = 0.15F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var custom = new Spell.Impact();
        custom.action = new Spell.Impact.Action();
        custom.action.custom = new Spell.Impact.Action.Custom();
        custom.action.type = Spell.Impact.Action.Type.CUSTOM;
        custom.action.custom.intent = SpellTarget.Intent.HARMFUL;
        custom.action.custom.handler = "more_rpg_classes:lightning";
        spell.impacts = List.of(custom);

        SpellBuilder.Cost.cooldown(spell, 5);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description).categories(Category.RANGED);
    }
    public static Entry dragon_breath_ranged = add(dragon_breath_ranged());
    private static Entry dragon_breath_ranged() {
        var id = Identifier.of(MOD_ID, "dragon_breath");
        var title = "Ender Dragon's Breath";
        var description = "On arrow hit: {trigger_chance} chance to create a dragon breath cloud, dealing {damage} damage per second.";

        var spell = new Spell();
        spell.school = SpellSchools.ARCANE;
        spell.range = 0.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        trigger.chance = 0.3F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.CLOUD;
        spell.deliver.delay = 5;

        var cloud = new Spell.Delivery.Cloud();
        cloud.volume.radius = 5.0F;
        cloud.volume.area = new Spell.Target.Area();
        cloud.volume.area.vertical_range_multiplier = 0.5F;
        cloud.volume.sound = new Sound(Identifier.of("entity.ender_dragon.ambient"));
        cloud.time_to_live_seconds = 4.0F;
        cloud.impact_tick_interval = 8;

        cloud.client_data = new Spell.Delivery.Cloud.ClientData();
        cloud.client_data.light_level = 15;
        cloud.client_data.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "dragon_breath",
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        20.0F, 0.0F, 0.0F
                ),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.ARCANE,
                                SpellEngineParticles.MagicParticles.Motion.ASCEND).id().toString(),
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        10.0F, 0.1F, 0.5F
                ).color(Color.ARCANE.toRGBA())
        };

        cloud.spawn = new Spell.Delivery.Cloud.Spawn();
        cloud.spawn.sound = new Sound(Identifier.of("entity.ender_dragon.shoot"));

        spell.deliver.clouds = List.of(cloud);

        var damage = SpellBuilder.Impacts.damage(0.5F);
        damage.attribute = "ranged_weapon:damage";
        damage.action.damage.knockback = 0.5F;
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        20.0F, 0.05F, 0.15F
                ).color(Color.ARCANE.toRGBA())
        };

        spell.impacts = List.of(damage);

        SpellBuilder.Cost.cooldown(spell, 8.0F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry reef_arrows = add(reef_arrows());
    private static Entry reef_arrows() {
        var id = Identifier.of(MOD_ID, "reef_arrows");
        var title = "Coral Reef Arrows";
        var description = "On arrow hit: {trigger_chance} chance to inflict bleeding for {effect_duration} seconds and dealing additional {damage} damage.";

        var spell = new Spell();
        spell.school = MoreSpellSchools.WATER;
        spell.range = 0.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        trigger.chance = 0.5F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var bleedingEffect = SpellBuilder.Impacts.effectSet("more_rpg_classes:bleeding", 5.0F, 0);
        bleedingEffect.attribute = "ranged_weapon:damage";
        bleedingEffect.target_modifiers = List.of(
                SpellBuilderHelper.targetModifier("#minecraft:undead", TriState.DENY)
        );
        bleedingEffect.action.status_effect.amplifier_power_multiplier = 0.2F;
        bleedingEffect.action.status_effect.show_particles = false;
        bleedingEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.dripping_blood.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10.0F, 0.05F, 0.3F
                )
        };

        var damage = SpellBuilder.Impacts.damage(0.5F);
        damage.attribute = "ranged_weapon:damage";
        damage.action.damage.knockback = 0.5F;
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:big_splash",
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        20.0F, 0.05F, 0.2F
                ),
                new ParticleBatch(
                        "more_rpg_classes:splash",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        15.0F, 0.05F, 0.2F
                )
        };
        damage.sound = Sound.withVolume(Identifier.of("more_rpg_classes:water_magic_impact1"), 0.4F);

        spell.impacts = List.of(bleedingEffect, damage);

        SpellBuilder.Cost.cooldown(spell, 4.0F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry glacial_splitter = add(glacial_splitter());
    private static Entry glacial_splitter() {
        var id = Identifier.of(MOD_ID, "glacial_splitter");
        var title = "Glacial Splitter";
        var description = "Defeating Enemies spawns glacial projectiles that deal {damage} damage and freeze enemies for {effect_duration} seconds.";

        var spell = new Spell();
        spell.school = SpellSchools.FROST;
        spell.range = 32.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        var targetCondition = new Spell.TargetCondition();
        targetCondition.health_percent_below = 0.0F;
        trigger.target_conditions = List.of(targetCondition);
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = "DAMAGE";
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.PROJECTILE;

        spell.deliver.projectile = new Spell.Delivery.ShootProjectile();
        var centerOffset = new Spell.Delivery.ShootProjectile.DirectionOffset();
        var offset1 = new Spell.Delivery.ShootProjectile.DirectionOffset();
        offset1.yaw = 20.0F;
        var offset2 = new Spell.Delivery.ShootProjectile.DirectionOffset();
        offset2.yaw = 35.0F;
        var offset3 = new Spell.Delivery.ShootProjectile.DirectionOffset();
        offset3.yaw = -20.0F;
        var offset4 = new Spell.Delivery.ShootProjectile.DirectionOffset();
        offset4.yaw = -35.0F;
        spell.deliver.projectile.direction_offsets = new Spell.Delivery.ShootProjectile.DirectionOffset[]{
                centerOffset,
                offset1,
                offset2,
                offset3,
                offset4
        };
        spell.deliver.projectile.direct_towards_target = true;
        spell.deliver.projectile.launch_properties.velocity = 1.2F;
        spell.deliver.projectile.launch_properties.extra_launch_count = 4;
        spell.deliver.projectile.launch_properties.extra_launch_delay = 0;
        spell.deliver.projectile.launch_properties.sound = Sound.withVolume(
                Identifier.of("spell_engine:generic_frost_impact"), 0.6F
        );

        spell.deliver.projectile.projectile = new Spell.ProjectileData();
        spell.deliver.projectile.projectile.homing_angle = 0.0F;

        spell.deliver.projectile.projectile.client_data = new Spell.ProjectileData.Client();
        spell.deliver.projectile.projectile.client_data.model = new Spell.ProjectileModel();
        spell.deliver.projectile.projectile.client_data.model.model_id = "more_rpg_classes:spell_projectile/glacial_arrow";
        spell.deliver.projectile.projectile.client_data.model.scale = 2.0F;
        spell.deliver.projectile.projectile.client_data.model.rotate_degrees_per_tick = 0.0F;

        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25.0F, 0.2F, 0.7F
                ).color(Color.FROST.toRGBA())
        };
        spell.release.sound = new Sound(Identifier.of("spell_engine:generic_frost_impact"));

        var damage = SpellBuilder.Impacts.damage(0.35F);
        damage.attribute = "ranged_weapon:damage";
        damage.action.damage.knockback = 0.5F;
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25.0F, 0.2F, 0.7F
                ).color(Color.FROST.toRGBA())
        };
        damage.sound = new Sound(Identifier.of("spell_engine:generic_frost_impact"));

        var frostedEffect = SpellBuilder.Impacts.effectSet("more_rpg_classes:frosted", 5.0F, 0);
        frostedEffect.target_modifiers = List.of(
                SpellBuilderHelper.targetModifier("#minecraft:freeze_immune_entity_types", TriState.DENY)
        );
        frostedEffect.action.status_effect.show_particles = false;
        frostedEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        5.0F, 0.1F, 0.35F
                ).color(Color.FROST.toRGBA())
        };

        spell.impacts = List.of(damage, frostedEffect);

        SpellBuilder.Cost.cooldown(spell, 4.0F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    public static Entry cursed_wither_bolt = add(cursed_wither_bolt());
    private static Entry cursed_wither_bolt() {
        var id = Identifier.of(MOD_ID, "cursed_wither_bolt");
        var title = "Cursed Wither Bolts";
        var description = "On arrow hit: {trigger_chance} chance to inflict wither's curse for {effect_duration} seconds and dealing additional {damage} damage.";

        var spell = new Spell();
        spell.school = SpellSchools.SOUL;
        spell.range = 0.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.ARROW_IMPACT;
        trigger.chance = 0.35F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var witherEffect = SpellBuilder.Impacts.effectSet("more_rpg_classes:withers_curse", 10.0F, 0);
        witherEffect.action.status_effect.show_particles = true;
        witherEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        15.0F, 0.2F, 0.25F
                ).color(858993663L),
                new ParticleBatch(
                        "sculk_soul",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        20.0F, 0.3F, 0.4F
                )
        };
        witherEffect.sound = new Sound(Identifier.of("entity.wither.ambient"));

        var damage = SpellBuilder.Impacts.damage(0.2F);
        damage.attribute = "ranged_weapon:damage";

        spell.impacts = List.of(witherEffect, damage);

        SpellBuilder.Cost.cooldown(spell, 6.0F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    //SHIELD
    public static Entry elder_guardian_shield = add(elder_guardian_shield());
    private static Entry elder_guardian_shield() {
        var id = Identifier.of(MOD_ID, "elder_guardian_shield");
        var title = "Elder Guardian Shield";
        var description = "On shield block: {trigger_chance} chance to inflict bleeding for {effect_duration} seconds and deal {damage} damage.";

        var spell = new Spell();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.range = 0.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SHIELD_BLOCK;
        trigger.chance = 0.5F;
        spell.passive.triggers = List.of(trigger);


        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var bleedingEffect = SpellBuilder.Impacts.effectSet("more_rpg_classes:bleeding",7,0);
        bleedingEffect.target_modifiers = List.of(
                SpellBuilderHelper.targetModifier("#minecraft:undead", TriState.DENY)
        );
        bleedingEffect.action.status_effect.amplifier_power_multiplier = 0.3F;
        bleedingEffect.action.status_effect.show_particles = false;
        bleedingEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "spell_engine:dripping_blood",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10.0F, 0.05F, 0.3F
                )
        };

        var damage = SpellBuilder.Impacts.damage(0.2F,0.25F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPARK,
                                SpellEngineParticles.MagicParticles.Motion.FLOAT).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10.0F, 0.3F, 0.35F
                ).color(3217014783L)
        };

        spell.impacts = List.of(bleedingEffect, damage);

        SpellBuilder.Cost.cooldown(spell,10F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry glacial_shield = add(glacial_shield());
    private static Entry glacial_shield() {
        var id = Identifier.of(MOD_ID, "glacial_shield");
        var title = "Glacial Shield";
        var description = "On shield block: {trigger_chance} chance to freeze nearby enemies for {effect_duration} seconds.";

        var spell = new Spell();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.range = 2.5F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SHIELD_BLOCK;
        trigger.chance = 0.2F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.vertical_range_multiplier = 1.0F;

        spell.release.sound = new Sound(Identifier.of("spell_engine:generic_frost_release"));
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.snowflake.id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        60.0F, 0.1F, 0.3F
                ),
                new ParticleBatch(
                        SpellEngineParticles.frost_shard.id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        60.0F, 0.3F, 0.6F
                )
        };
        var rangedParticle = new ParticleBatch(
                SpellEngineParticles.area_effect_293.id().toString(),
                ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.GROUND,
                1.0F, 0.0F, 0.0F
        ).color(2582052863L);
        rangedParticle.scale = 0.4F;
        spell.release.particles_scaled_with_ranged = new ParticleBatch[]{
                rangedParticle
        };

        var frozenEffect = SpellBuilder.Impacts.effectSet("more_rpg_classes:frozen_solid",3,0);
        frozenEffect.action.status_effect.show_particles = false;
        frozenEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.snowflake.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25.0F, 0.1F, 0.4F
                ),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.FROST,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        30.0F, 0.2F, 0.7F
                )
        };
        frozenEffect.sound = new Sound(Identifier.of("spell_engine:generic_frost_impact"));

        spell.impacts = List.of(frozenEffect);

        SpellBuilder.Cost.cooldown(spell,10F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry wither_shield = add(wither_shield());
    private static Entry wither_shield() {
        var id = Identifier.of(MOD_ID, "wither_shield");
        var title = "Wither Shield";
        var description = "On shield block: {trigger_chance} chance to shoot 3 wither skulls dealing {damage} damage and inflicting wither.";

        var spell = new Spell();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.range = 20.0F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SHIELD_BLOCK;
        trigger.chance = 0.3F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.AIM;
        spell.target.aim = new Spell.Target.Aim();

        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "smoke",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        40.0F, 0.6F, 0.8F
                )
        };

        spell.deliver = new Spell.Delivery();
        spell.deliver.type = Spell.Delivery.Type.PROJECTILE;

        spell.deliver.projectile = new Spell.Delivery.ShootProjectile();
        var centerOffset = new Spell.Delivery.ShootProjectile.DirectionOffset();
        var rightOffset = new Spell.Delivery.ShootProjectile.DirectionOffset();
        rightOffset.yaw = 30.0F;
        var leftOffset = new Spell.Delivery.ShootProjectile.DirectionOffset();
        leftOffset.yaw = -30.0F;
        spell.deliver.projectile.direction_offsets = new Spell.Delivery.ShootProjectile.DirectionOffset[]{
                centerOffset,
                rightOffset,
                leftOffset
        };
        spell.deliver.projectile.direct_towards_target = true;
        spell.deliver.projectile.launch_properties.velocity = 1.0F;
        spell.deliver.projectile.launch_properties.extra_launch_count = 2;
        spell.deliver.projectile.launch_properties.extra_launch_delay = 10;

        spell.deliver.projectile.projectile = new Spell.ProjectileData();
        spell.deliver.projectile.projectile.divergence = 5.0F;
        spell.deliver.projectile.projectile.client_data = new Spell.ProjectileData.Client();
        var travelParticle = new ParticleBatch(
                "smoke",
                ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                2.0F, 0.6F, 0.9F
        );
        travelParticle.rotation = ParticleBatch.Rotation.LOOK;
        spell.deliver.projectile.projectile.client_data.travel_particles = new ParticleBatch[]{
                travelParticle
        };
        spell.deliver.projectile.projectile.client_data.model = new Spell.ProjectileModel();
        spell.deliver.projectile.projectile.client_data.model.model_id = "more_rpg_classes:spell_projectile/wither_skull";
        spell.deliver.projectile.projectile.client_data.model.scale = 1.5F;
        spell.deliver.projectile.projectile.client_data.model.rotate_degrees_per_tick = 0.0F;

        var witherEffect = SpellBuilder.Impacts.effectSet("wither",5,1);
        witherEffect.action.status_effect.amplifier_power_multiplier = 0.25F;
        witherEffect.action.status_effect.show_particles = false;
        witherEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25.0F, 0.2F, 0.25F
                ).color(858993663L)
        };

        var damage = SpellBuilder.Impacts.damage(0.1F,0.25F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "smoke",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10.0F, 0.3F, 0.35F
                )
        };
        damage.sound = new Sound(Identifier.of("entity.generic.explode"));

        spell.impacts = List.of(witherEffect, damage);

        SpellBuilder.Cost.cooldown(spell,10F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry ender_dragon_shield = add(ender_dragon_shield());
    private static Entry ender_dragon_shield() {
        var id = Identifier.of(MOD_ID, "ender_dragon_shield");
        var title = "Ender Dragon Shield";
        var description = "On shield block: {trigger_chance} chance to create an explosive burst dealing {damage} damage to nearby enemies.";

        var spell = new Spell();
        spell.school = ExternalSpellSchools.PHYSICAL_MELEE;
        spell.range = 3.5F;
        spell.tier = 8;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SHIELD_BLOCK;
        trigger.chance = 0.4F;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.vertical_range_multiplier = 1.0F;

        spell.release.sound = new Sound(Identifier.of("entity.generic.explode"));
        spell.release.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.ARCANE,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        130.0F, 0.2F, 1.5F
                ).color(Color.ARCANE.toRGBA()),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.STRIPE,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        130.0F, 0.8F, 1.9F
                ).color(Color.ARCANE.toRGBA())
        };

        var damage = SpellBuilder.Impacts.damage(0.4F,1.0F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.BURST).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        30.0F, 0.2F, 1.2F
                ).color(4284940287L)
        };

        spell.impacts = List.of(damage);

        SpellBuilder.Cost.cooldown(spell,10.0F);
        spell.cost.batching = true;
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    //HEALING
    public static Entry sirens_tears = add(sirens_tears());
    private static Entry sirens_tears() {
        var id = Identifier.of(MOD_ID, "sirens_tears");
        var title = "Siren's Tears";
        var description = "On healing: {trigger_chance} chance to remove a harmful effect and apply regeneration that scales with missing health for {effect_duration} seconds.";

        var spell = new Spell();
        spell.school = SpellSchools.HEALING;
        spell.range = 0.0F;
        spell.tier = 7;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        trigger.chance = 0.3F;
        trigger.impact = new Spell.Trigger.ImpactCondition();
        trigger.impact.impact_type = "HEAL";
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var removeEffects = SpellBuilder.Impacts.effectCleanse();
        removeEffects.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:water_circle",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        1.0F, 0.2F, 1.0F
                ),
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SPELL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE).id().toString(),
                        ParticleBatch.Shape.WIDE_PIPE, ParticleBatch.Origin.FEET,
                        15.0F, 0.3F, 0.3F
                ).color(4294954239L)
        };

        var regenEffect = SpellBuilder.Impacts.effectSet(MRPGCEffects.SIRENS_TEAR.id.toString(),6,0);
        regenEffect.action.status_effect.show_particles = false;

        spell.impacts = List.of(removeEffects, regenEffect);

        SpellBuilder.Cost.cooldown(spell,20);
        spell.cost.batching = true;

        return new Entry(id, spell, title, description);
    }
    public static Entry dragonslayers_fury = add(dragonslayers_fury());
    private static Entry dragonslayers_fury() {
        var threshold = 0.2F;
        var id = Identifier.of(MOD_ID, "dragonslayers_fury");
        var title = "Dragonslayer's Fury";
        var effect = MRPGCEffects.DRAGON_SLAYERS_FURY;
        var description = "Healing or buffing allies under {threshold} health, grants them increased critical damage by {bonus} for {effect_duration} seconds.";
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().firstModifier();
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            return args.description().replace("{threshold}", SpellTooltip.percent(threshold))
                    .replace("{bonus}", bonus);
        };

        var spell = new Spell();
        spell.school = SpellSchools.ARCANE;
        spell.range = 0.0F;
        spell.tier = 7;
        spell.type = Spell.Type.PASSIVE;
        spell.passive = new Spell.Passive();

        var triggerHeal = new Spell.Trigger();
        triggerHeal.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        triggerHeal.impact = new Spell.Trigger.ImpactCondition();
        triggerHeal.impact.impact_type = Spell.Impact.Action.Type.HEAL.toString();
        triggerHeal.target_conditions = List.of(SpellBuilderHelper.healthRangeCondition(threshold,0.01F));
        var triggerEffect= new Spell.Trigger();
        triggerEffect.type = Spell.Trigger.Type.SPELL_IMPACT_SPECIFIC;
        triggerEffect.impact = new Spell.Trigger.ImpactCondition();
        triggerEffect.impact.impact_type = Spell.Impact.Action.Type.STATUS_EFFECT.toString();
        triggerEffect.target_conditions = List.of(SpellBuilderHelper.healthRangeCondition(threshold,0.01F));
        spell.passive.triggers = List.of(triggerHeal,triggerEffect);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var buffEffect = SpellBuilder.Impacts.effectSet(MRPGCEffects.DRAGON_SLAYERS_FURY.id.toString(),8,0);
        buffEffect.action.status_effect.show_particles = false;
        buffEffect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.MagicParticles.get(
                                SpellEngineParticles.MagicParticles.Shape.SKULL,
                                SpellEngineParticles.MagicParticles.Motion.DECELERATE
                        ).id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        10, 0.5F, 0.8F).color(Color.ARCANE.toRGBA()).extent(0.25F)
        };

        spell.impacts = List.of(buffEffect);

        SpellBuilder.Cost.cooldown(spell,35);
        spell.cost.batching = true;

        return new Entry(id, spell, title, description).mutator(mutator);
    }
    //SPELL
    public static Entry arcane_precision = add(arcane_precision());
    private static Entry arcane_precision() {
        var id = Identifier.of(MOD_ID, "arcane_precision");
        var title = "Arcane Precision";
        var description = "On dealing a critical hit with an active spell, apply a stack of Arcane Precision to the target for {effect_duration} seconds.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.ARCANE;
        spell.tier = 8;

        var trigger = SpellBuilder.Triggers.activeSpellCrit();
        trigger.impact.impact_type = Spell.Impact.Action.Type.DAMAGE.toString();
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var effect = SpellBuilder.Impacts.effectAdd(MRPGCEffects.ARCANE_PRECISION.id.toString(), 10, 1, 9);
        effect.action.status_effect.refresh_duration = true;
        effect.action.status_effect.show_particles = false;
        effect.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "dragon_breath",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        40, 0.6F, 0.8F
                ),
                new ParticleBatch(
                        "spell_engine:magic_arcane_impact_burst",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.FEET,
                        10, 0.05F, 0.2F
                ).extent(2.0F).color(4284940287L)
        };

        spell.impacts = List.of(effect);
        SpellBuilder.Cost.cooldown(spell, 1);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }

    public static Entry pyromaniac = add(pyromaniac());
    private static Entry pyromaniac() {
        var id = Identifier.of(MOD_ID, "pyromaniac");
        var title = "Pyromaniac";
        var description = "When dealing spell damage to a burning target, reduce fire spell cooldowns and deal bonus damage.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.FIRE;
        spell.tier = 8;

        var trigger = SpellBuilder.Triggers.spellHit(0.2F, null);
        trigger.target_conditions = List.of(
                SpellBuilder.TargetConditions.ofPredicate(SpellEntityPredicates.IS_ON_FIRE)
        );
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var cooldownImpact = new Spell.Impact();
        cooldownImpact.action = new Spell.Impact.Action();
        cooldownImpact.action.type = Spell.Impact.Action.Type.COOLDOWN;
        cooldownImpact.action.cooldown = new Spell.Impact.Action.Cooldown();
        cooldownImpact.action.cooldown.actives = new Spell.Impact.Action.Cooldown.Modify();
        cooldownImpact.action.cooldown.actives.school = SpellSchools.FIRE.id.toString();
        cooldownImpact.action.cooldown.actives.duration_multiplier = 0.7F;
        cooldownImpact.action.apply_to_caster = true;
        cooldownImpact.particles = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.sign_hourglass.id().toString(),
                        ParticleBatch.Shape.LINE_VERTICAL, ParticleBatch.Origin.CENTER,
                        1, 0.75F, 0.75F
                ).scale(1.2F).color(4282850047L).followEntity(true)
        };

        var damage = SpellBuilder.Impacts.damage(0.3F);

        spell.impacts = List.of(cooldownImpact, damage);
        SpellBuilder.Cost.cooldown(spell, 10);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    public static Entry rimefrost = add(rimefrost());
    private static Entry rimefrost() {
        var id = Identifier.of(MOD_ID, "rimefrost");
        var title = "Rimefrost";
        var description = "When dealing damage with an active frost spell, spawn a freezing cloud at the target.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = SpellSchools.FROST;
        spell.tier = 8;

        var trigger = SpellBuilder.Triggers.activeSpellHit(0.3F, SpellSchools.FROST.id.toString());
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver.type = Spell.Delivery.Type.CLOUD;

        var cloud = new Spell.Delivery.Cloud();
        cloud.volume.radius = 3.0F;
        cloud.volume.area.vertical_range_multiplier = 0.5F;
        cloud.volume.sound = new Sound("more_rpg_classes:frost_crackle_long");
        cloud.time_to_live_seconds = 7.0F;
        cloud.impact_tick_interval = 5;
        cloud.client_data.light_level = 6;
        cloud.client_data.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:freezing_snowflake",
                        ParticleBatch.Shape.PILLAR, ParticleBatch.Origin.FEET,
                        10, 0.1F, 0.12F
                )
        };
        cloud.client_data.particle_spawn_interval = 12;
        cloud.spawn.sound = new Sound(SpellEngineSounds.GENERIC_FROST_CASTING.id().toString());

        spell.deliver.clouds = List.of(cloud);

        var denyModifier = new Spell.Impact.TargetModifier();
        var freezeImmuneCondition = new Spell.TargetCondition();
        freezeImmuneCondition.entity_type = "#minecraft:freeze_immune_entity_types";
        denyModifier.conditions = List.of(freezeImmuneCondition);
        denyModifier.execute = TriState.DENY;

        var freezing = SpellBuilder.Impacts.effectAdd("more_rpg_classes:frosted", 2, 1, 4);
        freezing.action.status_effect.refresh_duration = true;
        freezing.action.status_effect.show_particles = false;
        freezing.target_modifiers = List.of(denyModifier);
        freezing.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:freezing_snowflake",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        25, 0.2F, 0.25F
                )
        };

        spell.impacts = List.of(freezing);
        SpellBuilder.Cost.cooldown(spell, 4);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    public static Entry water_flow = add(water_flow());
    private static Entry water_flow() {
        var id = Identifier.of(MOD_ID, "water_flow");
        var title = "Water Flow";
        var description = "When casting a spell, deal water damage and heal nearby allies in a small area.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = MoreSpellSchools.WATER;
        spell.range = 5.0F;
        spell.tier = 8;

        var trigger = new Spell.Trigger();
        trigger.type = Spell.Trigger.Type.SPELL_CAST;
        trigger.chance = 0.25F;
        trigger.chance_batching = true;
        trigger.equipment_condition = EquipmentSlot.MAINHAND;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.AREA;
        spell.target.area = new Spell.Target.Area();
        spell.target.area.include_caster = true;
        spell.target.area.vertical_range_multiplier = 1.0F;

        spell.release.sound = new Sound(Identifier.ofVanilla("ambient.underwater.exit").toString());
        spell.release.particles_scaled_with_ranged = new ParticleBatch[]{
                new ParticleBatch(
                        SpellEngineParticles.area_effect_293.id().toString(),
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.GROUND,
                        1, 0.0F, 0.0F
                ).scale(0.8F).color(2816865791L)
        };

        var vulnerableModifier = SpellBuilder.ImpactModifiers.create("#more_rpg_classes:vulnerable_to_water_spells");
        vulnerableModifier.modifier = new Spell.Impact.Modifier();
        vulnerableModifier.modifier.critical_chance_bonus = 0.3F;

        var resistantModifier = SpellBuilder.ImpactModifiers.create("#more_rpg_classes:resistant_to_water_spells");
        resistantModifier.modifier = new Spell.Impact.Modifier();
        resistantModifier.modifier.power_multiplier = -0.3F;

        var damage = SpellBuilder.Impacts.damage(0.2F);
        damage.target_modifiers = List.of(vulnerableModifier, resistantModifier);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:splash",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        15, 0.05F, 0.2F
                )
        };
        damage.sound = Sound.withVolume(Identifier.of("more_rpg_classes:water_magic_impact1"), 0.4F);

        var heal = SpellBuilder.Impacts.heal(0.15F);
        heal.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:water_heal",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        5, 0.01F, 0.05F
                ),
                new ParticleBatch(
                        "more_rpg_classes:water_heal",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        5, 0.05F, 0.1F
                ),
                new ParticleBatch(
                        "more_rpg_classes:water_circle",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.FEET,
                        1, 0.2F, 1.0F
                )
        };
        heal.sound = Sound.withVolume(SpellEngineSounds.GENERIC_HEALING_IMPACT_2.id(), 1.2F);

        spell.impacts = List.of(damage, heal);
        SpellBuilder.Cost.cooldown(spell, 5);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    public static Entry zephyrs_speed = add(zephyrs_speed());
    private static Entry zephyrs_speed() {
        var id = Identifier.of(MOD_ID, "zephyrs_speed");
        var title = "Zephyr's Speed";
        var description = "On dealing damage with an active spell, gain a stack of Zephyr's Speed for {effect_duration} seconds. " +
                "Increasing spell crit chance by {bonus} and movement speed by {bonus2}.";
        var effect = MRPGCEffects.ZEPHYRS_SPEED;

        var spell = SpellBuilder.createSpellPassive();
        spell.school = MoreSpellSchools.AIR;
        spell.tier = 8;
        SpellTooltip.DescriptionMutator mutator = (args) -> {
            var modifier = effect.config().attributes().get(1);
            var modifier2 = effect.config().attributes().get(0);
            var bonus = SpellTooltip.bonus(modifier.value, modifier.operation);
            var bonus2 = SpellTooltip.bonus(modifier2.value, modifier2.operation);
            return args.description()
                    .replace("{bonus}", bonus)
                    .replace("{bonus2}", bonus2);
        };

        var trigger = SpellBuilder.Triggers.activeSpellHit(0.35F, null);
        trigger.target_override = Spell.Trigger.TargetSelector.CASTER;
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        var buff = SpellBuilder.Impacts.effectAdd(effect.id.toString(), 10, 1, 9);
        buff.action.status_effect.refresh_duration = true;
        buff.action.status_effect.show_particles = false;
        buff.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "more_rpg_classes:gust",
                        ParticleBatch.Shape.SPHERE, ParticleBatch.Origin.CENTER,
                        4, 0.5F, 0.8F
                ).extent(1.0F)
        };

        spell.impacts = List.of(buff);
        SpellBuilder.Cost.cooldown(spell, 5);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
    public static Entry obsidian_shards = add(obsidian_shards());
    private static Entry obsidian_shards() {
        var id = Identifier.of(MOD_ID, "obsidian_shards");
        var title = "Obsidian Shards";
        var description = "On dealing damage with an active spell, shoot obsidian shards outward in all directions from the target.";

        var spell = SpellBuilder.createSpellPassive();
        spell.school = MoreSpellSchools.EARTH;
        spell.range = 20.0F;
        spell.tier = 8;

        var trigger = SpellBuilder.Triggers.activeSpellHit(0.25F, null);
        spell.passive.triggers = List.of(trigger);

        spell.target.type = Spell.Target.Type.FROM_TRIGGER;

        spell.deliver.type = Spell.Delivery.Type.PROJECTILE;
        spell.deliver.projectile = new Spell.Delivery.ShootProjectile();
        spell.deliver.projectile.direction_offsets = new Spell.Delivery.ShootProjectile.DirectionOffset[]{
                new Spell.Delivery.ShootProjectile.DirectionOffset(),
                new Spell.Delivery.ShootProjectile.DirectionOffset(-360.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(270.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(-270.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(180.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(-180.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(90.0F, 0),
                new Spell.Delivery.ShootProjectile.DirectionOffset(-90.0F, 0)
        };
        spell.deliver.projectile.direct_towards_target = true;
        spell.deliver.projectile.launch_properties.velocity = 1.3F;
        spell.deliver.projectile.launch_properties.extra_launch_count = 7;
        spell.deliver.projectile.launch_properties.extra_launch_delay = 0;
        spell.deliver.projectile.launch_properties.sound =
                Sound.withVolume(Identifier.of("more_rpg_classes:earth_magic_cast1"), 0.6F);

        var projectile = new Spell.ProjectileData();
        projectile.homing_angle = 0.0F;
        projectile.perks = new Spell.ProjectileData.Perks();
        projectile.perks.pierce = 999;
        projectile.hitbox = new Spell.ProjectileData.HitBox();
        projectile.hitbox.width = 0.5F;
        projectile.hitbox.height = 0.5F;
        projectile.client_data = new Spell.ProjectileData.Client();
        projectile.client_data.model = new Spell.ProjectileModel();
        projectile.client_data.model.model_id = "more_rpg_classes:spell_projectile/obsidian_shards";
        projectile.client_data.model.scale = 0.5F;
        projectile.client_data.model.rotate_degrees_per_tick = 0.0F;
        spell.deliver.projectile.projectile = projectile;

        var damage = SpellBuilder.Impacts.damage(0.25F, 0.5F);
        damage.particles = new ParticleBatch[]{
                new ParticleBatch(
                        "campfire_cosy_smoke",
                        ParticleBatch.Shape.CIRCLE, ParticleBatch.Origin.CENTER,
                        3, 0.005F, 0.008F
                )
        };
        damage.sound = Sound.withVolume(Identifier.ofVanilla("block.pointed_dripstone.break"), 1.5F);

        spell.impacts = List.of(damage);
        SpellBuilder.Cost.cooldown(spell, 5);
        spell.cost.cooldown.hosting_item = false;

        return new Entry(id, spell, title, description);
    }
}
