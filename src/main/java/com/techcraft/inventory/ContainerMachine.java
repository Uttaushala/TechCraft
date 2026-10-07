package com.techcraft.inventory;

import com.techcraft.item.ItemUpgrade;
import com.techcraft.tile.SideConfig;
import com.techcraft.tile.TileMachineBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ContainerMachine extends Container {
    private final TileMachineBase tile;
    private final int machineSlots;
    private final int inputSlots;
    private final int upgradeSlots;
    /** Server: last values sent. Client: values received from the server. */
    private final int[] fields = new int[TileMachineBase.FIELD_COUNT];

    public ContainerMachine(InventoryPlayer playerInventory, TileMachineBase tile) {
        this.tile = tile;
        this.inputSlots = tile.getInputSlotCount();

        int count = 0;
        for (Slot slot : tile.createSlots()) {
            addSlotToContainer(slot);
            count++;
        }
        for (Slot slot : tile.createUpgradeSlots()) {
            addSlotToContainer(slot);
            count++;
        }
        this.machineSlots = count;
        this.upgradeSlots = tile.getUpgradeSlotCount();

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInventory, col, 8 + col * 18, 142));
        }

        if (!tile.getWorld().isRemote) {
            for (int i = 0; i < fields.length; i++) {
                fields[i] = tile.getField(i);
            }
        }
    }

    public TileMachineBase getTile() {
        return tile;
    }

    public int getField(int id) {
        return fields[id];
    }

    @Override
    public void addListener(IContainerListener listener) {
        super.addListener(listener);
        for (int i = 0; i < fields.length; i++) {
            sendField(listener, i, fields[i]);
        }
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (int i = 0; i < fields.length; i++) {
            int value = tile.getField(i);
            if (value != fields[i]) {
                fields[i] = value;
                for (IContainerListener listener : listeners) {
                    sendField(listener, i, value);
                }
            }
        }
    }

    /** Window properties are sent as shorts, so every int is split into two 16-bit halves. */
    private void sendField(IContainerListener listener, int id, int value) {
        listener.sendWindowProperty(this, id * 2, value & 0xFFFF);
        listener.sendWindowProperty(this, id * 2 + 1, value >>> 16);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int data) {
        int field = id / 2;
        if (field >= fields.length) {
            return;
        }
        int current = fields[field];
        if (id % 2 == 0) {
            fields[field] = (current & 0xFFFF0000) | (data & 0xFFFF);
        } else {
            fields[field] = (current & 0xFFFF) | ((data & 0xFFFF) << 16);
        }
    }

    /** Side-configuration buttons in the GUI send ids 100 + resource * 6 + face index. */
    @Override
    public boolean enchantItem(EntityPlayer player, int id) {
        int index = id - 100;
        if (index < 0 || index >= SideConfig.RESOURCES * 6) {
            return false;
        }
        if (!player.world.isRemote) {
            tile.cycleSide(index / 6, tile.uiFace(index % 6));
        }
        return true;
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return !tile.isInvalid()
                && tile.getWorld().getTileEntity(tile.getPos()) == tile
                && player.getDistanceSq(tile.getPos().getX() + 0.5, tile.getPos().getY() + 0.5, tile.getPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        Slot slot = inventorySlots.get(index);
        if (slot == null || !slot.getHasStack()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getStack();
        ItemStack original = stack.copy();
        int playerStart = machineSlots;
        int playerEnd = machineSlots + 36;

        if (index < machineSlots) {
            if (!mergeItemStack(stack, playerStart, playerEnd, true)) {
                return ItemStack.EMPTY;
            }
        } else if (upgradeSlots > 0 && stack.getItem() instanceof ItemUpgrade) {
            if (!mergeItemStack(stack, machineSlots - upgradeSlots, machineSlots, false)) {
                return ItemStack.EMPTY;
            }
        } else if (inputSlots > 0 && inventorySlots.get(0).isItemValid(stack)) {
            if (!mergeItemStack(stack, 0, inputSlots, false)) {
                return ItemStack.EMPTY;
            }
        } else if (index < playerStart + 27) {
            if (!mergeItemStack(stack, playerStart + 27, playerEnd, false)) {
                return ItemStack.EMPTY;
            }
        } else if (!mergeItemStack(stack, playerStart, playerStart + 27, false)) {
            return ItemStack.EMPTY;
        }

        if (stack.isEmpty()) {
            slot.putStack(ItemStack.EMPTY);
        } else {
            slot.onSlotChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }
}
