package client.command.commands.gm0;

import client.Character;
import client.Client;
import client.command.Command;
import client.rebirth.Rebirth;

public class RebirthCommand extends Command {
    {
        setDescription("Resets your level and job.");
    }

    @Override
    public void execute(Client client, String[] params) {
        Character player = client.getPlayer();

        if (Rebirth.isEligibleForRebirth(player)) {
            if (params.length > 0) {
                if (toExplorer(params[0])) {
                    Rebirth.rebirthToExplorer(player);

                } else if (toCygnus(params[0])) {
                    Rebirth.rebirthToCygnus(player);

                } else if (toAran(params[0])) {
                    Rebirth.rebirthToAran(player);
                }

            } else {
                // default rebirth to beginner version of current job path
                Rebirth.rebirth(player);
            }
        } else {
            player.dropMessage("You must be level 200 to rebirth.");
        }
    }

    private boolean toExplorer (String param) {
        return (param.equalsIgnoreCase("e") || param.equalsIgnoreCase("explorer") || param.equalsIgnoreCase("beginner"));
    }

    private boolean toCygnus (String param) {
        return (param.equalsIgnoreCase("c") || param.equalsIgnoreCase("cygnus") || param.equalsIgnoreCase("noblesse"));
    }

    private boolean toAran (String param) {
        return (param.equalsIgnoreCase("a") || param.equalsIgnoreCase("aran") || param.equalsIgnoreCase("legend"));

    }
}
