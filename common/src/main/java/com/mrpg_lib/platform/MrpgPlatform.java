package com.mrpg_lib.platform;

public final class MrpgPlatform {
    public interface Util {
        boolean isModLoaded(String modId);

        boolean isDevelopmentEnvironment();
    }

    private static Util util;

    private MrpgPlatform() {
    }

    public static void setUtil(Util util) {
        MrpgPlatform.util = util;
    }

    public static boolean isModLoaded(String modId) {
        return util().isModLoaded(modId);
    }

    public static boolean isDevelopmentEnvironment() {
        return util().isDevelopmentEnvironment();
    }

    private static final String[] LOADER_UTILS = {
            "com.mrpg_lib.fabric.FabricPlatformUtil",
            "com.mrpg_lib.neoforge.NeoForgePlatformUtil"
    };

    private static Util util() {
        Util current = util;
        if (current == null) {
            current = detect();
            util = current;
        }
        return current;
    }

    private static Util detect() {
        for (String name : LOADER_UTILS) {
            try {
                Class<?> type = Class.forName(name, true, MrpgPlatform.class.getClassLoader());
                return (Util) type.getDeclaredConstructor().newInstance();
            } catch (ReflectiveOperationException | LinkageError ignored) {
            }
        }
        throw new IllegalStateException("MrpgPlatform could not find a loader implementation");
    }
}
