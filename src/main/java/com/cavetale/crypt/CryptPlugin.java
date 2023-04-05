package com.cavetale.crypt;

import com.cavetale.blank.Blank;
import com.cavetale.crypt.cache.RegionCache;
import lombok.Getter;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

@Getter
public final class CryptPlugin extends JavaPlugin {
    private static CryptPlugin instance;
    public static final String CRYPT_WORLD_NAME = "crypt";
    private final CryptCommand cryptCommand = new CryptCommand(this);
    private final EventListener eventListener = new EventListener(this);
    private World cryptWorld;
    private RegionCache regionCache;

    @Override
    public void onEnable() {
        instance = this;
        cryptCommand.enable();
        eventListener.enable();
        cryptWorld = Blank.create(CRYPT_WORLD_NAME, c -> c.environment(World.Environment.NORMAL));
        cryptWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        cryptWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        cryptWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        cryptWorld.setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
        cryptWorld.setTime(18000L);
        regionCache = new RegionCache(cryptWorld);
        regionCache.enable();
    }

    @Override
    public void onDisable() {
    }

    public static CryptPlugin plugin() {
        return instance;
    }
}
