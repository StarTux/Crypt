package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.struct.Vec3i;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;

/**
 * A grid to store tiles on.
 */
public final class RogueBoard {
    @Getter protected final Vec2i size;
    protected final RogueTile[] tiles;
    protected final char[] chars;

    public RogueBoard(final int width, final int height) {
        this.size = new Vec2i(width, height);
        this.tiles = new RogueTile[width * height];
        this.chars = new char[width * height];
        for (int i = 0; i < tiles.length; i += 1) {
            tiles[i] = RogueTile.UNDEFINED;
        }
    }

    public RogueTile getTile(int x, int z) {
        if (x < 0 || x >= size.x || z < 0 | z >= size.z) {
            return RogueTile.UNDEFINED;
        }
        return tiles[x + z * size.x];
    }

    public void setTile(int x, int z, RogueTile tile) {
        if (x < 0 || x >= size.x || z < 0 | z >= size.z) {
            throw new IllegalArgumentException(x + "," + z + " / " + size);
        }
        tiles[x + z * size.x] = tile;
    }

    public char getChar(int x, int z) {
        return chars[x + z * size.x];
    }

    public void setChar(int x, int z, char value) {
        if (x < 0 || x > size.x || z < 0 | z > size.z) {
            throw new IllegalArgumentException(x + "," + z + " / " + size);
        }
        chars[x + z * size.x] = value;
    }

    public String toMultiLineString() {
        List<String> lines = new ArrayList<>();
        for (int z = 0; z < size.z; z += 1) {
            String line = "";
            for (int x = 0; x < size.x; x += 1) {
                line += " ";
            }
            lines.add(line);
        }
        for (int z = 0; z < size.z; z += 1) {
            for (int x = 0; x < size.x; x += 1) {
                RogueTile tile = getTile(x, z);
                if (tile.isUndefined()) continue;
                final char chr;
                char customChar = getChar(x, z);
                if (customChar > 0) {
                    chr = customChar;
                } else {
                    chr = tile.character;
                }
                String line = lines.get(z);
                line = line.substring(0, x) + chr + line.substring(x + 1, line.length());
                lines.set(z, line);
            }
        }
        return String.join("\n", lines);
    }

    public static char toChar(int index) {
        if (index < 10) {
            return (char) ('0' + index);
        } else if (index < 10 + 26) {
            return (char) ('a' + (index - 10));
        } else if (index < 10 + 26 + 26) {
            return (char) ('A' + (index - 10 - 26));
        } else {
            return '.';
        }
    }

    /**
     * Return vector with column, line, size.
     * In other words: x, z, width.
     */
    public Vec3i getWidestLine() {
        Vec3i result = Vec3i.ZERO;
        int minDist = size.z;
        for (int z = 0; z < size.z; z += 1) {
            int width = 0;
            for (int x = size.x - 1; x >= 0; x -= 1) {
                if (getTile(x, z).isFloor()) {
                    width += 1;
                } else {
                    int dist = Math.abs((size.z - 1) / 2 - z);
                    if (width > result.z || (width == result.z && dist < minDist)) {
                        minDist = dist;
                        result = new Vec3i(x + 1, z, width);
                    }
                    width = 0;
                }
            }
        }
        return result;
    }
}
