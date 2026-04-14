package net.more_rpg_classes.custom.spell_impacts;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.more_rpg_classes.client.particle.MoreParticles;
import net.more_rpg_classes.client.particle.PopupParticleEffect;
import net.more_rpg_classes.custom.MoreSpellSchools;
import net.more_rpg_classes.entity.ISpellCasterEntity;
import net.more_rpg_classes.network.MobBeamPacket;
import net.spell_engine.api.spell.ExternalSpellSchools;
import net.spell_engine.api.spell.Spell;
import net.spell_engine.api.spell.event.SpellHandlers;
import net.spell_engine.api.spell.fx.ParticleBatch;
import net.spell_engine.api.spell.registry.SpellRegistry;
import net.spell_engine.fx.ParticleHelper;
import net.spell_engine.internals.SpellHelper;
import net.spell_engine.internals.container.SpellContainerSource;
import net.spell_engine.internals.target.SpellTarget;
import net.spell_engine.utils.SoundHelper;
import net.spell_engine.utils.TargetHelper;
import net.spell_engine.utils.WorldScheduler;
import net.spell_power.api.SpellPower;
import net.spell_power.api.SpellSchools;

import java.util.*;

public class SpellthiefImpact implements SpellHandlers.CustomImpact {

    private static final Set<UUID> stealingCasters = new HashSet<>();
    private static final Map<Identifier, List<RegistryEntry<Spell>>> mobSpellCache = new HashMap<>();

    private enum DeliveryType { PROJECTILE, CLOUD, METEOR, AREA, DIRECT, BEAM }

    @Override
    public SpellHandlers.ImpactResult onSpellImpact(
            RegistryEntry<Spell> spell,
            SpellPower.Result powerResult,
            LivingEntity caster,
            Entity target,
            SpellHelper.ImpactContext context
    ) {
        if (!(target instanceof LivingEntity livingTarget) || target == caster) {
            return new SpellHandlers.ImpactResult(false, false);
        }
        if (stealingCasters.contains(caster.getUuid())) {
            return new SpellHandlers.ImpactResult(false, false);
        }
        if (caster.getWorld().isClient()) {
            return new SpellHandlers.ImpactResult(true, false);
        }

        List<StatusEffectInstance> beneficialEffects = livingTarget.getStatusEffects().stream()
                .filter(e -> e.getEffectType().value().getCategory() == StatusEffectCategory.BENEFICIAL)
                .toList();
        for (StatusEffectInstance effect : beneficialEffects) {
            caster.addStatusEffect(new StatusEffectInstance(effect));
            livingTarget.removeStatusEffect(effect.getEffectType());
        }

        if (!beneficialEffects.isEmpty() && caster.getWorld() instanceof ServerWorld serverWorld) {
            for (StatusEffectInstance effect : beneficialEffects) {
                Identifier effectId = effect.getEffectType().getKey().map(k -> k.getValue()).orElse(null);
                if (effectId != null) {
                    serverWorld.spawnParticles(
                        new PopupParticleEffect(MoreParticles.SPELL_STOLEN_POPUP, effectId, false, caster.getId()),
                        caster.getX(), caster.getEyeY() + 0.2, caster.getZ(), 1, 0, 0, 0, 0);
                }
            }
            if (caster instanceof ServerPlayerEntity serverPlayer) {
                for (StatusEffectInstance effect : beneficialEffects) {
                    serverPlayer.sendMessage(
                        Text.translatable("message.more_rpg_classes.spellthief.effect_stolen",
                            Text.translatable(effect.getEffectType().value().getTranslationKey()),
                            livingTarget.getDisplayName()),
                        true
                    );
                }
            }
        }

        RegistryEntry<Spell> stolenEntry = null;

        if (livingTarget instanceof PlayerEntity targetPlayer) {
            List<RegistryEntry<Spell>> playerSpells = SpellContainerSource.activeSpellsOf(targetPlayer);
            if (!playerSpells.isEmpty()) {
                stolenEntry = playerSpells.get(caster.getRandom().nextInt(playerSpells.size()));
            }
        } else if (livingTarget instanceof ISpellCasterEntity) {
            Identifier entityTypeId = Registries.ENTITY_TYPE.getId(livingTarget.getType());
            List<RegistryEntry<Spell>> mobSpells = mobSpellCache.computeIfAbsent(entityTypeId, id -> {
                String prefix = "mob/" + id.getPath() + "/";
                String ns = id.getNamespace();
                Registry<Spell> spellRegistry = SpellRegistry.from(livingTarget.getWorld());
                List<RegistryEntry<Spell>> result = new ArrayList<>();
                spellRegistry.streamTags()
                    .filter(tag -> tag.id().getNamespace().equals(ns) && tag.id().getPath().startsWith(prefix))
                    .forEach(tag -> spellRegistry.getEntryList(tag).ifPresent(list -> list.forEach(result::add)));
                return result.isEmpty() ? List.of() : List.copyOf(result);
            });
            if (!mobSpells.isEmpty()) {
                stolenEntry = mobSpells.get(caster.getRandom().nextInt(mobSpells.size()));
            }
        }

        if (stolenEntry == null) {
            return new SpellHandlers.ImpactResult(true, false);
        }

        Identifier spellId = stolenEntry.getKey().get().getValue();
        stealingCasters.add(caster.getUuid());
        ((WorldScheduler) caster.getWorld()).schedule(1, () -> stealingCasters.remove(caster.getUuid()));
        castStolenSpell(caster, livingTarget, stolenEntry);

        if (caster instanceof ServerPlayerEntity serverPlayer) {
            Spell stolenSpell = stolenEntry.value();
            int color = (stolenSpell.school != null) ? stolenSpell.school.color : 0xFFFFFF;
            Text spellText = Text.translatable("spell." + spellId.getNamespace() + "." + spellId.getPath() + ".name")
                .styled(s -> s.withBold(true).withColor(color));
            serverPlayer.sendMessage(
                Text.translatable("message.more_rpg_classes.spellthief.spell_stolen", spellText, livingTarget.getDisplayName()),
                true
            );
        }

        if (caster.getWorld() instanceof ServerWorld serverWorld) {
            serverWorld.spawnParticles(
                new PopupParticleEffect(MoreParticles.SPELL_STOLEN_POPUP, spellId, true, caster.getId()),
                caster.getX(), caster.getEyeY() + 0.2, caster.getZ(),
                1, 0, 0, 0, 0
            );
        }

        return new SpellHandlers.ImpactResult(true, false);
    }

