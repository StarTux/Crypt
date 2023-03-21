package com.cavetale.crypt.rogue;

import com.cavetale.crypt.struct.Area;
import java.util.Random;
import java.util.UUID;
import org.junit.Test;

public final class RogueGeneratorTest {
    static final int MIN_ROOM_SIZE = 4;

    public void test() {
        Random random = new Random();
        int minLeft = 0;
        int minRight = 0;
        int maxLeft = 0;
        int maxRight = 0;
        for (int i = 0; i < 99; i += 1) {
            int width = MIN_ROOM_SIZE * 2 + random.nextInt(MIN_ROOM_SIZE);
            int a = random.nextInt(512) - random.nextInt(512);
            int b = a + width - 1;
            int s = a + MIN_ROOM_SIZE - 1 + random.nextInt(width - MIN_ROOM_SIZE * 2 + 1);
            int t = s + 1;
            int l = s - a + 1;
            int r = b - t + 1;
            assert l + r == width;
            assert l >= MIN_ROOM_SIZE : "l = " + l;
            assert r >= MIN_ROOM_SIZE : "r = " + r;
            if (i == 0) {
                minLeft = l;
                maxLeft = l;
                minRight = r;
                maxRight = r;
            } else {
                if (l < minLeft) minLeft = l;
                if (r < minRight) minRight = r;
                if (l > maxLeft) maxLeft = l;
                if (r > maxRight) maxRight = r;
            }
        }
        System.out.println("left = " + minLeft + " ... " + maxLeft);
        System.out.println("right = " + minRight + " ... " + maxRight);
    }

    @Test
    public void test2() {
        RogueGenerator gen = new RogueGenerator(null, new Random(), new UUID(0L, 0L), "noname", null, null);
        gen.setTotalArea(new Area(0, 0, 80, 40));
        gen.main();
        System.out.println(gen.makeBoard().toMultiLineString());
    }
}
