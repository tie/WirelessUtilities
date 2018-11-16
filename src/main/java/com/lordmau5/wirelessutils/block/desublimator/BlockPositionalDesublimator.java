package com.lordmau5.wirelessutils.block.desublimator;

import com.lordmau5.wirelessutils.block.base.BlockBaseMachine;
import com.lordmau5.wirelessutils.tile.desublimator.TilePositionalDesublimator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockPositionalDesublimator extends BlockBaseMachine {
    public BlockPositionalDesublimator() {
        super();

        setName("positional_desublimator");
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TilePositionalDesublimator();
    }
}
