package com.lordmau5.wirelessutils.item.module;

import cofh.api.core.IAugmentable;
import cofh.core.util.helpers.StringHelper;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.item.base.ItemBaseUpgrade;
import com.lordmau5.wirelessutils.tile.base.Machine;
import com.lordmau5.wirelessutils.tile.vaporizer.TileBaseVaporizer;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.constants.TextHelpers;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public abstract class ItemModule extends ItemBaseUpgrade {

    public ItemModule() {
        super();

        setMaxStackSize(16);
        setMaxDamage(0);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Nonnull
    public Level getRequiredLevel(@Nonnull ItemStack stack) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return Level.getMinLevel();

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("RequiredLevel") )
                return Level.fromInt(itemTag.getByte("RequiredLevel"));
        }

        Level out = getRequiredLevelDelegate(stack);
        if ( out == null )
            return Level.getMinLevel();

        return out;
    }

    @Nullable
    public Level getRequiredLevelDelegate(@Nonnull ItemStack stack) {
        return null;
    }

    public double getEnergyMultiplier(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 1;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyMult") )
                return itemTag.getDouble("EnergyMult");
        }

        return getEnergyMultiplierDelegate(stack, augmentable);
    }

    public double getEnergyMultiplierDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 1;
    }

    public int getEnergyAddition(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyAdd") )
                return itemTag.getInteger("EnergyAdd");
        }

        return getEnergyAdditionDelegate(stack, augmentable);
    }

    public int getEnergyAdditionDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 0;
    }

    public int getEneryDrain(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        if ( stack.isEmpty() || stack.getItem() != this )
            return 0;

        if ( stack.hasTagCompound() ) {
            NBTTagCompound itemTag = stack.getTagCompound();
            if ( itemTag != null && itemTag.hasKey("EnergyDrain") )
                return itemTag.getInteger("EnergyDrain");
        }

        return getEnergyDrainDelegate(stack, augmentable);
    }

    public int getEnergyDrainDelegate(@Nonnull ItemStack stack, @Nullable IAugmentable augmentable) {
        return 0;
    }

    @Override
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);

        Level minLevel = getRequiredLevel(stack);
        if ( !minLevel.equals(Level.getMinLevel()) ) {
            tooltip.add(new TextComponentTranslation(
                    "item." + WirelessUtils.MODID + ".augment.min_level",
                    minLevel.getTextComponent()
            ).getFormattedText());
        }

        double multiplier = getEnergyMultiplier(stack, null);
        int addition = getEnergyAddition(stack, null);
        int drain = getEneryDrain(stack, null);

        if ( multiplier != 1 || addition != 0 || drain != 0 ) {
            tooltip.add(StringHelper.localize("item." + WirelessUtils.MODID + ".augment.energy"));

            if ( drain != 0 )
                tooltip.add(new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".augment.energy.entry",
                        !StringHelper.isShiftKeyDown() && drain >= 1000 ? TextHelpers.getScaledNumber(drain, "RF/t", true) : StringHelper.formatNumber(drain) + " RF/t"
                ).getFormattedText());

            if ( multiplier != 1 || addition != 0 ) {
                tooltip.add(new TextComponentTranslation(
                        "item." + WirelessUtils.MODID + ".augment.energy.entry",
                        new TextComponentTranslation(
                                "item." + WirelessUtils.MODID + ".augment.energy.action",
                                String.format("%.2f", multiplier),
                                StringHelper.isShiftKeyDown() ? StringHelper.formatNumber(addition) : TextHelpers.getScaledNumber(addition, "", true)
                        )
                ).getFormattedText());
            }
        }
    }

    public boolean canApplyTo(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer) {
        if ( stack.hasTagCompound() ) {
            NBTTagCompound tag = stack.getTagCompound();
            if ( tag != null && tag.hasKey("AllowedMachines", Constants.NBT.TAG_LIST) ) {
                Machine machine = vaporizer.getClass().getAnnotation(Machine.class);
                if ( machine == null )
                    return false;

                String name = machine.name();
                NBTTagList list = tag.getTagList("AllowedMachines", Constants.NBT.TAG_STRING);
                boolean allowed = false;
                for (int i = 0; i < list.tagCount(); i++) {
                    if ( name.equals(list.getStringTagAt(i)) ) {
                        allowed = true;
                        break;
                    }
                }

                if ( !allowed )
                    return false;
            }
        }

        return canApplyToDelegate(stack, vaporizer);
    }

    public abstract boolean canApplyToDelegate(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer);

    @Nullable
    public abstract TileBaseVaporizer.IVaporizerBehavior getBehavior(@Nonnull ItemStack stack, @Nonnull TileBaseVaporizer vaporizer);
}
