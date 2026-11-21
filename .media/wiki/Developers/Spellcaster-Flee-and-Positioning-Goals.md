# Spellcaster Flee and Positioning Goals

Needs Spell Engine installed. These goals work with `ISpellCasterEntity` from [section 8](Mob-Spell-Casting).

Ready-made AI goals for spell-casting mobs that need to keep their distance and flee when low on health.

**`IConditionalFleeEntity`**

Implement this interface on your entity to unlock the two goals below. Your entity must extend `PathAwareEntity` (or any subclass) to use `LowHealthFleeGoal`. All methods have defaults, so override only what you need:

```java
public class MyWizard extends PathAwareEntity implements ISpellCasterEntity, IConditionalFleeEntity {

    @Override public float getFleeDistance()         { return 4.0F; }
    @Override public float getLowHealthFleeDistance() { return 6.0F; }

    @Override public List<Identifier> getFleeImmuneEffects() {
        return List.of(Identifier.of("mymod", "arcane_shield"));
    }

    @Override public List<Identifier> getFleeIgnoreIfTargetHasEffects() {
        return List.of(Identifier.of("mymod", "frost_snare"));
    }

    @Override public float getFleeIgnoreTargetHpThreshold() { return 0.3f; }
}
```
- `getFleeImmuneEffects`: effects on the caster that stop it from fleeing (e.g. a damage immunity shield).
- `getFleeIgnoreIfTargetHasEffects`: effects on the target that stop the caster from fleeing (e.g. a snare or root).
- `getFleeIgnoreTargetHpThreshold`: if the target's health share is below this value, the caster keeps attacking instead of fleeing. `0` turns it off.

**`LowHealthFleeGoal<T extends PathAwareEntity & IConditionalFleeEntity>`**

Flees from players when the mob drops below **35% HP**. Fleeing is skipped when any of the three conditions above is met (caster is immune, target is snared, or target is nearly dead).

```java
this.goalSelector.add(1, new LowHealthFleeGoal<>(this));
```

**`BackAwayGoal<T extends MobEntity & ISpellCasterEntity>`**

Backs away from the combat target whenever it gets closer than `minDistance` blocks. Stops as soon as it is safe, so spells can fire right away. Does nothing while the mob is already casting.

```java
this.goalSelector.add(2, new BackAwayGoal<>(this, 4.0F, 1.2));
```
The arguments are the mob, `minDistance` and the movement speed.

**Full example**

```java
@Override
protected void initGoals() {
    this.goalSelector.add(0, new SwimGoal(this));
    this.goalSelector.add(1, new LowHealthFleeGoal<>(this));
    this.goalSelector.add(2, new BackAwayGoal<>(this, 4.0F, 1.2));

    MobSpellCastGoal fireball  = new MobSpellCastGoal(this, "mymod:fireball",  spellGoals);
    MobSpellCastGoal frostbolt = new MobSpellCastGoal(this, "mymod:frostbolt", spellGoals);
    this.goalSelector.add(3, fireball);
    this.goalSelector.add(4, frostbolt);
    spellGoals.add(fireball);
    spellGoals.add(frostbolt);
}
```
