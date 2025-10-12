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
            if (portal.zone != null) continue;
            chat.addMessage(null, "Clearing data for portal: " + portal.uniqueId);
            if (portal.currentChunk != null){
                portal.currentChunk.removeEntity(portal);
                portal.currentChunk = null;
            }
            SeamlessPortals.portalManager.removePortal(portal);
        }
        chat.addMessage(null, "All broken portal data cleared");
    }

    @Override
    public String getShortDescription() {
        return "portalClear: Deletes all improperly loaded portals";
    }
}
