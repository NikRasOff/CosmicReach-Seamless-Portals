package com.nikrasoff.seamlessportals.commands;

import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;

public class TopCommand extends Command {

    ListAnchorsCommand anchorsCommand = new ListAnchorsCommand();
    ClearAnchorsCommand clearAnchorsCommand = new ClearAnchorsCommand();

    public static void register() {
        Command.registerCommand(TopCommand::new, "portal");
    }

    @Override
    public void setup(Account account, String[] args) {
        super.setup(account, args);

        anchorsCommand.setup(account, args);
        clearAnchorsCommand.setup(account, args);
    }

    @Override
    public void run(IChat chat) {
        super.run(chat);

        String command = getNextArg();

        switch (command) {
            case "anchorClear" -> clearAnchorsCommand.run(chat);
            case "anchorList" -> anchorsCommand.run(chat);
            default -> chat.addMessage(null, "unknown command: portal " + command);
        }
    }

    @Override
    public String getShortDescription() {
        return "All of this mod's commands";
    }
}
