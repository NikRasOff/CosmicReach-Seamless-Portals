package com.nikrasoff.seamlessportals.commands;

import com.github.puzzle.game.commands.CommandManager;
import com.github.puzzle.game.commands.ServerCommandSource;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.GameSingletons;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;

public class ListAnchorsCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher){
        LiteralArgumentBuilder<ServerCommandSource> cmd = CommandManager.literal("anchorList");
        cmd.requires(ServerCommandSource::hasOperator).executes(ListAnchorsCommand::run);
        LiteralArgumentBuilder<ServerCommandSource> cmd2 = CommandManager.literal("portal");
        cmd2.then(cmd);
        dispatcher.register(cmd2);
    }

    public static int run(CommandContext<ServerCommandSource> context){
        if (GameSingletons.isClient){
            Chat.MAIN_CLIENT_CHAT.addMessage(null, "All spacial anchors:");
            SeamlessPortals.portalManager.spacialAnchors.forEach(entry -> {
                Chat.MAIN_CLIENT_CHAT.addMessage(null, "At id " + entry.key + ":");
                entry.value.forEach(info -> {
                    Chat.MAIN_CLIENT_CHAT.addMessage(null, " -- " + info.position + " in zone " + info.zoneId);
                });
            });
        }
        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
            NetworkIdentity identity = context.getSource().getIdentity();
            identity.send(new MessagePacket("All spacial anchors:"));
            SeamlessPortals.portalManager.spacialAnchors.forEach(entry -> {
                identity.send(new MessagePacket("At id " + entry.key + ":"));
                entry.value.forEach(info -> {
                    identity.send(new MessagePacket(" -- " + info.position + " in zone " + info.zoneId));
                });
            });
        }
        return 0;
    }
}
