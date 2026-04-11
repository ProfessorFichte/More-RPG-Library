package net.more_rpg_classes.compat.armory_rpgs;

public enum FightClass {
    AIR_WIZARD("Air Wizard"),
    EARTH_WIZARD("Earth Wizard"),
    WATER_WIZARD("Water Wizard"),
    BERSERKER("Berserker"),
    FORCEMASTER("Forcemaster"),
    DEADEYE("Deadeye"),
    WAR_ARCHER("War Archer"),
    TUNDRA_HUNTER("Tundra Hunter"),
    BARD("Bard");

    final String translation;

    FightClass(String translation) {
        this.translation = translation;
    }
}
