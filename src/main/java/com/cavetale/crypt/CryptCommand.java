package com.cavetale.crypt;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.crypt.rogue.RogueGenerator;
import java.util.Random;
import org.bukkit.entity.Player;

public final class CryptCommand extends AbstractCommand<CryptPlugin> {
    protected CryptCommand(final CryptPlugin plugin) {
        super(plugin, "crypt");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("test").denyTabCompletion()
            .description("Text Crypt Generation")
            .playerCaller(this::test);
    }

    private boolean test(Player player, String[] args) {
        if (args.length != 0) return false;
        RogueGenerator rogueGen = new RogueGenerator(plugin.getCryptWorld(),
                                                     new Random(),
                                                     player.getUniqueId(),
                                                     player.getName());
        rogueGen.start();
        player.teleport(rogueGen.getSpawn().toCenterFloorLocation(plugin.getCryptWorld()));
        return true;
    }
}
