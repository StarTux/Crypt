package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.struct.Vec3i;
import com.cavetale.crypt.cache.RegionCacheTag;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Material;
import org.bukkit.World;
import static com.cavetale.crypt.CryptPlugin.plugin;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

@Getter @RequiredArgsConstructor
public final class RogueGenerator {
    private final World world;
    private final Random random;
    private final UUID uuid;
    private final String name;
    private final RogueStyle style = RogueStyle.DESERT;
    //private final Runnable callback;
    private RegionCacheTag tag;
    private Vec3i spawn;

    static final int FLOOR = 65;
    static final int CEILING = 69;

    public void start() {
        this.tag = plugin().getRegionCache().allocateRegions(1, 1, uuid, name);
        final int cx = (tag.getOrigin().x << 9) + 255;
        final int cz = (tag.getOrigin().z << 9) + 255;
        // Split Areas
        int outset = 32;
        Area totalArea = new Area(cx - outset, cz - outset, cx + outset - 1, cz + outset - 1);
        List<Area> areas = new ArrayList<>();
        areas.add(totalArea);
        final int steps = split(areas, 0);
        // Make Rooms
        List<Room> rooms = new ArrayList<>();
        for (Area area : areas) rooms.add(new Room(area));
        processNbors(rooms);
        while (true) {
            if (!combine(rooms)) break;
        }
        for (Room room : rooms) room.processWalls();
        // Change Blocks
        int blocks = 0;
        for (Room room : rooms) {
            RogueContext context = new RogueContext(random, room, FLOOR, CEILING);
            for (Area area : room.areas) {
                for (int z = area.az; z <= area.bz; z += 1) {
                    for (int x = area.ax; x <= area.bx; x += 1) {
                        Vec2i vec = new Vec2i(x, z);
                        if (room.walls.contains(vec)) {
                            for (int y = FLOOR; y <= CEILING; y += 1) {
                                world.getBlockAt(x, y, z).setBlockData(style.wall(context, x, y, z));
                                blocks += 1;
                            }
                        } else {
                            for (int y = FLOOR + 1; y < CEILING; y += 1) {
                                world.getBlockAt(x, y, z).setType(Material.CAVE_AIR);
                                blocks += 1;
                            }
                            world.getBlockAt(x, FLOOR, z).setBlockData(style.floor(context, x, FLOOR, z));
                            world.getBlockAt(x, CEILING, z).setBlockData(style.ceiling(context, x, CEILING, z));
                            blocks += 2;
                        }
                    }
                }
            }
        }
        spawn = new Vec3i(cx, 65, cz);
        plugin().getLogger().info("[RogueGenerator]"
                                  + " area:" + totalArea
                                  + " splits:" + steps
                                  + " areas:" + areas.size()
                                  + " rooms:" + rooms.size()
                                  + " spawn:" + spawn
                                  + " blocks:" + blocks);
    }

    private static final int MIN_ROOM_SIZE = 4;
    private static final int MIN_ROOM_SIZE_2 = MIN_ROOM_SIZE * 2;

    /**
     * Recursive area (rectangle) splitter.
     * Areas may not overlap but be exactly adjacent.  Walls will be
     * placed in the outermost pixels within the area.
     */
    private int split(List<Area> rooms, int index) {
        final Area room = rooms.get(index);
        final int width = room.getSizeX();
        final int height = room.getSizeZ();
        final boolean horizontal = width == height
            ? random.nextBoolean()
            : width > height;
        if (horizontal && width <= MIN_ROOM_SIZE_2) return 0;
        if (!horizontal && height <= MIN_ROOM_SIZE_2) return 0;
        final int ax = room.ax;
        final int az = room.az;
        final int bx = room.bx;
        final int bz = room.bz;
        final Area room1; // Replace room at index
        final Area room2; // New room
        if (horizontal) {
            final int sx = ax + MIN_ROOM_SIZE - 1 + random.nextInt(width - MIN_ROOM_SIZE_2 + 1);
            final int tx = sx + 1;
            room1 = new Area(ax, az, sx, bz);
            room2 = new Area(tx, az, bx, bz);
        } else {
            final int sz = az + MIN_ROOM_SIZE - 1 + random.nextInt(height - MIN_ROOM_SIZE_2 + 1);
            final int tz = sz + 1;
            room1 = new Area(ax, az, bx, sz);
            room2 = new Area(ax, tz, bx, bz);
        }
        rooms.set(index, room1);
        final int index2 = rooms.size();
        rooms.add(room2);
        return split(rooms, index)
            + split(rooms, index2)
            + 1;
    }

    /**
     * Two areas are neighbors if they are able to share a set of
     * matching doorways.
     * So, they need to be right next to each other, and have
     * overlapping non-wall surface.
     */
    private static boolean areNbors(Area a, Area b) {
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

    /**
     * Process neighboring rooms prio to combining them.  So this
     * assumes that each room has exactly one area.
     */
    private static void processNbors(List<Room> rooms) {
        for (int i = 0; i < rooms.size() - 1; i += 1) {
            Room a = rooms.get(i);
            for (int j = i + 1; j < rooms.size(); j += 1) {
                Room b = rooms.get(j);
                if (!areNbors(a.areas.get(0), b.areas.get(0))) continue;
                a.nbors.add(b);
                b.nbors.add(a);
            }
        }
    }

    private static final int DESIRED_ROOM_AREA = 9 * 9;

    /**
     * Combine rooms to create interesting shapes.
     */
    private boolean combine(List<Room> rooms) {
        sort(rooms, comparing(Room::getArea));
        Room room1 = rooms.get(0);
        if (room1.getArea() >= DESIRED_ROOM_AREA) return false;
        Room room2 = room1.nbors.get(random.nextInt(room1.nbors.size()));
        rooms.remove(room1);
        rooms.remove(room2);
        Room room3 = new Room(room1, room2);
        for (Room it : rooms) {
            if (it.nbors.contains(room1) || it.nbors.contains(room2)) {
                it.nbors.remove(room1);
                it.nbors.remove(room2);
                it.nbors.add(room3);
            }
        }
        rooms.add(room3);
        return true;
    }
}
