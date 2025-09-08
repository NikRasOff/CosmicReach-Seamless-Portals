package com.nikrasoff.seamlessportals.commands;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;
import finalforeach.cosmicreach.networking.NetworkIdentity;
import finalforeach.cosmicreach.singletons.GameSingletons;
import finalforeach.cosmicreach.chat.Chat;
import finalforeach.cosmicreach.networking.packets.MessagePacket;
import finalforeach.cosmicreach.networking.server.ServerSingletons;

public class ClearAnchorsCommand extends Command {

    public void run(IChat chat){
        SeamlessPortals.portalManager.spacialAnchors.clear();
        if (GameSingletons.isClient){
            Chat.MAIN_CLIENT_CHAT.addMessage(null, "Spacial anchor data cleared");
        }
        if (GameSingletons.isHost && ServerSingletons.SERVER != null){
            NetworkIdentity identity = ServerSingletons.getConnection(getCallingPlayer());
            identity.send(new MessagePacket("Spacial anchor data cleared"));
        }
    }

    @Override
    public String getShortDescription() {
        return "";
    }
}
