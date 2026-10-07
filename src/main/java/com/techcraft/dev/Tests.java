package com.techcraft.dev;

import com.techcraft.init.ModBlocks;
import com.techcraft.tile.FaceMode;
import com.techcraft.tile.SideConfig;
import com.techcraft.tile.TileMachineBase;
import com.techcraft.init.ModItems;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
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
                    "battery_box_ultimate", "pump", "geothermal_generator", "water_wheel", "biomass_generator", "auto_miner", "block_breaker", "block_placer", "mob_grinder", "upgrade_speed", "upgrade_efficiency", "battery_basic", "battery_advanced", "battery_ultimate", "electric_drill",
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

        // Side configuration: disabled faces expose no capability, input-only faces can't be drained.
        tests.add(new SelfTest.Test("side-config-energy", c -> {
            c.machine(ModBlocks.BATTERY_BOX, EnumFacing.NORTH, 0, 0, 0);
            TileMachineBase battery = (TileMachineBase) c.tile(0, 0, 0);
            battery.getSideConfig().set(SideConfig.ENERGY, EnumFacing.EAST, FaceMode.DISABLED);
            battery.getSideConfig().set(SideConfig.ENERGY, EnumFacing.WEST, FaceMode.OUTPUT);
        }, c -> {
            TileMachineBase battery = (TileMachineBase) c.tile(0, 0, 0);
            if (battery.hasCapability(CapabilityEnergy.ENERGY, EnumFacing.EAST)) {
                return "disabled face still has the energy capability";
            }
            IEnergyStorage west = battery.getCapability(CapabilityEnergy.ENERGY, EnumFacing.WEST);
            if (west == null || !west.canExtract() || west.canReceive()) {
                return "output face should only extract";
            }
            IEnergyStorage front = battery.getCapability(CapabilityEnergy.ENERGY, EnumFacing.NORTH);
            if (front == null || !front.canExtract() || front.canReceive()) {
                return "default front should only extract";
            }
            IEnergyStorage back = battery.getCapability(CapabilityEnergy.ENERGY, EnumFacing.SOUTH);
            return back != null && back.canReceive() && !back.canExtract() ? null : "default back should only receive";
        }));

        // A crusher face set to OUTPUT pushes finished items into a chest by itself.
        tests.add(new SelfTest.Test("item-eject-into-chest", c -> {
            c.machine(ModBlocks.CRUSHER, EnumFacing.NORTH, 0, 0, 0);
            c.place(Blocks.CHEST, 1, 0, 0);
            ((TileMachineBase) c.tile(0, 0, 0)).getSideConfig().set(SideConfig.ITEMS, EnumFacing.EAST, FaceMode.OUTPUT);
            c.items(0, 0, 0).setStackInSlot(1, new ItemStack(Blocks.GRAVEL, 5));
        }, c -> {
            net.minecraftforge.items.IItemHandler chest = c.tile(1, 0, 0).getCapability(
                    net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.WEST);
            int total = 0;
            for (int i = 0; i < chest.getSlots(); i++) {
                total += chest.getStackInSlot(i).getCount();
            }
            return total == 5 ? null : "chest holds " + total + " items, expected 5";
        }));

        // Speed upgrades make a furnace finish more items in the same time. Both furnaces are fed by battery boxes.
        tests.add(new SelfTest.Test("speed-upgrades", c -> {
            for (int row = 0; row < 2; row++) {
                int z = row * 2;
                c.machine(ModBlocks.BATTERY_BOX, EnumFacing.EAST, 0, 0, z);
                c.machine(ModBlocks.ELECTRIC_FURNACE, EnumFacing.NORTH, 1, 0, z);
                for (int i = 0; i < 300; i++) {
                    c.energy(0, 0, z).receiveEnergy(1000, false);
                }
                c.items(1, 0, z).setStackInSlot(0, new ItemStack(ModItems.IRON_DUST, 20));
            }
            ((TileMachineBase) c.tile(1, 0, 2)).getUpgradeInventory().setStackInSlot(0, new ItemStack(ModItems.UPGRADE_SPEED, 4));
        }, c -> {
            int slow = c.items(1, 0, 0).getStackInSlot(1).getCount();
            int fast = c.items(1, 0, 2).getStackInSlot(1).getCount();
            return fast > slow && slow > 0 ? null : "plain furnace made " + slow + ", upgraded one made " + fast;
        }));

        // Efficiency upgrades lower the energy used for the same work.
        tests.add(new SelfTest.Test("efficiency-upgrades", c -> {
            for (int row = 0; row < 2; row++) {
                int z = row * 2;
                c.machine(ModBlocks.CRUSHER, EnumFacing.NORTH, 0, 0, z);
                c.energy(0, 0, z).receiveEnergy(20000, false);
                c.items(0, 0, z).setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 1));
            }
            ((TileMachineBase) c.tile(0, 0, 2)).getUpgradeInventory().setStackInSlot(0, new ItemStack(ModItems.UPGRADE_EFFICIENCY, 4));
        }, c -> {
            int plain = c.energy(0, 0, 0).getEnergyStored();
            int efficient = c.energy(0, 0, 2).getEnergyStored();
            return efficient > plain ? null : "plain crusher has " + plain + " FE left, efficient one " + efficient;
        }));

        // Pump: draws water from the block below and pushes it into the tank next to it.
        tests.add(new SelfTest.Test("pump-fills-neighbour-tank", c -> {
            c.machine(ModBlocks.PUMP, EnumFacing.NORTH, 0, 0, 0);
            c.machine(ModBlocks.FLUID_TANK, EnumFacing.NORTH, 1, 0, 0);
            c.place(Blocks.WATER, 0, -1, 0);
            c.energy(0, 0, 0).receiveEnergy(100000, false);
        }, c -> {
            FluidStack content = c.fluids(1, 0, 0).getTankProperties()[0].getContents();
            return content != null && content.getFluid() == FluidRegistry.WATER && content.amount >= 1000 ? null : "tank holds " + content;
        }));

        tests.add(new SelfTest.Test("geothermal-generator", c -> {
            c.machine(ModBlocks.GEOTHERMAL_GENERATOR, EnumFacing.NORTH, 0, 0, 0);
            c.place(Blocks.LAVA, 0, -1, 0);
        }, c -> c.expectEnergyAbove(c.energy(0, 0, 0), 0)));

        tests.add(new SelfTest.Test("water-wheel", c -> {
            c.machine(ModBlocks.WATER_WHEEL, EnumFacing.NORTH, 0, 0, 0);
            c.place(Blocks.WATER, 1, 3, 0);
        }, c -> c.expectEnergyAbove(c.energy(0, 0, 0), 0)));

        tests.add(new SelfTest.Test("biomass-generator", c -> {
            c.machine(ModBlocks.BIOMASS_GENERATOR, EnumFacing.NORTH, 0, 0, 0);
            c.items(0, 0, 0).setStackInSlot(0, new ItemStack(Items.WHEAT, 4));
        }, c -> c.expectEnergyAbove(c.energy(0, 0, 0), 0)));

        tests.add(new SelfTest.Test("biomass-rejects-meat", c -> c.machine(ModBlocks.BIOMASS_GENERATOR, EnumFacing.NORTH, 0, 0, 0), c -> {
            net.minecraftforge.items.IItemHandler inv = c.items(0, 0, 0);
            boolean meat = inv.insertItem(0, new ItemStack(Items.BEEF, 1), true).isEmpty();
            boolean wheat = inv.insertItem(0, new ItemStack(Items.WHEAT, 1), true).isEmpty();
            return !meat && wheat ? null : "meat accepted: " + meat + ", wheat accepted: " + wheat;
        }));

        // The miner digs the stone platform under it and stores the cobblestone.
        tests.add(new SelfTest.Test("auto-miner", c -> {
            c.machine(ModBlocks.AUTO_MINER, EnumFacing.NORTH, 2, 0, 0);
            c.energy(2, 0, 0).receiveEnergy(100000, false);
        }, c -> {
            net.minecraftforge.items.IItemHandler inv = c.items(2, 0, 0);
            int total = 0;
            for (int i = 0; i < inv.getSlots(); i++) {
                total += inv.getStackInSlot(i).getCount();
            }
            return total > 0 ? null : "miner collected nothing";
        }));

        tests.add(new SelfTest.Test("block-breaker", c -> {
            c.machine(ModBlocks.BLOCK_BREAKER, EnumFacing.EAST, 0, 0, 0);
            c.place(Blocks.COBBLESTONE, 1, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(50000, false);
        }, c -> {
            if (!c.isAir(1, 0, 0)) {
                return "block in front was not broken";
            }
            return c.expectItem(c.items(0, 0, 0), 0, net.minecraft.item.Item.getItemFromBlock(Blocks.COBBLESTONE), 1);
        }));

        tests.add(new SelfTest.Test("block-placer", c -> {
            c.machine(ModBlocks.BLOCK_PLACER, EnumFacing.EAST, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(20000, false);
            c.items(0, 0, 0).setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 3));
        }, c -> c.isAir(1, 0, 0) ? "nothing was placed in front" : null));

        tests.add(new SelfTest.Test("mob-grinder", c -> {
            c.machine(ModBlocks.MOB_GRINDER, EnumFacing.EAST, 0, 0, 0);
            c.energy(0, 0, 0).receiveEnergy(50000, false);
            net.minecraft.entity.passive.EntityCow cow = new net.minecraft.entity.passive.EntityCow(c.world());
            BlockPos at = c.at(3, 0, 0);
            cow.setPosition(at.getX() + 0.5, at.getY(), at.getZ() + 0.5);
            c.world().spawnEntity(cow);
        }, c -> {
            net.minecraftforge.items.IItemHandler inv = c.items(0, 0, 0);
            for (int i = 0; i < inv.getSlots(); i++) {
                if (!inv.getStackInSlot(i).isEmpty()) {
                    return null;
                }
            }
            return "grinder collected no drops";
        }));

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
