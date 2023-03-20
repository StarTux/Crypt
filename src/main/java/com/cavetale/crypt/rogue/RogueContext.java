package com.cavetale.crypt.rogue;

import java.util.Random;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class RogueContext {
    protected final Random random;
    protected final Room room;
    protected final int floorLevel;
    protected final int ceilingLevel;
}
