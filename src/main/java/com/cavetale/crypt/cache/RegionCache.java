package com.cavetale.crypt.cache;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.util.Json;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.World;
import static com.cavetale.crypt.CryptPlugin.plugin;

/**
 * Allocate regions and remember which are already in use.
 */
public final class RegionCache {
    private final World world;
    private final String name;
    private final File folder;
    private final Map<Vec2i, RegionCacheTag> regionMap = new HashMap<>();

    public RegionCache(final World world) {
        this.world = world;
        this.name = world.getName();
        this.folder = new File(world.getWorldFolder(), "crypts");
    }

    public void enable() {
        folder.mkdirs();
        load();
    }

    private void severe(String msg) {
        plugin().getLogger().severe("[RegionCache] [" + name + "] " + msg);
    }

    private void info(String msg) {
        plugin().getLogger().info("[RegionCache] [" + name + "] " + msg);
    }

    /**
     * Load all RegionCacheTag instances, from one file each in the
     * "crypts" folder, and store them in regionMap.
     */
    private void load() {
        regionMap.clear();
        for (File file : folder.listFiles()) {
            String filename = file.getName();
            String[] split = filename.split("\\.");
            // r.X.Y.json
            if (split.length != 4 || !split[0].equals("r") || !split[3].equals("json")) {
                severe("Bad filename: " + file + ": " + List.<String>of(split));
                continue;
            }
            final int x;
            final int z;
            try {
                x = Integer.parseInt(split[1]);
                z = Integer.parseInt(split[2]);
            } catch (NumberFormatException nfe) {
                severe("Bad coordinates: " + file);
                continue;
            }
            final Vec2i vec = Vec2i.of(x, z);
            RegionCacheTag tag = Json.load(file, RegionCacheTag.class);
            regionMap.put(vec, tag);
        }
        info(regionMap.size() + " regions loaded");
    }

    /**
     * Allocate some regions.  Each region will be represented by one
     * file named "r.X.Z.json", and the file will store the uuid and
     * name which are not otherwise used but could be useful for the
     * purpose of debugging.
     *
     * @param An informal uuid to store in the region(s)
     * @param An informal name to store in the region(s)
     * @return the region coordinates
     */
    public RegionCacheTag allocateRegions(int width, int height, UUID uuid, String cachename) {
        RegionCacheTag tag = new RegionCacheTag(uuid, cachename);
        allocateRegions(width, height, tag);
        return tag;
    }

    /**
     * Allocate a new area of regions.  The result will be stored in
     * the origin of the tag.
     * @param width the width in 512 block regions
     * @param height the height in 512 block regions
     * @tag the tag
     */
    public void allocateRegions(int width, int height, RegionCacheTag tag) {
        if (tryToAllocate(0, 0, width, height, tag)) return;
        for (int r = 1;; r += 1) { // ring
            for (int d = -r; d < r; d += 1) {
                if (tryToAllocate(d, -r, width, height, tag)) return; // north
            }
            for (int d = -r; d < r; d += 1) {
                if (tryToAllocate(r, d, width, height, tag)) return; // east
            }
            for (int d = -r; d < r; d += 1) {
                if (tryToAllocate(-d, r, width, height, tag)) return; // south
            }
            for (int d = -r; d < r; d += 1) {
                if (tryToAllocate(-r, -d, width, height, tag)) return; // west
            }
        }
    }

    private boolean tryToAllocate(final int x, final int z, final int w, final int h, RegionCacheTag tag) {
        for (int dz = 0; dz < h; dz += 1) {
            for (int dx = 0; dx < w; dx += 1) {
                Vec2i vec = Vec2i.of(x + dx, z + dz);
                if (regionMap.containsKey(vec)) return false;
            }
        }
        tag.setOrigin(Vec2i.of(x, z));
        tag.setSize(Vec2i.of(w, h));
        info("Regions allocated: " + Json.serialize(tag));
        for (int dz = 0; dz < h; dz += 1) {
            for (int dx = 0; dx < w; dx += 1) {
                Vec2i vec = Vec2i.of(x + dx, z + dz);
                regionMap.put(vec, tag);
                tag.save(folder, vec);
            }
        }
        return true;
    }
}
