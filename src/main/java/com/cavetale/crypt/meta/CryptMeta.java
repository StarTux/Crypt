package com.cavetale.crypt.meta;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
/**
 * Saved for each crypt.
 */
public final class CryptMeta {
    private final List<CryptFloorMeta> floors = new ArrayList<>();
    private int currentFloor;

    public CryptFloorMeta getOrCreateFloor(final int index) {
        while (floors.size() <= index) {
            CryptFloorMeta meta = new CryptFloorMeta();
            meta.setIndex(floors.size());
            floors.add(meta);
        }
        return floors.get(index);
    }
}
