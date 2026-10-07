package com.techcraft.dev;

import com.techcraft.init.ModBlocks;
import com.techcraft.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The actual scenarios; see {@link SelfTest}. */
final class Tests {
    static void register(List<SelfTest.Test> tests) {
        tests.add(new SelfTest.Test("recipes-loaded", c -> { }, c -> {
            List<String> missing = new ArrayList<>();
            for (String name : Arrays.asList("circuit", "machine_frame", "coal_generator", "electric_furnace", "crusher",
                    "battery_box", "compressor", "alloy_furnace", "charger", "lava_generator", "fluid_tank", "wind_turbine",
                    "solar_panel", "solar_panel_advanced", "solar_panel_ultimate", "battery_box_advanced",
                    "battery_box_ultimate", "battery_basic", "battery_advanced", "battery_ultimate", "electric_drill",
                    "chainsaw", "tech_wrench", "energy_meter")) {
                SelfTest.recipeExists(missing, name);
            }
            return missing.isEmpty() ? null : "missing recipes: " + missing;
        }));

        // Coal generator pushes energy into a neighbouring electric furnace, which smelts iron dust.
        tests.add(new SelfTest.Test("generator-powers-furnace", c -> {
            c.machine(ModBlocks.COAL_GENERATOR, EnumFacing.NORTH, 0, 0, 0);
            c.machine(ModBlocks.ELECTRIC_FURNACE, EnumFacing.NORTH, 1, 0, 0);
            c.items(0, 0, 0).setStackInSlot(0, new ItemStack(Items.COAL, 8));
            c.items(1, 0, 0).setStackInSlot(0, new ItemStack(ModItems.IRON_DUST, 2));
        }, c -> c.expectItem(c.items(1, 0, 0), 1, Items.IRON_INGOT, 1)));

        tests.add(new SelfTest.Test("crusher", c -> {
            c.machine(ModBlocks.CRUSHER, EnumFacing.NORTH, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
            c.items(0, 0, 0).setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 2));
        }, c -> c.expectItem(c.items(0, 0, 0), 1, net.minecraft.item.Item.getItemFromBlock(Blocks.GRAVEL), 1)));

        tests.add(new SelfTest.Test("crusher-iron-ingot-to-dust", c -> {
            c.machine(ModBlocks.CRUSHER, EnumFacing.NORTH, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
            c.items(0, 0, 0).setStackInSlot(0, SelfTest.ingotIron());
        }, c -> c.expectItem(c.items(0, 0, 0), 1, ModItems.IRON_DUST, 1)));

        tests.add(new SelfTest.Test("compressor", c -> {
            c.machine(ModBlocks.COMPRESSOR, EnumFacing.NORTH, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
            c.items(0, 0, 0).setStackInSlot(0, SelfTest.ingotIron());
        }, c -> {
            ItemStack out = c.items(0, 0, 0).getStackInSlot(1);
            return out.isEmpty() ? "no plate produced" : (net.minecraftforge.oredict.OreDictionary.getOreIDs(out).length > 0 ? null : "output has no ore dictionary names: " + out);
        }));

        tests.add(new SelfTest.Test("alloy-furnace-steel", c -> {
            c.machine(ModBlocks.ALLOY_FURNACE, EnumFacing.NORTH, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
            IItemHandlerModifiable inv = c.items(0, 0, 0);
            inv.setStackInSlot(0, new ItemStack(Items.COAL, 2));
            inv.setStackInSlot(1, SelfTest.ingotIron());
        }, c -> c.items(0, 0, 0).getStackInSlot(2).isEmpty() ? "no alloy produced" : null));

        tests.add(new SelfTest.Test("solar-panel", c -> c.machine(ModBlocks.SOLAR_PANEL, EnumFacing.NORTH, 0, 0, 0),
                c -> c.expectEnergyAbove(c.energy(0, 0, 0), 0)));

        tests.add(new SelfTest.Test("wind-turbine", c -> c.machine(ModBlocks.WIND_TURBINE, EnumFacing.NORTH, 0, 0, 0),
                c -> c.expectEnergyAbove(c.energy(0, 0, 0), 0)));

        tests.add(new SelfTest.Test("lava-generator", c -> {
            c.machine(ModBlocks.LAVA_GENERATOR, EnumFacing.NORTH, 0, 0, 0);
            c.fluids(0, 0, 0).fill(SelfTest.bucket("lava"), true);
        }, c -> {
            String problem = c.expectEnergyAbove(c.energy(0, 0, 0), 0);
            if (problem != null) {
                return problem;
            }
            return c.fluids(0, 0, 0).fill(new FluidStack(FluidRegistry.WATER, 1000), false) == 0 ? null : "lava generator accepted water";
        }));

        tests.add(new SelfTest.Test("fluid-tank", c -> {
            c.machine(ModBlocks.FLUID_TANK, EnumFacing.NORTH, 0, 0, 0);
            IFluidHandler tank = c.fluids(0, 0, 0);
            tank.fill(new FluidStack(FluidRegistry.WATER, 3000), true);
            tank.drain(1000, true);
        }, c -> {
            FluidStack content = c.fluids(0, 0, 0).getTankProperties()[0].getContents();
            return content != null && content.amount == 2000 ? null : "tank holds " + content;
        }));

        tests.add(new SelfTest.Test("charger-fills-battery", c -> {
            c.machine(ModBlocks.CHARGER, EnumFacing.NORTH, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
            c.items(0, 0, 0).setStackInSlot(0, new ItemStack(ModItems.BATTERY_BASIC));
        }, c -> {
            ItemStack stack = c.items(0, 0, 0).getStackInSlot(0);
            IEnergyStorage battery = stack.getCapability(CapabilityEnergy.ENERGY, null);
            return battery != null && battery.getEnergyStored() > 1000 ? null : "battery holds " + (battery == null ? "no energy capability" : battery.getEnergyStored());
        }));

        tests.add(new SelfTest.Test("battery-box-outputs-front", c -> {
            c.machine(ModBlocks.BATTERY_BOX, EnumFacing.EAST, 0, 0, 0);
            c.machine(ModBlocks.CRUSHER, EnumFacing.NORTH, 1, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(1000, false);
        }, c -> c.expectEnergyAbove(c.energy(1, 0, 0), 0)));

        tests.add(new SelfTest.Test("electric-drill-needs-energy", c -> { }, c -> {
            ItemStack empty = new ItemStack(ModItems.ELECTRIC_DRILL);
            ItemStack full = ((com.techcraft.item.ItemEnergyBase) ModItems.ELECTRIC_DRILL).createFull();
            int emptyLevel = ModItems.ELECTRIC_DRILL.getHarvestLevel(empty, "pickaxe", null, null);
            int fullLevel = ModItems.ELECTRIC_DRILL.getHarvestLevel(full, "pickaxe", null, null);
            return emptyLevel < 0 && fullLevel == 3 ? null : "harvest levels were " + emptyLevel + " / " + fullLevel;
        }));
    }

    private Tests() {
    }
}
