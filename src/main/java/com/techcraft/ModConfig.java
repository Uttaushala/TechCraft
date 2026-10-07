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

    @Config.Comment("Biomass Generator settings (burns crops, seeds, saplings, leaves and plant food)")
    public static Generator biomass = new Generator(25, 40000, 200);

    @Config.Comment("Geothermal Generator settings")
    public static Geothermal geothermal = new Geothermal();

    @Config.Comment("Water Wheel settings")
    public static WaterWheel waterWheel = new WaterWheel();

    @Config.Comment("Pump settings")
    public static Pump pump = new Pump();

    @Config.Comment("Auto Miner settings")
    public static Miner miner = new Miner();

    @Config.Comment("Block Breaker settings")
    public static Breaker blockBreaker = new Breaker();

    @Config.Comment("Block Placer settings")
    public static Placer blockPlacer = new Placer();

    @Config.Comment("Mob Grinder settings")
    public static Grinder mobGrinder = new Grinder();

    @Config.Comment("Gas Tank and Gas Generator (need Mekanism)")
    public static GasSettings gas = new GasSettings();

    @Config.Comment("IC2 energy converters (need IndustrialCraft 2)")
    public static Ic2 ic2 = new Ic2();

    @Config.Comment("Batteries and electric tools")
    public static Equipment equipment = new Equipment();

    public static class Generator {
        @Config.Comment("FE produced per tick while burning")
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerTick;

        @Config.Comment("Internal energy buffer (FE)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity;

        @Config.Comment("Max FE pushed to neighbours per tick")
        @Config.RangeInt(min = 1, max = 1000000)
        public int maxOutput;

        @Config.Comment("Burn time multiplier relative to the vanilla furnace")
        @Config.RangeDouble(min = 0.1, max = 10)
        public double burnTimeMultiplier = 1.0;

        public Generator() {
            this(40, 50000, 200);
        }

        public Generator(int energyPerTick, int capacity, int maxOutput) {
            this.energyPerTick = energyPerTick;
            this.capacity = capacity;
            this.maxOutput = maxOutput;
        }
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

    public static class GasSettings {
        @Config.Comment("Gas Tank storage (mB)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int tankCapacity = 64000;

        @Config.Comment("Gas Generator tank (mB)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int generatorTankCapacity = 16000;

        @Config.Comment("Gas burned per tick")
        @Config.RangeInt(min = 1, max = 10000)
        public int mbPerTick = 2;

        @Config.Comment("Gas name=FE produced per mB burned. Gases not listed can't be used.")
        public String[] fuels = {"hydrogen=15", "ethene=60"};

        @Config.RangeInt(min = 1000, max = 100000000)
        public int generatorCapacity = 50000;

        @Config.RangeInt(min = 1, max = 1000000)
        public int generatorMaxOutput = 400;
    }

    public static class Ic2 {
        @Config.Comment("FE per 1 EU")
        @Config.RangeInt(min = 1, max = 100)
        public int feePerEu = 4;

        @Config.Comment("Internal buffer of each converter (FE)")
        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 200000;

        @Config.Comment("Max FE per tick moved to the FE side by the EU to FE converter")
        @Config.RangeInt(min = 1, max = 100000000)
        public int maxFeOutput = 20000;

        @Config.Comment("IC2 tier of the converters: 1 = 32 EU/t, 2 = 128, 3 = 512, 4 = 2048, 5 = 8192")
        @Config.RangeInt(min = 1, max = 5)
        public int tier = 4;
    }

    public static class Geothermal {
        @Config.Comment("FE/t for every lava block touching the generator (not on top)")
        @Config.RangeInt(min = 1, max = 100000)
        public int perLava = 20;
    }

    public static class WaterWheel {
        @Config.Comment("FE/t for every flowing water block on a side of the wheel")
        @Config.RangeInt(min = 1, max = 100000)
        public int perWater = 10;
    }

    public static class Pump {
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerTick = 40;

        @Config.Comment("Ticks to pump one bucket")
        @Config.RangeInt(min = 1, max = 10000)
        public int ticksPerBucket = 20;

        @Config.Comment("Pumping water doesn't use up the source block")
        public boolean infiniteWater = true;

        @Config.Comment("How far below the pump it looks for liquid")
        @Config.RangeInt(min = 1, max = 128)
        public int range = 48;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 20000;

        @Config.Comment("Storage (mB)")
        @Config.RangeInt(min = 1000, max = 1000000)
        public int tankCapacity = 16000;
    }

    public static class Miner {
        @Config.Comment("Mines a square of (2 * radius + 1) blocks below itself, layer by layer")
        @Config.RangeInt(min = 1, max = 16)
        public int radius = 4;

        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerBlock = 250;

        @Config.RangeInt(min = 1, max = 1000)
        public int ticksPerBlock = 10;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 100000;

        @Config.Comment("Also break blocks that hold items (chests, machines). They lose their contents.")
        public boolean mineTileEntities = false;
    }

    public static class Breaker {
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerBlock = 150;

        @Config.RangeInt(min = 1, max = 1000)
        public int ticksPerBlock = 10;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 50000;

        @Config.Comment("Also break blocks that hold items (chests, machines). They lose their contents.")
        public boolean mineTileEntities = false;
    }

    public static class Placer {
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerBlock = 50;

        @Config.RangeInt(min = 1, max = 1000)
        public int ticksPerBlock = 10;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 20000;
    }

    public static class Grinder {
        @Config.RangeInt(min = 1, max = 100000)
        public int energyPerHit = 200;

        @Config.RangeInt(min = 1, max = 1000)
        public int damage = 6;

        @Config.RangeInt(min = 1, max = 1000)
        public int ticksPerHit = 20;

        @Config.RangeInt(min = 1000, max = 100000000)
        public int capacity = 50000;
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

        @Config.Comment("Energy stored in each piece of Tech Armor")
        @Config.RangeInt(min = 1000, max = 2000000000)
        public int armorCapacity = 500000;
        @Config.RangeInt(min = 1, max = 100000000)
        public int armorTransfer = 4000;
        @Config.Comment("FE per tick the jetpack uses while thrusting")
        @Config.RangeInt(min = 0, max = 100000)
        public int jetpackEnergyPerTick = 25;
        @Config.Comment("FE per block of fall the Tech Boots absorb")
        @Config.RangeInt(min = 0, max = 100000)
        public int bootsEnergyPerBlock = 30;

        @Config.RangeInt(min = 1000, max = 2000000000)
        public int swordCapacity = 300000;
        @Config.RangeInt(min = 0, max = 100000)
        public int swordEnergyPerHit = 150;
        @Config.Comment("Attack damage of a charged Energy Sword (an uncharged one does 1)")
        @Config.RangeInt(min = 1, max = 1000)
        public int swordDamage = 9;

        @Config.RangeInt(min = 1000, max = 2000000000)
        public int magnetCapacity = 100000;
        @Config.RangeInt(min = 1, max = 32)
        public int magnetRange = 6;
        @Config.Comment("FE per tick while the magnet is on, plus 1 per attracted item")
        @Config.RangeInt(min = 0, max = 1000)
        public int magnetEnergyPerTick = 2;

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
