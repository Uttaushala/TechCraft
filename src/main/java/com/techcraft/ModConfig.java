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

    @Config.Comment("Advanced Battery Box settings")
    public static Battery batteryBoxAdvanced = new Battery(10000000, 10000);

    @Config.Comment("Ultimate Battery Box settings")
    public static Battery batteryBoxUltimate = new Battery(100000000, 100000);

    @Config.Comment("Compressor settings")
    public static Processor compressor = new Processor(25, 120, 20000);

    @Config.Comment("Alloy Furnace settings")
    public static Processor alloyFurnace = new Processor(35, 160, 20000);

    @Config.Comment("Charger settings")
    public static Charger charger = new Charger();

    @Config.Comment("Solar panel output (FE/t in full daylight; halved in rain)")
    public static Solar solar = new Solar();

    @Config.Comment("Wind Turbine settings")
    public static Wind windTurbine = new Wind();

    @Config.Comment("Lava Generator settings")
    public static LavaGenerator lavaGenerator = new LavaGenerator();

    @Config.Comment("Fluid Tank settings")
    public static FluidTankSettings fluidTank = new FluidTankSettings();

    @Config.Comment("Batteries and electric tools")
    public static Equipment equipment = new Equipment();

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
        public int capacity;

        @Config.Comment("Max FE input/output per tick")
        @Config.RangeInt(min = 1, max = 100000000)
        public int transferRate;

        public Battery() {
            this(1000000, 1000);
        }

        public Battery(int capacity, int transferRate) {
            this.capacity = capacity;
            this.transferRate = transferRate;
        }
    }

    public static class Charger {
        @Config.Comment("Internal energy buffer (FE)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 100000;

        @Config.Comment("Max FE moved into each item per tick")
        @Config.RangeInt(min = 1, max = 10000000)
        public int transferRate = 4000;
    }

    public static class Solar {
        @Config.RangeInt(min = 1, max = 100000)
        public int basic = 6;
        @Config.RangeInt(min = 1, max = 100000)
        public int advanced = 24;
        @Config.RangeInt(min = 1, max = 100000)
        public int ultimate = 96;
    }

    public static class Wind {
        @Config.Comment("FE/t at build height (Y 170+); falls to 0 at Y 50. Storms give a bonus.")
        @Config.RangeInt(min = 1, max = 100000)
        public int maxOutput = 40;
    }

    public static class LavaGenerator {
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerTick = 100;

        @Config.Comment("mB of lava consumed per burn cycle")
        @Config.RangeInt(min = 1, max = 1000)
        public int mbPerCycle = 10;

        @Config.Comment("Ticks one burn cycle lasts")
        @Config.RangeInt(min = 1, max = 10000)
        public int cycleTicks = 40;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 50000;

        @Config.RangeInt(min = 1, max = 1000000)
        public int maxOutput = 400;

        @Config.Comment("Lava storage (mB)")
        @Config.RangeInt(min = 1000, max = 1000000)
        public int tankCapacity = 8000;
    }

    public static class FluidTankSettings {
        @Config.Comment("Storage (mB)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 64000;
    }

    public static class Equipment {
        @Config.RangeInt(min = 1000, max = 2000000000)
        public int batteryBasicCapacity = 100000;
        @Config.RangeInt(min = 1000, max = 2000000000)
        public int batteryAdvancedCapacity = 1000000;
        @Config.RangeInt(min = 1000, max = 2000000000)
        public int batteryUltimateCapacity = 10000000;

        @Config.Comment("Max FE per tick a battery accepts/gives")
        @Config.RangeInt(min = 1, max = 100000000)
        public int batteryBasicTransfer = 1000;
        @Config.RangeInt(min = 1, max = 100000000)
        public int batteryAdvancedTransfer = 10000;
        @Config.RangeInt(min = 1, max = 100000000)
        public int batteryUltimateTransfer = 100000;

        @Config.RangeInt(min = 1000, max = 2000000000)
        public int toolCapacity = 400000;
        @Config.Comment("Max FE per tick a tool charges at")
        @Config.RangeInt(min = 1, max = 100000000)
        public int toolTransfer = 4000;

        @Config.Comment("FE per block broken with the Electric Drill")
        @Config.RangeInt(min = 0, max = 100000)
        public int drillEnergyPerBlock = 60;
        @Config.RangeInt(min = 1, max = 100)
        public int drillSpeed = 14;

        @Config.Comment("FE per block broken with the Chainsaw")
        @Config.RangeInt(min = 0, max = 100000)
        public int chainsawEnergyPerBlock = 50;
        @Config.RangeInt(min = 1, max = 100)
        public int chainsawSpeed = 16;
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
