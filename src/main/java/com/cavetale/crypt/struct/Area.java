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
}
