package com.cavetale.crypt.rogue;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.struct.Vec3i;
import com.cavetale.crypt.cache.RegionCacheTag;
import com.cavetale.crypt.struct.Area;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import static com.cavetale.core.util.CamelCase.toCamelCase;
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
    @Setter private Area totalArea;
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
        final int cx = (tag.getOrigin().x << 9) + 255;
        final int cz = (tag.getOrigin().z << 9) + 255;
        // Split Areas
        int outset = 32;
        this.totalArea = new Area(cx - outset, cz - outset, cx + outset - 1, cz + outset - 1);
        Bukkit.getScheduler().runTaskAsynchronously(plugin(), () -> {
                main();
                Bukkit.getScheduler().runTask(plugin(), this::post);
            });
    }

    /**
     * The main generation function which can be processed
     * This requires the totalArea to be set!
     * asynchronously,  or ran on its own for testing.
     */
    protected void main() {
        areas.add(totalArea);
        splitArea(0);
        // Make Rooms
        for (Area area : areas) rooms.add(new RogueRoom(area));
        processNbors();
        while (true) {
            if (!combineRooms()) break;
        }
        removeRooms(rooms.size() / 2);
        for (RogueRoom room : rooms) room.makeBoard();
        findCrawlPath();
        for (RogueRoom room : rooms) room.placeDoors(FLOOR);
        for (RogueRoom room : rooms) room.decorate();
        var center = rooms.get(0).areas.get(0).getCenter();
        this.spawn = new Vec3i(center.x, FLOOR + 1, center.z);
    }

    /**
     * Place the dungeon in the world once the main generation task is
     * done.  Must be called in the main thread.
     */
    private void post() {
        drawRooms();
        plugin().getLogger().info("\n" + makeBoard().toMultiLineString());
        plugin().getLogger().info("[RogueGenerator]"
                                  + " area:" + totalArea
                                  + " splits:" + totalSplits
                                  + " areas:" + areas.size()
                                  + " rooms:" + rooms.size()
                                  + " spawn:" + spawn
                                  + " blocks:" + totalBlocks);
        if (callback != null) callback.accept(this);
    }

    private static final int MIN_ROOM_SIZE = 5;
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
                if (!Area.areNbors(MIN_ROOM_SIZE - 1, a.areas.get(0), b.areas.get(0))) continue;
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
        for (int i = 0; i < rooms.size(); i += 1) {
            RogueRoom room1 = rooms.get(i);
            if (room1.nbors.isEmpty()) continue;
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
        return false;
    }

    /**
     * Remove some room while attempting to keep the whole crypt
     * traversable.
     * To that end, we never remove:
     * - Rooms splitting the map in two
     * - Rooms cutting off one of the corners
     * - Two rooms being neighbors of each other
     */
    private void removeRooms(int maxRemoveCount) {
        Collections.shuffle(rooms, random);
        List<RogueRoom> removeRooms = new ArrayList<>();
        for (int i = 0; i < rooms.size() && removeRooms.size() < maxRemoveCount; i += 1) {
            RogueRoom room = rooms.get(i);
            removeRooms.add(room);
            if (!areAllRoomsConnected(removeRooms)) {
                removeRooms.remove(room);
            }
        }
        rooms.removeAll(removeRooms);
        for (RogueRoom room : rooms) {
            room.nbors.removeAll(removeRooms);
        }
    }

    private boolean areAllRoomsConnected(List<RogueRoom> ignoreList) {
        List<RogueRoom> connected = new ArrayList<>();
        for (RogueRoom room : rooms) {
            if (!ignoreList.contains(room)) {
                connected.add(room);
                break;
            }
        }
        if (connected.isEmpty()) return false;
        for (int i = 0; i < connected.size(); i += 1) {
            RogueRoom room = connected.get(i);
            for (RogueRoom nbor : room.nbors) {
                if (!connected.contains(nbor) && !ignoreList.contains(nbor)) {
                    connected.add(nbor);
                }
            }
        }
        return connected.size() == rooms.size() - ignoreList.size();
    }

    private void findCrawlPath() {
        Collections.sort(rooms, comparing(RogueRoom::getArea));
        List<RogueRoom> done = new ArrayList<>();
        RogueRoom entrance = rooms.get(0);
        entrance.purpose = RogueRoomPurpose.ENTRANCE;
        done.add(entrance);
        List<RogueRoom> prev = List.of(entrance);
        List<RogueRoom> next;
        int iter = 1;
        int distance = 0;
        while (!prev.isEmpty()) {
            distance += 1;
            next = new ArrayList<>();
            for (RogueRoom from : prev) {
                for (RogueRoom to : from.nbors) {
                    if (done.contains(to)) continue;
                    done.add(to);
                    from.openDoor(to, random);
                    next.add(to);
                    from.nextRooms.add(to);
                    to.previousRoom = from;
                    to.roomIndex = iter++;
                    to.distanceToEntrance = distance;
                }
            }
            prev = next;
        }
        // Room with highest index is exit
        Collections.sort(rooms, comparing(RogueRoom::getDistanceToEntrance));
        RogueRoom exit = rooms.get(rooms.size() - 1);
        exit.purpose = RogueRoomPurpose.EXIT;
        List<RogueRoomPurpose> purposes = new ArrayList<>(List.of(RogueRoomPurpose.values()));
        purposes.remove(RogueRoomPurpose.ENTRANCE);
        purposes.remove(RogueRoomPurpose.EXIT);
        Iterator<RogueRoomPurpose> purposeIter = purposes.iterator();
        for (int i = rooms.size() - 2; i >= 0 && purposeIter.hasNext(); i -= 1) {
            RogueRoom room = rooms.get(i);
            if (!room.nextRooms.isEmpty()) continue;
            room.purpose = purposeIter.next();
        }
    }

    private static final List<Vec2i> NBOR_TILES_4 = List.of(new Vec2i(0, 1), new Vec2i(0, -1),
                                                            new Vec2i(1, 0), new Vec2i(-1, 0));

    /**
     * Draw all rooms in the world.
     */
    private int drawRooms() {
        int blocks = 0;
        RogueContext context = new RogueContext(random, FLOOR);
        for (RogueRoom room : rooms) {
            context.room = room;
            final int ceiling = FLOOR + Math.max(3, 6 + random.nextInt(3) - random.nextInt(3));
            context.ceilingLevel = ceiling;
            for (int dz = 0; dz < room.board.size.z; dz += 1) {
                for (int dx = 0; dx < room.board.size.x; dx += 1) {
                    final RogueTile tile = room.board.getTile(dx, dz);
                    if (tile.isUndefined()) continue;
                    context.tile = tile;
                    int x = room.boundingBox.ax + dx;
                    int z = room.boundingBox.az + dz;
                    boolean isPitAdjacent = false;
                    for (Vec2i vector : NBOR_TILES_4) {
                        if (room.board.getTile(dx + vector.x, dz + vector.z).isPit()) {
                            isPitAdjacent = true;
                            break;
                        }
                    }
                    if (isPitAdjacent) {
                        for (int y = world.getMinHeight(); y < FLOOR; y += 1) {
                            world.getBlockAt(x, y, z).setBlockData(style.wall(context.xyz(x, y, z)));
                        }
                    }
                    if (tile.isWall()) {
                        boolean isDoorFrame = false;
                        for (Vec2i vector : NBOR_TILES_4) {
                            if (room.board.getTile(dx + vector.x, dz + vector.z).isDoor()) {
                                isDoorFrame = true;
                                break;
                            }
                        }
                        for (int y = FLOOR; y <= ceiling; y += 1) {
                            Set<RogueContext.Hint> hints = isDoorFrame && y <= FLOOR + 3
                                ? Set.of(RogueContext.Hint.DOOR_FRAME)
                                : Set.of();
                            world.getBlockAt(x, y, z).setBlockData(style.wall(context.xyz(x, y, z, hints)));
                            this.totalBlocks += 1;
                        }
                    } else if (tile.isDoor()) {
                        for (int y = FLOOR; y <= FLOOR + 2; y += 1) {
                            world.getBlockAt(x, y, z).setBlockData(Material.CAVE_AIR.createBlockData());
                            this.totalBlocks += 1;
                        }
                        world.getBlockAt(x, FLOOR + 3, z).setBlockData(style.wall(context.xyz(x, FLOOR + 3, z, Set.of(RogueContext.Hint.DOOR_FRAME))));
                        this.totalBlocks += 1;
                        for (int y = FLOOR + 4; y < ceiling; y += 1) {
                            world.getBlockAt(x, y, z).setBlockData(style.wall(context.xyz(x, y, z)));
                            this.totalBlocks += 1;
                        }
                        world.getBlockAt(x, FLOOR, z).setBlockData(style.floor(context.xyz(x, FLOOR, z)));
                        world.getBlockAt(x, ceiling, z).setBlockData(style.ceiling(context.xyz(x, ceiling, z)));
                        this.totalBlocks += 2;
                    } else if (tile.isPit()) {
                        for (int y = world.getMinHeight(); y < ceiling; y += 1) {
                            world.getBlockAt(x, y, z).setBlockData(Material.CAVE_AIR.createBlockData());
                        }
                        world.getBlockAt(x, ceiling, z).setBlockData(style.ceiling(context.xyz(x, ceiling, z)));
                    } else if (tile.isFloor()) {
                        for (int y = FLOOR + 1; y < ceiling; y += 1) {
                            world.getBlockAt(x, y, z).setType(Material.CAVE_AIR);
                            this.totalBlocks += 1;
                        }
                        world.getBlockAt(x, FLOOR, z).setBlockData(style.floor(context.xyz(x, FLOOR, z)));
                        world.getBlockAt(x, ceiling, z).setBlockData(style.ceiling(context.xyz(x, ceiling, z)));
                        this.totalBlocks += 2;
                    }
                }
            }
        }
        return blocks;
    }

    /**
     * Combine all rooms into one board.  If main() has not finished,
     * the result is undefined.
     */
    public RogueBoard makeBoard() {
        RogueBoard result = new RogueBoard(totalArea.getSizeX(), totalArea.getSizeZ());
        for (RogueRoom room : rooms) {
            for (int dz = 0; dz < room.board.size.z; dz += 1) {
                for (int dx = 0; dx < room.board.size.x; dx += 1) {
                    RogueTile tile = room.board.getTile(dx, dz);
                    if (tile.isUndefined()) continue;
                    int x = dx + room.boundingBox.ax - totalArea.ax;
                    int z = dz + room.boundingBox.az - totalArea.az;
                    result.setTile(x, z, tile);
                    if (room.purpose != null) {
                        String label = toCamelCase(" ", room.purpose);
                        Vec3i line = room.board.getWidestLine();
                        int offset = Math.max(line.x, line.x + (line.z - label.length()) / 2);
                        for (int i = 0; i < line.z && i < label.length(); i += 1) {
                            result.setChar(offset + room.boundingBox.ax - totalArea.ax + i,
                                           line.y + room.boundingBox.az - totalArea.az,
                                           label.charAt(i));
                        }
                    }
                }
            }
        }
        return result;
    }
}
