package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.List;

/**
 * Room abstraction for the RogueGenerator.
 */
final class Room {
    protected final List<Area> areas = new ArrayList<>();
    protected final List<Room> nbors = new ArrayList<>();
    protected final List<Vec2i> walls = new ArrayList<>();

    protected Room() { }

    protected Room(final Area mainArea) {
        areas.add(mainArea);
    }

    /**
     * Combiner constructor.
     */
    protected Room(final Room a, final Room b) {
        areas.addAll(a.areas);
        areas.addAll(b.areas);
        nbors.addAll(a.nbors);
        for (Room nbor : b.nbors) {
            if (!nbors.contains(nbor)) nbors.add(nbor);
        }
        nbors.remove(a);
        nbors.remove(b);
    }

    protected int getArea() {
        int result = 0;
        for (Area area : areas) result += area.getArea();
        return result;
    }

    public boolean contains(Vec2i vec) {
        for (Area area : areas) {
            if (area.contains(vec)) return true;
        }
        return false;
    }

    protected void processWalls() {
        for (Area area : areas) {
            WALLS: for (Vec2i wall : area.getWalls()) {
                for (int dz = -1; dz <= 1; dz += 1) {
                    for (int dx = -1; dx <= 1; dx += 1) {
                        if (dx == 0 && dz == 0) continue;
                        if (!contains(wall.add(dx, dz))) {
                            walls.add(wall);
                            continue WALLS;
                        }
                    }
                }
            }
        }
    }
}