    private void castStolenSpell(LivingEntity caster, LivingEntity target, RegistryEntry<Spell> spellEntry) {
        Spell stolenSpell = spellEntry.value();
        SpellPower.Result power = getHighestPower(caster);
        boolean isHelpful = isPrimarilyHelpful(stolenSpell);
        DeliveryType delivery = deriveDelivery(stolenSpell);
        boolean hasSpawn = hasSpawnImpact(stolenSpell);
        boolean harmfulCustomSpawn = delivery == DeliveryType.DIRECT && hasHarmfulCustomImpact(stolenSpell);
        ParticleBatch[] castParticles = (stolenSpell.active != null && stolenSpell.active.cast != null
                && stolenSpell.active.cast.particles != null && stolenSpell.active.cast.particles.length > 0)
                ? stolenSpell.active.cast.particles : null;

        if (stolenSpell.release != null) {
            ParticleHelper.sendBatches(caster, stolenSpell.release.particles);
            SoundHelper.playSound(caster.getWorld(), caster, stolenSpell.release.sound);
            if (stolenSpell.release.particles_scaled_with_ranged != null) {
                ParticleBatch[] batches = stolenSpell.release.particles_scaled_with_ranged;
                ParticleBatch[] scaled = new ParticleBatch[batches.length];
                for (int i = 0; i < batches.length; i++) {
                    scaled[i] = batches[i].copy().scale(stolenSpell.range);
                }
                ParticleHelper.sendBatches(caster, scaled);
            }
        }

        int channelCount = (stolenSpell.active != null && stolenSpell.active.cast != null)
                ? stolenSpell.active.cast.channel_ticks : 0;
        int releaseCount = channelCount > 0 ? channelCount : 1;
        int tickInterval = (channelCount > 0 && stolenSpell.active.cast.duration > 0)
                ? Math.max(1, (int)(stolenSpell.active.cast.duration * 20 / channelCount)) : 0;

        WorldScheduler scheduler = (WorldScheduler) caster.getWorld();

        if (delivery == DeliveryType.BEAM && stolenSpell.target != null && stolenSpell.target.beam != null) {
            sendBeamPacket(caster, target, spellEntry.getKey().get().getValue());
        }

        for (int releaseIndex = 0; releaseIndex < releaseCount; releaseIndex++) {
            final int delay = releaseIndex * tickInterval;
            Runnable release = () -> {
                if (!caster.isAlive()) return;
                if (castParticles != null) {
                    ParticleHelper.sendBatches(caster, castParticles);
                }
                switch (delivery) {
                    case PROJECTILE -> {
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(caster.getEyePos()).target(SpellHelper.focusMode(stolenSpell));
                        if (isHelpful) {
                            SpellHelper.performImpacts(caster.getWorld(), caster, caster, caster, spellEntry,
                                    stolenSpell.impacts, ctx, false, null);
                        } else {
                            SpellHelper.shootProjectile(caster.getWorld(), caster, target, spellEntry, ctx, 0);
                        }
                    }
                    case CLOUD -> {
                        LivingEntity cloudTarget = isHelpful ? caster : (stolenSpell.target != null && stolenSpell.target.aim != null && stolenSpell.target.aim.required ? target : caster);
                        Vec3d pos = cloudTarget.getPos();
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(caster.getEyePos()).target(SpellTarget.FocusMode.AREA);
                        SpellHelper.placeCloud(caster.getWorld(), caster, cloudTarget, pos, spellEntry, ctx);
                    }
                    case METEOR -> {
                        LivingEntity meteorTarget = isHelpful ? caster : (stolenSpell.target != null && stolenSpell.target.aim != null && stolenSpell.target.aim.required ? target : caster);
                        Vec3d pos = meteorTarget.getPos();
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(pos).target(SpellTarget.FocusMode.AREA);
                        try {
                            SpellHelper.fallProjectile(caster.getWorld(), caster, meteorTarget, pos, spellEntry, ctx);
                        } catch (Exception e) {
                            SpellHelper.performImpacts(caster.getWorld(), caster, meteorTarget, caster, spellEntry,
                                    stolenSpell.impacts, ctx, false, null);
                        }
                    }
                    case AREA -> {
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(caster.getPos()).target(SpellTarget.FocusMode.AREA);
                        if (hasSpawn) {
                            SpellHelper.ImpactContext spawnCtx = new SpellHelper.ImpactContext()
                                    .power(power).position(caster.getPos()).target(SpellTarget.FocusMode.DIRECT);
                            SpellHelper.performImpacts(caster.getWorld(), caster, caster, caster, spellEntry,
                                    stolenSpell.impacts, spawnCtx, false, Spell.Impact.Action.Type.SPAWN);
                        }
                        if (isHelpful) {
                            SpellHelper.performImpacts(caster.getWorld(), caster, caster, caster, spellEntry,
                                    stolenSpell.impacts, ctx, false, null);
                        }
                        Spell.Target.Area area = stolenSpell.target != null ? stolenSpell.target.area : null;
                        for (Entity t : TargetHelper.targetsFromArea(caster, stolenSpell.range, area, e -> e != caster)) {
                            SpellHelper.performImpacts(caster.getWorld(), caster, t, caster, spellEntry,
                                    stolenSpell.impacts, ctx, false, null);
                        }
                    }
                    case BEAM -> {
                        if (!target.isAlive()) {
                            sendBeamClearPacket(caster);
                            return;
                        }
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(caster.getEyePos()).target(SpellTarget.FocusMode.AREA);
                        if (hasSpawn) {
                            SpellHelper.ImpactContext spawnCtx = new SpellHelper.ImpactContext()
                                    .power(power).position(target.getPos()).target(SpellTarget.FocusMode.DIRECT);
                            SpellHelper.performImpacts(caster.getWorld(), caster, caster, caster, spellEntry,
                                    stolenSpell.impacts, spawnCtx, false, Spell.Impact.Action.Type.SPAWN);
                        }
                        SpellHelper.performImpacts(caster.getWorld(), caster, target, caster, spellEntry,
                                stolenSpell.impacts, ctx, false, null);
                        double beamRange = stolenSpell.range > 0 ? stolenSpell.range : 32.0;
                        Vec3d beamFrom = caster.getEyePos();
                        Vec3d beamDir = target.getEyePos().subtract(beamFrom).normalize();
                        if (caster.getWorld() instanceof ServerWorld beamWorld) {
                            for (Entity candidate : beamWorld.getOtherEntities(caster,
                                    caster.getBoundingBox().expand(beamRange),
                                    e -> e instanceof LivingEntity && e.isAlive() && e != target)) {
                                Vec3d toCandidate = candidate.getBoundingBox().getCenter().subtract(beamFrom);
                                double projection = toCandidate.dotProduct(beamDir);
                                if (projection < 0 || projection > beamRange) continue;
                                if (toCandidate.subtract(beamDir.multiply(projection)).lengthSquared() > 4.0) continue;
                                SpellHelper.performImpacts(caster.getWorld(), caster, candidate, caster, spellEntry,
                                        stolenSpell.impacts, ctx, false, null);
                            }
                        }
                    }
                    case DIRECT -> {
                        LivingEntity directTarget = isHelpful ? caster : target;
                        Vec3d contextPos = (isHelpful || !harmfulCustomSpawn) ? caster.getEyePos() : target.getPos();
                        SpellHelper.ImpactContext ctx = new SpellHelper.ImpactContext()
                                .power(power).position(contextPos).target(SpellHelper.focusMode(stolenSpell));
                        if (hasSpawn) {
                            Vec3d spawnPos = (isHelpful || !harmfulCustomSpawn) ? caster.getPos() : target.getPos();
                            SpellHelper.ImpactContext spawnCtx = new SpellHelper.ImpactContext()
                                    .power(power).position(spawnPos).target(SpellTarget.FocusMode.DIRECT);
                            SpellHelper.performImpacts(caster.getWorld(), caster, caster, caster, spellEntry,
                                    stolenSpell.impacts, spawnCtx, false, Spell.Impact.Action.Type.SPAWN);
                        }
                        SpellHelper.performImpacts(caster.getWorld(), caster, directTarget, caster, spellEntry,
                                stolenSpell.impacts, ctx, false, null);
                    }
                }
                if (stolenSpell.impacts != null) {
                    for (Spell.Impact impact : stolenSpell.impacts) {
                        if (impact.sound != null) {
                            SoundHelper.playSound(caster.getWorld(), caster, impact.sound);
                        }
                    }
                }
            };
            if (delay <= 0) {
                release.run();
            } else {
                scheduler.schedule(delay, release);
            }
        }

        if (delivery == DeliveryType.BEAM && stolenSpell.target != null && stolenSpell.target.beam != null) {
            int clearDelay = releaseCount > 1 ? (releaseCount - 1) * tickInterval + 2 : 2;
            scheduler.schedule(clearDelay, () -> sendBeamClearPacket(caster));
        }
    }

