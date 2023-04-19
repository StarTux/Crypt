package com.cavetale.crypt.rogue;

import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;

/**
 * This context is made by RogueGenerator and handed to RogueStyle to
 * help with a consistent decoration.
 */
@RequiredArgsConstructor
public final class RogueContext {
    protected final Random random;
    protected final int floorLevel;
    protected RogueRoom room;
    protected int ceilingLevel;
    protected int x;
    protected int y;
    protected int z;
    protected RogueTile tile;
    protected Set<Hint> hints = Set.of();

    protected RogueContext xyz(final int nx, final int ny, final int nz, final Set<Hint> nhints) {
        this.x = nx;
        this.y = ny;
        this.z = nz;
        this.hints = nhints;
        return this;
    }

    protected RogueContext xyz(final int nx, final int ny, final int nz) {
        return xyz(nx, ny, nz, Set.of());
    }

    protected enum Hint {
        DOOR_FRAME,
        ;
    }
}
