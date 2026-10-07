package com.techcraft;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = TechCraft.MODID)
public class ModConfig {
    @Config.Comment("Coal Generator settings")
    public static Generator generator = new Generator();

    @Config.Comment("Electric Furnace settings")
    public static Processor electricFurnace = new Processor(20, 100, 20000);

    @Config.Comment("Crusher settings")
    public static Processor crusher = new Processor(30, 200, 20000);

    @Config.Comment("Battery Box settings")
    public static Battery batteryBox = new Battery();

    public static class Generator {
        @Config.Comment("FE produced per tick while burning")
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerTick = 40;

        @Config.Comment("Internal energy buffer (FE)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 50000;

        @Config.Comment("Max FE pushed to neighbours per tick")
        @Config.RangeInt(min = 1, max = 1000000)
        public int maxOutput = 200;

        @Config.Comment("Burn time multiplier relative to the vanilla furnace")
        @Config.RangeDouble(min = 0.1, max = 10)
        public double burnTimeMultiplier = 1.0;
    }

    public static class Processor {
        @Config.Comment("FE consumed per tick while working")
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerTick;

        @Config.Comment("Ticks needed for one operation")
        @Config.RangeInt(min = 1, max = 10000)
        public int ticksPerOperation;

        @Config.Comment("Internal energy buffer (FE)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity;

        public Processor(int energyPerTick, int ticksPerOperation, int capacity) {
            this.energyPerTick = energyPerTick;
            this.ticksPerOperation = ticksPerOperation;
            this.capacity = capacity;
        }
    }

    public static class Battery {
        @Config.Comment("Energy storage (FE)")
        @Config.RangeInt(min = 1000, max = 2000000000)
        public int capacity = 1000000;

        @Config.Comment("Max FE input/output per tick")
        @Config.RangeInt(min = 1, max = 1000000)
        public int transferRate = 1000;
    }

    @Mod.EventBusSubscriber(modid = TechCraft.MODID)
    public static class EventHandler {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
            if (TechCraft.MODID.equals(event.getModID())) {
                ConfigManager.sync(TechCraft.MODID, Config.Type.INSTANCE);
            }
        }
    }
}
