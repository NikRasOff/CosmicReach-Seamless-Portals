package com.nikrasoff.seamlessportals.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.chat.Chat;

public class ClearAnchorsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("anchorClear");
        cmd.requires(ServerCommandSource::hasOperator).executes(ClearAnchorsCommand::run);
        LiteralArgumentBuilder<ServerCommandSource> cmd2 = CommandManager.literal("portal");
        cmd2.then(cmd);
        dispatcher.register(cmd2);
    }

    public static int run(CommandContext<ServerCommandSource> context){
        SeamlessPortals.portalManager.spacialAnchors.clear();
        Chat.MAIN_CLIENT_CHAT.addMessage(null, "Spacial anchor data cleared");
        return 0;
    }
}
