package net.more_rpg_classes.entity;

import java.util.UUID;

public interface ControlledOwnerAccess {
    UUID mrpg$getControlOwner();

    void mrpg$setControlOwner(UUID owner);
}
