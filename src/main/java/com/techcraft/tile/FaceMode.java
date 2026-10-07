package com.techcraft.tile;

/** What a machine face does for one resource (energy, items or fluids). */
public enum FaceMode {
    /** The machine's built-in behaviour. */
    DEFAULT,
    INPUT,
    OUTPUT,
    DISABLED;

    public FaceMode next() {
        return values()[(ordinal() + 1) % values().length];
    }
}
