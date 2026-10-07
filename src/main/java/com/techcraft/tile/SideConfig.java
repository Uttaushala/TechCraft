package com.techcraft.tile;

import net.minecraft.util.EnumFacing;

/** Per-face mode for each resource. Stored by absolute direction; packed into 12 bits per resource for syncing. */
public final class SideConfig {
    public static final int ENERGY = 0;
    public static final int ITEMS = 1;
    public static final int FLUIDS = 2;
    public static final int RESOURCES = 3;

    private final FaceMode[][] modes = new FaceMode[RESOURCES][6];

    public SideConfig() {
        for (FaceMode[] row : modes) {
            java.util.Arrays.fill(row, FaceMode.DEFAULT);
        }
    }

    public FaceMode get(int resource, EnumFacing face) {
        return modes[resource][face.getIndex()];
    }

    public void set(int resource, EnumFacing face, FaceMode mode) {
        modes[resource][face.getIndex()] = mode;
    }

    public void cycle(int resource, EnumFacing face) {
        set(resource, face, get(resource, face).next());
    }

    public int encode(int resource) {
        int value = 0;
        for (int i = 0; i < 6; i++) {
            value |= modes[resource][i].ordinal() << (2 * i);
        }
        return value;
    }

    public void decode(int resource, int value) {
        for (int i = 0; i < 6; i++) {
            modes[resource][i] = FaceMode.values()[(value >> (2 * i)) & 3];
        }
    }

    public static FaceMode decodeFace(int encoded, EnumFacing face) {
        return FaceMode.values()[(encoded >> (2 * face.getIndex())) & 3];
    }
}
