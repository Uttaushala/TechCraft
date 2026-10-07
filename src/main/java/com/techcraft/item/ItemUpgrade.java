package com.techcraft.item;

/** Goes into a machine's upgrade slots. Up to 4 per slot. */
public class ItemUpgrade extends ItemBase {
    public enum Type {
        /** Each upgrade: +50% work speed, +80% power draw. */
        SPEED,
        /** Each upgrade: -15% energy use (never below 20%). */
        EFFICIENCY
    }

    private final Type type;

    public ItemUpgrade(String name, Type type) {
        super(name);
        this.type = type;
        setMaxStackSize(4);
    }

    public Type getType() {
        return type;
    }
}
