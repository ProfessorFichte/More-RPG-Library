package net.more_rpg_classes.entity;

import net.minecraft.entity.mob.MobEntity;
import net.spell_engine.internals.target.EntityRelations;

public class MrpgEntityRelationMatcher {
    public static void register(){
        EntityRelations.registerTeamMatcher("mrpg_mob_spell_caster", (entity1, entity2) -> {
            if (!(entity1 instanceof ISpellCasterEntity)) return null;
            if (!(entity1 instanceof MobEntity mob1) || !(entity2 instanceof MobEntity mob2)) return null;
            if (mob1.getTarget() == entity2 || mob2.getTarget() == entity1) return null;
            return new EntityRelations.TeamRelation(true, false);
        });
    }
}
