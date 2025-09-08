package com.nikrasoff.seamlessportals.commands;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;

public class ListAnchorsCommand extends Command {

    @Override
    public void run(IChat chat) {
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
            NetworkIdentity identity = ServerSingletons.getConnection(getCallingPlayer());
            identity.send(new MessagePacket("All spacial anchors:"));
            SeamlessPortals.portalManager.spacialAnchors.forEach(entry -> {
                identity.send(new MessagePacket("At id " + entry.key + ":"));
                entry.value.forEach(info -> {
                    identity.send(new MessagePacket(" -- " + info.position + " in zone " + info.zoneId));
                });
            });
        }
    }

    @Override
    public String getShortDescription() {
        return "";
    }

}
