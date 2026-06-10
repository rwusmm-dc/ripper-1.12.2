package com.rayferric.havook.feature.mod.misc;

import com.rayferric.havook.feature.Mod;
import com.rayferric.havook.feature.mod.ModCategoryEnum;

public class FastMathMod extends Mod {
    public FastMathMod() {
        super("fastmath", "Fast Math", "Reduces CPU spikes from ESP calculations using fast approximations.", ModCategoryEnum.MISC);
    }

    public static float fastSqrt(float x) {
        return (float) Math.sqrt(x);
    }

    public static float fastDist(float x1, float y1, float z1, float x2, float y2, float z2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        return fastSqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double fastDist(double x1, double y1, double z1, double x2, double y2, double z2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double dz = z2 - z1;
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static float fastSin(float x) {
        return (float) Math.sin(x);
    }

    public static float fastCos(float x) {
        return (float) Math.cos(x);
    }

    public static float fastAtan2(float y, float x) {
        return (float) Math.atan2(y, x);
    }
}