    private static void sendBeamPacket(LivingEntity caster, LivingEntity target, Identifier spellId) {
        if (!(caster.getWorld() instanceof ServerWorld sw)) return;
        var packet = new MobBeamPacket(caster.getId(), target.getId(), spellId);
        sw.getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    private static void sendBeamClearPacket(LivingEntity caster) {
        if (!(caster.getWorld() instanceof ServerWorld sw)) return;
        var packet = new MobBeamPacket(caster.getId(), -1, null);
        sw.getPlayers().forEach(player -> ServerPlayNetworking.send(player, packet));
    }

    private SpellPower.Result getHighestPower(LivingEntity caster) {
        SpellPower.Result best = SpellPower.getSpellPower(SpellSchools.ARCANE, caster);
        SpellPower.Result[] candidates = {
            SpellPower.getSpellPower(SpellSchools.FIRE, caster),
            SpellPower.getSpellPower(SpellSchools.FROST, caster),
            SpellPower.getSpellPower(SpellSchools.HEALING, caster),
            SpellPower.getSpellPower(SpellSchools.LIGHTNING, caster),
            SpellPower.getSpellPower(SpellSchools.SOUL, caster),
            SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_MELEE, caster),
            SpellPower.getSpellPower(ExternalSpellSchools.PHYSICAL_RANGED, caster),
            SpellPower.getSpellPower(ExternalSpellSchools.DEFENSE, caster),
            SpellPower.getSpellPower(ExternalSpellSchools.HEALTH, caster),
            SpellPower.getSpellPower(MoreSpellSchools.EARTH, caster),
            SpellPower.getSpellPower(MoreSpellSchools.WATER, caster),
            SpellPower.getSpellPower(MoreSpellSchools.AIR, caster),
            SpellPower.getSpellPower(MoreSpellSchools.NATURE, caster),
        };
        for (SpellPower.Result r : candidates) {
            if (r.baseValue() > best.baseValue()) best = r;
        }
        return best;
    }

