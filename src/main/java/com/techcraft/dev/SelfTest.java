package com.techcraft.dev;

import com.techcraft.TechCraft;
import com.techcraft.block.BlockMachine;
import com.techcraft.init.ModBlocks;
import com.techcraft.init.ModItems;
import com.techcraft.item.ItemEnergyBase;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Runs the machines for real inside a dedicated server and checks the results. Only active when the JVM is started
 * with -Dtechcraft.selftest=true, which the CI smoke test does; it never runs in normal play.
 */
public final class SelfTest {
    private static final int RUN_TICKS = 500;
    private static final int SPACING = 8;

    private static final List<Test> TESTS = new ArrayList<>();
    private static WorldServer world;
    private static int ticks;
    private static boolean running;

    public static boolean enabled() {
        return Boolean.getBoolean("techcraft.selftest");
    }

    /** Called from init so test-only ore dictionary entries exist before the recipes are built. */
    public static void registerTestOres() {
        if (enabled()) {
            OreDictionary.registerOre("ingotSteel", ModItems.PLATES[7]);
        }
    }

    public static void start() {
        world = FMLCommonHandler.instance().getMinecraftServerInstance().getWorld(0);
        world.getGameRules().setOrCreateGameRule("doDaylightCycle", "false");
        world.getGameRules().setOrCreateGameRule("doWeatherCycle", "false");
        world.setWorldTime(1000);

        BlockPos spawn = world.getSpawnPoint();
        Tests.register(TESTS);
        int index = 0;
        for (Test test : TESTS) {
            BlockPos origin = new BlockPos(spawn.getX() + index * SPACING, 100, spawn.getZ());
            test.origin = origin;
            for (int dx = -1; dx <= 5; dx++) {
                for (int dz = -2; dz <= 3; dz++) {
                    world.setBlockState(origin.add(dx, -1, dz), Blocks.STONE.getDefaultState(), 2);
                }
            }
            try {
                test.setup.accept(new Ctx(origin));
            } catch (Throwable t) {
                test.error = "setup threw " + t;
                TechCraft.LOGGER.error("Self-test setup failed", t);
            }
            index++;
        }
        MinecraftForge.EVENT_BUS.register(new SelfTest());
        running = true;
        TechCraft.LOGGER.info("SELFTEST started with {} tests, running {} ticks", TESTS.size(), RUN_TICKS);
    }

    @SubscribeEvent
    public void onTick(TickEvent.ServerTickEvent event) {
        if (!running || event.phase != TickEvent.Phase.END || ++ticks < RUN_TICKS) {
            return;
        }
        running = false;
        int passed = 0;
        for (Test test : TESTS) {
            String failure = test.error;
            if (failure == null) {
                try {
                    failure = test.check.apply(new Ctx(test.origin));
                } catch (Throwable t) {
                    failure = "check threw " + t;
                    TechCraft.LOGGER.error("Self-test check failed", t);
                }
            }
            if (failure == null) {
                passed++;
                TechCraft.LOGGER.info("SELFTEST {}: PASS", test.name);
            } else {
                TechCraft.LOGGER.error("SELFTEST {}: FAIL - {}", test.name, failure);
            }
        }
        TechCraft.LOGGER.info("SELFTEST RESULT: {} ({}/{})", passed == TESTS.size() ? "PASSED" : "FAILED", passed, TESTS.size());
        FMLCommonHandler.instance().getMinecraftServerInstance().initiateShutdown();
    }

    public static final class Test {
        final String name;
        final java.util.function.Consumer<Ctx> setup;
        final Function<Ctx, String> check;
        BlockPos origin;
        String error;

        Test(String name, java.util.function.Consumer<Ctx> setup, Function<Ctx, String> check) {
            this.name = name;
            this.setup = setup;
            this.check = check;
        }
    }

    /** Helpers handed to tests; coordinates are relative to the test's own platform. */
    public static final class Ctx {
        final BlockPos origin;

        Ctx(BlockPos origin) {
            this.origin = origin;
        }

        BlockPos at(int dx, int dy, int dz) {
            return origin.add(dx, dy, dz);
        }

        void place(Block block, int dx, int dy, int dz) {
            world.setBlockState(at(dx, dy, dz), block.getDefaultState(), 3);
        }

        void machine(BlockMachine block, EnumFacing facing, int dx, int dy, int dz) {
            world.setBlockState(at(dx, dy, dz), block.getDefaultState().withProperty(BlockMachine.FACING, facing), 3);
        }

        TileEntity tile(int dx, int dy, int dz) {
            TileEntity tile = world.getTileEntity(at(dx, dy, dz));
            if (tile == null) {
                throw new IllegalStateException("no tile entity at " + dx + "," + dy + "," + dz);
            }
            return tile;
        }

        IItemHandlerModifiable items(int dx, int dy, int dz) {
            return (IItemHandlerModifiable) tile(dx, dy, dz).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        }

        IEnergyStorage energy(int dx, int dy, int dz) {
            return tile(dx, dy, dz).getCapability(CapabilityEnergy.ENERGY, null);
        }

        IFluidHandler fluids(int dx, int dy, int dz) {
            return tile(dx, dy, dz).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, null);
        }

        String expectItem(IItemHandler inventory, int slot, Item item, int minCount) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.getItem() != item || stack.getCount() < minCount) {
                return "slot " + slot + " holds " + stack + ", expected at least " + minCount + " x " + item.getRegistryName();
            }
            return null;
        }

        String expectEnergyAbove(IEnergyStorage energy, int min) {
            return energy.getEnergyStored() > min ? null : "energy is " + energy.getEnergyStored() + ", expected above " + min;
        }
    }

    static void recipeExists(List<String> missing, String name) {
        if (!CraftingManager.REGISTRY.containsKey(new ResourceLocation(TechCraft.MODID, name))) {
            missing.add(name);
        }
    }

    static FluidStack bucket(String fluid) {
        return new FluidStack(FluidRegistry.getFluid(fluid), 1000);
    }

    static Item item(String name) {
        return Item.REGISTRY.getObject(new ResourceLocation(TechCraft.MODID, name));
    }

    static ItemEnergyBase battery() {
        return ModItems.BATTERY_BASIC;
    }

    static ItemStack ingotIron() {
        return new ItemStack(Items.IRON_INGOT);
    }
}
