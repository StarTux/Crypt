package com.cavetale.crypt;

import com.cavetale.core.command.AbstractCommand;
import com.cavetale.core.command.CommandArgCompleter;
import com.cavetale.crypt.rogue.RogueGenerator;
import com.cavetale.crypt.rogue.RogueStyle;
import java.util.Random;
import org.bukkit.entity.Player;
import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.*;

public final class CryptCommand extends AbstractCommand<CryptPlugin> {
    protected CryptCommand(final CryptPlugin plugin) {
        super(plugin, "crypt");
    }

    @Override
    protected void onEnable() {
        rootNode.addChild("test").arguments("<style>")
            .completers(CommandArgCompleter.enumLowerList(RogueStyle.class))
            .description("Text Crypt Generation")
            .playerCaller(this::test);
    }

    private boolean test(Player player, String[] args) {
        if (args.length != 1) return false;
        RogueStyle style = CommandArgCompleter.requireEnum(RogueStyle.class, args[0]);
        RogueGenerator rogueGen = new RogueGenerator(plugin.getCryptWorld(),
                                                     new Random(),
                                                     player.getUniqueId(),
                                                     player.getName(),
                                                     style,
                                                     gen -> player.teleport(gen.getSpawn().toCenterFloorLocation(plugin.getCryptWorld())));
        rogueGen.start();
        player.sendMessage(text("Test generation started...", YELLOW));
        return true;
    }
}
