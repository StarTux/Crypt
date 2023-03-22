package com.cavetale.crypt.struct;

import com.cavetale.core.struct.Vec2i;
import java.util.ArrayList;
import java.util.List;
import lombok.Value;

@Value
public final class Area {
    public final int ax;
    public final int az;
    public final int bx;
    public final int bz;

    public Vec2i getMin() {
        return new Vec2i(ax, az);
    }

    public Vec2i getMax() {
        return new Vec2i(bx, bz);
    }

    public int getSizeX() {
        return bx - ax + 1;
    }

    public int getSizeZ() {
        return bz - az + 1;
    }

    public int getArea() {
        return getSizeX() * getSizeZ();
    }

    public boolean contains(Vec2i vec) {
        return ax <= vec.x && vec.x <= bx
            && az <= vec.z && vec.z <= bz;
    }

    public boolean contains(int x, int z) {
        return ax <= x && x <= bx
            && az <= z && z <= bz;
    }

    public List<Vec2i> getWalls() {
        List<Vec2i> result = new ArrayList<>();
        for (int x = ax; x <= bx; x += 1) {
            result.add(new Vec2i(x, az));
            result.add(new Vec2i(x, bz));
        }
        for (int z = az + 1; z < bz; z += 1) {
            result.add(new Vec2i(ax, z));
            result.add(new Vec2i(bx, z));
        }
        return result;
    }

    /**
     * Two areas are neighbors if they are able to share a set of
     * matching doorways.
     * So, they need to be right next to each other, and have
     * overlapping non-wall surface.
     */
    public static boolean areNbors(int overlap, Area a, Area b) {
        // Check if they touch
        if (a.bx + 1 == b.ax || b.bx + 1 == a.ax) {
            // Horizontal
            return (a.az <= b.bz - overlap && a.bz >= b.az + overlap)
                || (b.az <= a.bz - overlap && b.bz >= a.az + overlap);
        } else if (a.bz + 1 == b.az || b.bz + 1 == a.az) {
            // Vertical
            return (a.ax <= b.bx - overlap && a.bx >= b.ax + overlap)
                || (b.ax <= a.bx - overlap && b.bx >= a.ax + overlap);
        } else {
            return false;
        }
    }

    public static Area getBoundingBox(List<Area> areas) {
        int ax = areas.get(0).ax;
        int az = areas.get(0).az;
        int bx = areas.get(0).bx;
        int bz = areas.get(0).bz;
        for (int i = 1; i < areas.size(); i += 1) {
            Area area = areas.get(i);
            ax = Math.min(area.ax, ax);
            az = Math.min(area.az, az);
            bx = Math.max(area.bx, bx);
            bz = Math.max(area.bz, bz);
        }
        return new Area(ax, az, bx, bz);
    }
}
