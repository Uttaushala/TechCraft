package com.techcraft.compat.jei;

import com.techcraft.TechCraft;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IDrawable;
import mezz.jei.api.gui.IDrawableAnimated;
import mezz.jei.api.gui.IDrawableStatic;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

import java.util.function.IntSupplier;

/** One recipe screen shared by the crusher, compressor and alloy furnace (one or two inputs, one output). */
public class MachineCategory extends BlankRecipeCategory<MachineWrapper> {
    private final String uid;
    private final String titleKey;
    private final int inputs;
    private final IntSupplier totalEnergy;
    private final IDrawable background;
    private final IDrawableAnimated arrow;

    public MachineCategory(IGuiHelper gui, String uid, String titleKey, int inputs, IntSupplier totalEnergy) {
        this.uid = uid;
        this.titleKey = titleKey;
        this.inputs = inputs;
        this.totalEnergy = totalEnergy;
        this.background = gui.createBlankDrawable(120, 46);
        IDrawableStatic arrowShape = gui.createDrawable(
                new ResourceLocation("minecraft", "textures/gui/container/furnace.png"), 176, 14, 24, 17);
        this.arrow = gui.createAnimatedDrawable(arrowShape, 100, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public String getUid() {
        return uid;
    }

    @Override
    public String getTitle() {
        return I18n.format(titleKey);
    }

    @Override
    public String getModName() {
        return TechCraft.NAME;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public void drawExtras(Minecraft minecraft) {
        arrow.draw(minecraft, 50, 10);
        String energy = String.format("%,d FE", totalEnergy.getAsInt());
        minecraft.fontRenderer.drawString(energy, (120 - minecraft.fontRenderer.getStringWidth(energy)) / 2, 36, 0x808080);
    }

    @Override
    public void setRecipe(IRecipeLayout layout, MachineWrapper wrapper, IIngredients ingredients) {
        IGuiItemStackGroup stacks = layout.getItemStacks();
        if (inputs == 1) {
            stacks.init(0, true, 18, 5);
        } else {
            stacks.init(0, true, 18, 0);
            stacks.init(1, true, 18, 18);
        }
        stacks.init(inputs, false, 84, 5);
        stacks.set(ingredients);
    }
}
