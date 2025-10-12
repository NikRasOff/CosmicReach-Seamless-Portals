package com.nikrasoff.seamlessportals.commands;

import com.badlogic.gdx.utils.Array;
import finalforeach.cosmicreach.accounts.Account;
import finalforeach.cosmicreach.chat.IChat;
import finalforeach.cosmicreach.chat.commands.Command;

public class TopCommand extends Command {
    Array<Command> portalCommands = new Array<>();

    ListAnchorsCommand anchorsCommand = new ListAnchorsCommand();
    ClearAnchorsCommand clearAnchorsCommand = new ClearAnchorsCommand();
    ClearPortalsCommand clearPortalsCommand = new ClearPortalsCommand();

    public static void register() {
        Command.registerCommand(TopCommand::new, "portal");
    }

    private void addCommand(Command command, Account account, String[] args){
        command.setup(account, args);
        portalCommands.add(command);
    }

    @Override
    public void setup(Account account, String[] args) {
        super.setup(account, args);

        addCommand(anchorsCommand, account, args);
        addCommand(clearAnchorsCommand, account, args);
        addCommand(clearPortalsCommand, account, args);
    }

    private void showHelp(IChat chat){
        chat.addMessage(null, "help: Displays a list of all commands");
        for (Command command : portalCommands){
            chat.addMessage(null, command.getShortDescription());
        }
    }

    @Override
    public void run(IChat chat) {
        super.run(chat);

        String command = hasNextArg() ? getNextArg() : "";

        switch (command) {
            case "anchorClear" -> clearAnchorsCommand.run(chat);
            case "anchorList" -> anchorsCommand.run(chat);
            case "portalClear" -> clearPortalsCommand.run(chat);
            case "help" -> showHelp(chat);
            default -> {
                chat.addMessage(null, "Unknown command: portal " + command);
                chat.addMessage(null, "For a list of available commands, type \"portal help\"");
            }
        }
    }

    @Override
    public String getShortDescription() {
        return "Commands for the Seamless Portals mod. Type \"portal help\" for a list of commands.";
    }
}
