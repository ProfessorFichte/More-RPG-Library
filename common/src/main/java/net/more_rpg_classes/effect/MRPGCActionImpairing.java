package net.more_rpg_classes.effect;

import net.spell_engine.api.effect.EntityActionsAllowed;

public class MRPGCActionImpairing {

    public static final EntityActionsAllowed FROZEN = new EntityActionsAllowed(
            false,
            false,
            new EntityActionsAllowed.PlayersAllowed(
                    false,
                    false,
                    false
            ),
            new EntityActionsAllowed.MobsAllowed(
                    false
            ),
            EntityActionsAllowed.SemanticType.NONE
    );

    public static final EntityActionsAllowed IGNITED = new EntityActionsAllowed(
            true,
            false,
            new EntityActionsAllowed.PlayersAllowed(
                    false,
                    false,
                    false
            ),
            new EntityActionsAllowed.MobsAllowed(
                    false
            ),
            EntityActionsAllowed.SemanticType.NONE
    );
    public static final EntityActionsAllowed STAGGER = new EntityActionsAllowed(
            true,
            true,
            new EntityActionsAllowed.PlayersAllowed(
                    false,
                    false,
                    false
            ),
            new EntityActionsAllowed.MobsAllowed(
                    false
            ),
            EntityActionsAllowed.SemanticType.NONE
    );
    public static final EntityActionsAllowed FEARED = new EntityActionsAllowed(
            true,
            true,
            new EntityActionsAllowed.PlayersAllowed(
                    false,
                    false,
                    false
            ),
            new EntityActionsAllowed.MobsAllowed(
                    false
            ),
            EntityActionsAllowed.SemanticType.NONE
    );
}
