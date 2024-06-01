package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import java.util.ArrayList;
import java.util.List;

public enum RogueRoomFeature {
    PIT {
        @Override public boolean place(RogueGenerator gen, RogueRoom room) {
            return placeHole(room, 2, RogueTile.PIT);
        }
    },
    WATER {
        @Override public boolean place(RogueGenerator gen, RogueRoom room) {
            return placeHole(room, 3, RogueTile.WATER);
        }
    },
    LAVA {
        @Override public boolean place(RogueGenerator gen, RogueRoom room) {
            return placeHole(room, 3, RogueTile.LAVA);
        }
    },
    ;

    public abstract boolean place(RogueGenerator gen, RogueRoom room);

    protected boolean placeHole(RogueRoom room, int frame, RogueTile tile) {
        RogueBoard board = room.board;
        List<Vec2i> validTiles = new ArrayList<>();
        for (int z = 1; z < board.size.z - 1; z += 1) {
            for (int x = 1; x < board.size.x - 1; x += 1) {
                if (!board.getTile(x, z).isFloor()) continue;
                boolean isNearWall = false;
                for (int dz = -frame; dz <= frame && !isNearWall; dz += 1) {
                    for (int dx = -frame; dx <= frame && !isNearWall; dx += 1) {
                        if (board.getTile(x + dx, z + dz).isWall()) {
                            isNearWall = true;
                        }
                    }
                }
                if (!isNearWall) validTiles.add(new Vec2i(x, z));
            }
        }
        if (validTiles.size() <= 4) return false;
        for (Vec2i v : validTiles) {
            board.setTile(v.x, v.z, tile);
        }
        return true;
    }
}
