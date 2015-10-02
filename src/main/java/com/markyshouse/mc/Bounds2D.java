package com.markyshouse.mc;

/**
 * Created by mark on 9/17/2015.
 */
public class Bounds2D {
    // x: west <--> east
    // y: north <--> south
    private int _east;
    private int _west;
    private int _north;
    private int _south;

    public Bounds2D(int east, int west, int north, int south) {
        _east = east;
        _west = west;
        _north = north;
        _south = south;
    }
/*
    public Bounds2D(int west, int north, int width, int height) {
        _west = west;
        _north = north;
        _east = _west + width;
        _south = _north + height;
    }
    */
    public boolean intersects(Bounds2D other) {
        if (other._south < _north || other._north > _south) {
            return false;
        }
        if (other._east < _west || other._west > _east) {
            return false;
        }
        return true;
    }
}
