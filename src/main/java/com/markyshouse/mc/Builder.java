package com.markyshouse.mc;

/**
 * Created by mark on 9/16/2015.
 */
public class Builder {
    protected IBlockChooser wallChooser;
    protected IBlockChooser floorChooser;
    protected IBlockChooser windowChooser;
    protected IBlockChooser doorChooser;
    protected IBlockChooser slopeChooser;
    protected IBlockChooser groundsChooser;

    public void build() {}

    public void setWallChooser(IBlockChooser wallChooser) {
        this.wallChooser = wallChooser;
    }
    public void setFloorChooser(IBlockChooser floorChooser) {
        this.floorChooser = floorChooser;
    }
    public void setWindowChooser(IBlockChooser windowChooser) {
        this.windowChooser = windowChooser;
    }
    public void setDoorChooser(IBlockChooser doorChooser) {
        this.doorChooser = doorChooser;
    }
    public void setSlopeChooser(IBlockChooser slopeChooser) {
        this.slopeChooser = slopeChooser;
    }
    public void setGroundsChooser(IBlockChooser groundsChooser) {
        this.groundsChooser = groundsChooser;
    }
}
