package com.nikrasoff.seamlessportals.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;

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
        if (GameSingletons.isClient){
            Chat.MAIN_CLIENT_CHAT.addMessage(null, "Spacial anchor data cleared");
        }
        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
            context.getSource().getIdentity().send(new MessagePacket("Spacial anchor data cleared"));
        }
        return 0;
    }
}
