package com.mrpg_lib.entity;

import java.util.UUID;

public interface ControlledOwnerAccess {
    UUID mrpg$getControlOwner();

    void mrpg$setControlOwner(UUID owner);
}
