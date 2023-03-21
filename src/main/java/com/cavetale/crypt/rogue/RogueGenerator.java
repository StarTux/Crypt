package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.struct.Vec3i;
import com.cavetale.crypt.cache.RegionCacheTag;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import static com.cavetale.crypt.CryptPlugin.plugin;
import static java.util.Collections.sort;
import static java.util.Comparator.comparing;

@Getter @RequiredArgsConstructor
public final class RogueGenerator {
    // Generator Parameters
    private final World world;
    private final Random random;
    private final UUID uuid;
    private final String name;
    private final RogueStyle style;
    private final Consumer<RogueGenerator> callback;
    // Output
    private RegionCacheTag tag;
    private Vec3i spawn;
    private Area totalArea;
    private int totalSplits;
    private int totalBlocks;
    private final List<Area> areas = new ArrayList<>();
    private final List<RogueRoom> rooms = new ArrayList<>();

    static final int FLOOR = 65;

    /**
     * Start the generation process based on the stored input
     * parameters.
     */
    public void start() {
        this.tag = plugin().getRegionCache().allocateRegions(1, 1, uuid, name);
        Bukkit.getScheduler().runTaskAsynchronously(plugin(), this::main);
    }

    /**
     * The main generation function which can be processed
     * asynchronously.
     */
    private void main() {
        final int cx = (tag.getOrigin().x << 9) + 255;
        final int cz = (tag.getOrigin().z << 9) + 255;
        this.spawn = new Vec3i(cx, FLOOR + 1, cz);
        // Split Areas
        int outset = 32;
        this.totalArea = new Area(cx - outset, cz - outset, cx + outset - 1, cz + outset - 1);
        areas.add(totalArea);
        splitArea(0);
        // Make Rooms
        for (Area area : areas) rooms.add(new RogueRoom(area));
        processNbors();
        while (true) {
            if (!combineRooms()) break;
        }
        for (RogueRoom room : rooms) room.processWalls();
        Bukkit.getScheduler().runTask(plugin(), this::post);
    }

    /**
     * Place the dungeon in the world once the main generation task is
     * done.  Must be called in the main thread.
     */
    private void post() {
        drawRooms();
        plugin().getLogger().info("[RogueGenerator]"
                                  + " area:" + totalArea
                                  + " splits:" + totalSplits
                                  + " areas:" + areas.size()
                                  + " rooms:" + rooms.size()
                                  + " spawn:" + spawn
                                  + " blocks:" + totalBlocks);
        if (callback != null) callback.accept(this);
    }

    private static final int MIN_ROOM_SIZE = 4;
    private static final int MIN_ROOM_SIZE_2 = MIN_ROOM_SIZE * 2;

    /**
     * Recursive area (rectangle) splitter.
     * Areas may not overlap but be exactly adjacent.  Walls will be
     * placed in the outermost pixels within the area.
     * @param the area index within the areas list
     */
    private void splitArea(int index) {
        final Area area = areas.get(index);
        final int width = area.getSizeX();
        final int height = area.getSizeZ();
        final boolean horizontal = width == height
            ? random.nextBoolean()
            : width > height;
        if (horizontal && width < MIN_ROOM_SIZE_2) return;
        if (!horizontal && height < MIN_ROOM_SIZE_2) return;
        final int ax = area.ax;
        final int az = area.az;
        final int bx = area.bx;
        final int bz = area.bz;
        final Area area1; // Replace area at index
        final Area area2; // New area
        if (horizontal) {
            final int sx = ax + MIN_ROOM_SIZE - 1 + random.nextInt(width - MIN_ROOM_SIZE_2 + 1);
            final int tx = sx + 1;
            area1 = new Area(ax, az, sx, bz);
            area2 = new Area(tx, az, bx, bz);
        } else {
            final int sz = az + MIN_ROOM_SIZE - 1 + random.nextInt(height - MIN_ROOM_SIZE_2 + 1);
            final int tz = sz + 1;
            area1 = new Area(ax, az, bx, sz);
            area2 = new Area(ax, tz, bx, bz);
        }
        areas.set(index, area1);
        final int index2 = areas.size();
        areas.add(area2);
        totalSplits += 1;
        splitArea(index);
        splitArea(index2);
    }

    /**
     * Process neighboring rooms prio to combining them.  So this
     * assumes that each room has exactly one area.
     */
    private void processNbors() {
        for (int i = 0; i < rooms.size() - 1; i += 1) {
            RogueRoom a = rooms.get(i);
            for (int j = i + 1; j < rooms.size(); j += 1) {
                RogueRoom b = rooms.get(j);
                if (!Area.areNbors(a.areas.get(0), b.areas.get(0))) continue;
                a.nbors.add(b);
                b.nbors.add(a);
            }
        }
    }

    private static final int DESIRED_ROOM_AREA = 9 * 9;

    /**
     * Combine rooms to create interesting shapes.  This function does
     * one step and the return value determines if it was to be the
     * final step.
     * @return true if there should be another step, false otherwise
     */
    private boolean combineRooms() {
        sort(rooms, comparing(RogueRoom::getArea));
        RogueRoom room1 = rooms.get(0);
        if (room1.getArea() >= DESIRED_ROOM_AREA) return false;
        RogueRoom room2 = room1.nbors.get(random.nextInt(room1.nbors.size()));
        rooms.remove(room1);
        rooms.remove(room2);
        RogueRoom room3 = new RogueRoom(room1, room2);
        for (RogueRoom it : rooms) {
            if (it.nbors.contains(room1) || it.nbors.contains(room2)) {
                it.nbors.remove(room1);
                it.nbors.remove(room2);
                it.nbors.add(room3);
            }
        }
        rooms.add(room3);
        return true;
    }

    /**
     * Draw all rooms in the world.
     */
    private int drawRooms() {
        int blocks = 0;
        for (RogueRoom room : rooms) {
            int ceiling = FLOOR + 5 + random.nextInt(3) - random.nextInt(3);
            RogueContext context = new RogueContext(random, room, FLOOR, ceiling);
            for (Area area : room.areas) {
                for (int z = area.az; z <= area.bz; z += 1) {
                    for (int x = area.ax; x <= area.bx; x += 1) {
                        Vec2i vec = new Vec2i(x, z);
                        if (room.walls.contains(vec)) {
                            for (int y = FLOOR; y <= ceiling; y += 1) {
                                world.getBlockAt(x, y, z).setBlockData(style.wall(context, x, y, z));
                                this.totalBlocks += 1;
                            }
                        } else {
                            for (int y = FLOOR + 1; y < ceiling; y += 1) {
                                world.getBlockAt(x, y, z).setType(Material.CAVE_AIR);
                                this.totalBlocks += 1;
                            }
                            world.getBlockAt(x, FLOOR, z).setBlockData(style.floor(context, x, FLOOR, z));
                            world.getBlockAt(x, ceiling, z).setBlockData(style.ceiling(context, x, ceiling, z));
                            this.totalBlocks += 2;
                        }
                    }
                }
            }
        }
        return blocks;
    }
}
