package net.more_rpg_classes.entity;

import net.more_rpg_classes.entity.attribute.ProjectileAttributeData;

public interface IProjectileAttributeStorage {
    void mrpgc$setAttributeData(ProjectileAttributeData data);
    ProjectileAttributeData mrpgc$getAttributeData();
    boolean mrpgc$hasAttributeData();
}