    private boolean isPrimarilyHelpful(Spell spell) {
        var intents = SpellHelper.impactIntents(spell);
        return intents.contains(SpellTarget.Intent.HELPFUL) && !intents.contains(SpellTarget.Intent.HARMFUL);
    }

    private DeliveryType deriveDelivery(Spell spell) {
        if (spell.target != null) {
            if (spell.target.type == Spell.Target.Type.AREA) return DeliveryType.AREA;
            if (spell.target.type == Spell.Target.Type.BEAM) return DeliveryType.BEAM;
        }
        if (spell.deliver != null) {
            return switch (spell.deliver.type) {
                case PROJECTILE, SHOOT_ARROW -> DeliveryType.PROJECTILE;
                case METEOR -> DeliveryType.METEOR;
                case CLOUD -> DeliveryType.CLOUD;
                default -> DeliveryType.DIRECT;
            };
        }
        return DeliveryType.DIRECT;
    }

    private boolean hasSpawnImpact(Spell spell) {
        if (spell.impacts == null) return false;
        return spell.impacts.stream()
                .anyMatch(i -> i.action != null && i.action.type == Spell.Impact.Action.Type.SPAWN);
    }

    private boolean hasHarmfulCustomImpact(Spell spell) {
        if (spell.impacts == null) return false;
        return spell.impacts.stream()
                .anyMatch(i -> i.action != null && i.action.type == Spell.Impact.Action.Type.CUSTOM
                        && i.action.custom != null && i.action.custom.intent == SpellTarget.Intent.HARMFUL);
    }

}
