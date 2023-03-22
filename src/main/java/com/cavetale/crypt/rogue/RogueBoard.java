package com.cavetale.crypt.rogue;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import com.cavetale.core.struct.Vec2i;

/**
 * A grid to store tiles on.
 */
public final class RogueBoard {
    @Getter protected final Vec2i size;
    protected final RogueTile[] tiles;
    protected final int[] indexes;

    public RogueBoard(final int width, final int height) {
        this.size = new Vec2i(width, height);
        this.tiles = new RogueTile[width * height];
        this.indexes = new int[width * height];
        for (int i = 0; i < tiles.length; i += 1) {
            tiles[i] = RogueTile.UNDEFINED;
        }
    }

    public RogueTile getTile(int x, int z) {
        return tiles[x + z * size.x];
    }

    public void setTile(int x, int z, RogueTile tile) {
        if (x < 0 || x > size.x || z < 0 | z > size.z) {
            throw new IllegalArgumentException(x + "," + z + " / " + size);
        }
        tiles[x + z * size.x] = tile;
    }

    public int getIndex(int x, int z) {
        return indexes[x + z * size.x];
    }

    public void setIndex(int x, int z, int value) {
        if (x < 0 || x > size.x || z < 0 | z > size.z) {
            throw new IllegalArgumentException(x + "," + z + " / " + size);
        }
        indexes[x + z * size.x] = value;
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
                if (tile.isWall()) {
                    chr = tile.getCharacter();
                } else if (tile.isFloor()) {
                    chr = toChar(getIndex(x, z));
                } else if (tile.isDoor()) {
                    chr = tile.getCharacter();
                } else {
                    chr = '?';
                }
                String line = lines.get(z);
                line = line.substring(0, x) + chr + line.substring(x + 1, line.length());
                lines.set(z, line);
            }
        }
        return String.join("\n", lines);
    }

    private static char toChar(int index) {
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
}
