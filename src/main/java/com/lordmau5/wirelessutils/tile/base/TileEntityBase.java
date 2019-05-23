package com.lordmau5.wirelessutils.tile.base;

import cofh.core.block.TileRSControl;
import com.lordmau5.wirelessutils.WirelessUtils;
import com.lordmau5.wirelessutils.utils.EventDispatcher;
import com.lordmau5.wirelessutils.utils.constants.Properties;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.IWorldNameable;
import net.minecraft.world.World;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;

import javax.annotation.Nonnull;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public abstract class TileEntityBase extends TileRSControl implements EventDispatcher.IEventListener, IWorldNameable {
    private boolean watchUnload = false;

    private final Map<String, String> MODEL_PROPERTIES = new HashMap<>();

    @Override
    protected Object getMod() {
        return WirelessUtils.instance;
    }

    @Override
    protected String getModVersion() {
        return WirelessUtils.VERSION;
    }

    public void callNeighborStateChange(EnumFacing facing) {
        if ( world == null || pos == null )
            return;

        if ( ForgeEventFactory.onNeighborNotify(world, pos, world.getBlockState(pos), EnumSet.of(facing), false).isCanceled() )
            return;

        world.neighborChanged(pos.offset(facing), getBlockType(), pos);
    }

    @Override
    public void callNeighborStateChange() {
        if ( world == null || pos == null )
            return;

        super.callNeighborStateChange();
    }

    @Override
    public void callNeighborTileChange() {
        if ( world == null || pos == null )
            return;

        super.callNeighborTileChange();
    }

    /* Life Cycle */

    @Override
    public void validate() {
        super.validate();
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.addListener(world.provider.getDimension(), 0, 0, this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        onDestroy();
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        if ( !oldState.getBlock().equals(newState.getBlock()) )
            return super.shouldRefresh(world, pos, oldState, newState);

        onRefresh(world, pos, oldState, newState);
        return false;
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        onDestroy();
    }

    @Override
    protected void finalize() throws Throwable {
        onDestroy();
        super.finalize();
    }

    @Override
    public void handleEvent(@Nonnull Event event) {
        if ( event instanceof WorldEvent.Unload ) {
            onDestroy();
        }
    }

    /* Life Cycle Events */

    public void onDestroy() {
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.removeListener(world.provider.getDimension(), 0, 0, this);
    }

    protected void onRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {

    }

    /* Watch for World Unload */
    public void setWatchUnload() {
        setWatchUnload(true);
    }

    public void setWatchUnload(boolean watch) {
        if ( watch == watchUnload )
            return;

        watchUnload = watch;
        if ( !isInvalid() && world != null && world.provider != null ) {
            int dimension = world.provider.getDimension();
            if ( watchUnload )
                EventDispatcher.WORLD_UNLOAD.addListener(dimension, 0, 0, this);
            else
                EventDispatcher.WORLD_UNLOAD.removeListener(dimension, 0, 0, this);
        }

    }

    @Override
    public void setWorld(World worldIn) {
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.removeListener(world.provider.getDimension(), 0, 0, this);
        super.setWorld(worldIn);
        if ( watchUnload && world != null && world.provider != null )
            EventDispatcher.WORLD_UNLOAD.addListener(world.provider.getDimension(), 0, 0, this);
    }

    /* IWorldNameable */

    @Override
    public String getName() {
        return customName.isEmpty() ? getTileName() : customName;
    }

    @Override
    public boolean hasCustomName() {
        return !customName.isEmpty();
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TextComponentString(getName());
    }

    /* Client <-> Server Sync Methods */

    // TODO: Reevaluate these three methods.
    public void notifyBlockUpdate() {
        final IBlockState state = getWorld().getBlockState(getPos());
        getWorld().notifyBlockUpdate(getPos(), state, state, 3);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        notifyBlockUpdate();
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        super.onDataPacket(net, pkt);

        notifyBlockUpdate();
    }

    /* NBT Save and Load */
    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        readExtraFromNBT(tag);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        writeExtraToNBT(tag);
        return tag;
    }

    public NBTTagCompound writeExtraToNBT(NBTTagCompound tag) {
        return tag;
    }

    public void readExtraFromNBT(NBTTagCompound tag) {
    }

    public void setProperty(String key, String value) {
        synchronized (MODEL_PROPERTIES) {
            MODEL_PROPERTIES.put(key, value);
        }
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        state = super.getExtendedState(state, world, pos);
        if ( state instanceof IExtendedBlockState ) {
            synchronized (MODEL_PROPERTIES) {
                state = ((IExtendedBlockState) state).withProperty(Properties.MODEL_PROPERTIES, new HashMap<>(MODEL_PROPERTIES));
            }
        }
        return state;
    }
}
