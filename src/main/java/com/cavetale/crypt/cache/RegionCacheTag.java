package com.cavetale.crypt.cache;

import com.cavetale.core.struct.Vec2i;
import com.cavetale.core.util.Json;
import com.cavetale.crypt.meta.CryptMeta;
import java.io.File;
import java.util.Date;
import java.util.UUID;
import lombok.Data;

@Data
public final class RegionCacheTag {
    private Vec2i origin;
    private Vec2i size;
    private UUID player; // informal
    private String name; // informal
    private String created;
    private CryptMeta crypt;

    public RegionCacheTag() {
        this.created = new Date().toString();
    }

    public RegionCacheTag(final UUID player, final String name) {
        this.player = player;
        this.name = name;
    }

    public void save(File folder, Vec2i region) {
        File file = new File(folder, "r." + region.x + "." + region.z + ".json");
        Json.save(file, this, true);
    }
}
