package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.struct.Vec3i;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Axis;

public final class RogueDoor {
    protected final Axis axis;
    protected List<RogueRoom> rooms = new ArrayList<>();
    protected final List<Vec2i> vectors = new ArrayList<>();
    protected final List<RogueTile> tiles = new ArrayList<>();
    protected final List<Vec3i> blocks = new ArrayList<>();

    protected RogueDoor(final Axis axis,
                        final RogueRoom room1, final RogueRoom room2,
                        final Vec2i vector1, final Vec2i vector2,
                        final RogueTile tile1, final RogueTile tile2) {
        this.axis = axis;
        rooms.add(room1);
        rooms.add(room2);
        vectors.add(vector1);
        vectors.add(vector2);
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
