package com.cavetale.crypt;

import com.cavetale.blank.event.BlankBiomeProviderEvent;
import com.cavetale.blank.event.BlankGeneratorEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import static com.cavetale.crypt.CryptPlugin.CRYPT_WORLD_NAME;

@RequiredArgsConstructor
public final class EventListener implements Listener {
    private final CryptPlugin plugin;

    public void enable() {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    private void onBlankGenerator(BlankGeneratorEvent event) {
        if (!event.getWorldName().equals(CRYPT_WORLD_NAME)) return;
        if (event.getPhase().ordinal() != 0) return;
        final int min = event.getWorldInfo().getMinHeight();
        final int max = event.getWorldInfo().getMaxHeight();
        event.getChunkData().setRegion(0, min, 0, 16, max, 16, Material.BEDROCK);
    }

    @EventHandler
    private void onBlankBiomeProvider(BlankBiomeProviderEvent event) {
        if (!event.getWorldName().equals(CRYPT_WORLD_NAME)) return;
        final Biome biome = Biome.CRIMSON_FOREST;
        event.getBiomeProvider().setSingleBiome(biome);
        event.setBiome(biome);
        event.setBiomes(List.of(biome));
    }
}
