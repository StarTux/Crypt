package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Axis;

public final class RogueDoor {
    protected final Axis axis;
    protected List<RogueRoom> rooms = new ArrayList<>();
    protected final List<Vec2i> tiles = new ArrayList<>();

    protected RogueDoor(final Axis axis, final RogueRoom room1, final RogueRoom room2, final Vec2i tile1, final Vec2i tile2) {
        this.axis = axis;
        rooms.add(room1);
        rooms.add(room2);
        tiles.add(tile1);
        tiles.add(tile2);
    }

    public RogueRoom otherRoom(RogueRoom thisRoom) {
        for (RogueRoom it : rooms) {
            if (it != thisRoom) return it;
        }
        return null;
    }
}
