package com.mrpg_lib.compat.spell_engine;

import com.mrpg_lib.entity.ISpellCasterEntity;
import com.mrpg_lib.entity.TestSpellCasters;

import net.minecraft.entity.mob.MobEntity;
import net.spell_engine.internals.target.EntityRelations;

public class MrpgEntityRelationMatcher {
    public static void register(){
        EntityRelations.registerTeamMatcher("mrpg_mob_spell_caster", (entity1, entity2) -> {
            if (!(entity1 instanceof ISpellCasterEntity) && !TestSpellCasters.isMarked(entity1)) return null;
            if (!(entity1 instanceof MobEntity mob1) || !(entity2 instanceof MobEntity mob2)) return null;
            if (mob1.getTarget() == entity2 || mob2.getTarget() == entity1) return null;
            return new EntityRelations.TeamRelation(true, false);
        });
    }
}
