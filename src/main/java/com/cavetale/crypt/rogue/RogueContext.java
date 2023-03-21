package com.cavetale.crypt.rogue;

import java.util.Random;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RogueContext {
    protected final Random random;
    protected final RogueRoom room;
    protected final RogueTile tile;
    protected final int floorLevel;
    protected final int ceilingLevel;
}
