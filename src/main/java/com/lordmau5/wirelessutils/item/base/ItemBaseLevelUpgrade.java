package com.lordmau5.wirelessutils.item.base;

import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.tile.base.IUpgradeable;
import com.lordmau5.wirelessutils.utils.Level;
import com.lordmau5.wirelessutils.utils.mod.ModAdvancements;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;

public abstract class ItemBaseLevelUpgrade extends ItemBaseUpgrade {

    public ItemBaseLevelUpgrade() {
        super();

        setMaxStackSize(16);
        setMaxDamage(0);
        setHasSubtypes(true);
    }

    @Override
    public void initModel() {
        ModelLoader.setCustomMeshDefinition(this, stack -> new ModelResourceLocation(stack.getItem().getRegistryName(), "inventory"));
    }

    @Override
    public int getMetadata(int damage) {
        return damage;
    }

    public Level getLevel(ItemStack stack) {
        return Level.fromInt(stack.getMetadata());
    }

    @Override
    public void onUpgradeInstalled(EntityPlayer player, World world, BlockPos pos, IUpgradeable tile, EnumFacing side, ItemStack stack) {
        super.onUpgradeInstalled(player, world, pos, tile, side, stack);
        if ( !world.isRemote && player instanceof EntityPlayerMP )
            ModAdvancements.UPGRADED.trigger((EntityPlayerMP) player);
    }

    @Override
    public EnumRarity getRarity(ItemStack stack) {
        return getLevel(stack).rarity;
    }

    @Override
    public String getItemStackDisplayName(ItemStack stack) {
        Level level = getLevel(stack);

        return new TextComponentTranslation(
                "info." + WirelessUtils.MODID + ".tiered.name",
                new TextComponentTranslation(getTranslationKey(stack) + ".name"),
                level.getName()
        ).getUnformattedText();
    }

    @Override
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if ( !isInCreativeTab(tab) )
            return;

        Level[] levels = Level.values();
        for (int i = 0; i < levels.length; i++) {
            Level level = levels[i];
            if ( level == Level.getMinLevel() )
                continue;

            items.add(new ItemStack(this, 1, i));
        }
    }
}
