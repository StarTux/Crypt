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
    public static boolean areNbors(Area a, Area b) {
        // Check if they touch
        if (a.bx + 1 == b.ax || b.bx + 1 == a.ax) {
            // Horizontal
            return (a.az < b.bz - 1 && a.bz > b.az + 1)
                || (b.az < a.bz - 1 && b.bz > a.az + 1);
        } else if (a.bz + 1 == b.az || b.bz + 1 == a.az) {
            // Vertical
            return (a.ax < b.bx - 1 && a.bx > b.ax + 1)
                || (b.ax < a.bx - 1 && b.bx > a.ax + 1);
        } else {
            return false;
        }
    }
}
