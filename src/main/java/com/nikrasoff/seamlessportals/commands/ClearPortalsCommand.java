package com.nikrasoff.seamlessportals.commands;

import com.nikrasoff.seamlessportals.SeamlessPortals;
import com.nikrasoff.seamlessportals.portals.Portal;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;

public class ClearPortalsCommand extends Command {
    @Override
    public void run(IChat chat) {
        super.run(chat);
        Portal[] portals = SeamlessPortals.portalManager.getPortalArray();
        for (Portal portal : portals){
            if (portal.zone == null) continue;
            portal.zone.removeEntity(portal);
        }
        SeamlessPortals.portalManager.createdPortals.clear();
        chat.addMessage(null, "All portal data cleared");
    }

    @Override
    public String getShortDescription() {
        return "";
    }
}
