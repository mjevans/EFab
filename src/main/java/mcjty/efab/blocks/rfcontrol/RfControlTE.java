package mcjty.efab.blocks.rfcontrol;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import mcjty.efab.blocks.GenericEFabTile;
import mcjty.efab.blocks.IEFabEnergyStorage;
import mcjty.efab.config.GeneralConfiguration;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({
        @Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyProvider", modid = "redstoneflux"),
        @Optional.Interface(iface = "cofh.redstoneflux.api.IEnergyReceiver", modid = "redstoneflux")
})
public class RfControlTE extends GenericEFabTile implements IEnergyProvider, IEnergyReceiver, IEFabEnergyStorage, ITickable {

    private int energy = 0;

    // Client side only
    private int sparks = -1;

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        energy = tagCompound.getInteger("energy");
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("energy", energy);
    }

    @Override
    public boolean extractEnergy(int amount) {
        if (amount > energy) {
            return false;
        }
        energy -= amount;
        markDirtyQuick();
        return true;
    }

    @Override
    public void update() {
        if (getWorld().isRemote) {
            if (sparks > 0) {
                sparks--;
            } else if (sparks == 0) {
                sparks--;
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        }
    }

    // Client side only
    public boolean hasSpark() {
        return sparks >= 0;
    }

    public void setSpark(int s) {
        sparks = s;
        getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
    }

    @Override
    public int getMaxInternalConsumption() {
        return GeneralConfiguration.rfControlMax;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        return receiveEnergyInternal(maxReceive, simulate);
    }

    private int receiveEnergyInternal(int maxReceive, boolean simulate) {
        int toreceive = Math.min(maxReceive, GeneralConfiguration.rfControlInputPerTick);
        int newenergy = energy + toreceive;
        if (newenergy > GeneralConfiguration.rfControlMax) {
            toreceive -= newenergy - GeneralConfiguration.rfControlMax;
            newenergy = GeneralConfiguration.rfControlMax;
        }
        if (!simulate && energy != newenergy) {
            energy = newenergy;
            markDirtyQuick();
        }
        return toreceive;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int getEnergyStored(EnumFacing from) {
        return energy;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return GeneralConfiguration.rfControlMax;
    }

    @Optional.Method(modid = "redstoneflux")
    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return from != EnumFacing.UP;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    private IEnergyStorage handler = null;

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            if (handler == null) {
                handler = new IEnergyStorage() {
                    @Override
                    public int receiveEnergy(int maxReceive, boolean simulate) {
                        return receiveEnergyInternal(maxReceive, simulate);
                    }

                    @Override
                    public int extractEnergy(int maxExtract, boolean simulate) {
                        return 0;
                    }

                    @Override
                    public int getEnergyStored() {
                        return RfControlTE.this.energy;
                    }

                    @Override
                    public int getMaxEnergyStored() {
                        return GeneralConfiguration.rfControlMax;
                    }

                    @Override
                    public boolean canExtract() {
                        return false;
                    }

                    @Override
                    public boolean canReceive() {
                        return true;
                    }
                };
            }
            return CapabilityEnergy.ENERGY.cast(handler);
        }
        return super.getCapability(capability, facing);
    }
}
