package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.cmds.*;

public final class Commands {

    public static void register(CommandHandler cmd) {
        cmd.registerCommand(CmdHelp.class);
        cmd.registerCommand(CmdInfo.class);
        cmd.registerCommand(CmdList.class);
        cmd.registerCommand(CmdClaim.class);
        cmd.registerCommand(CmdAuto.class);
        cmd.registerCommand(CmdTnt.class);
        cmd.registerCommand(CmdUnclaim.class);
        cmd.registerCommand(CmdUnclaimAll.class);
        cmd.registerCommand(CmdGive.class);
        cmd.registerCommand(CmdAdminUnclaim.class);
        cmd.registerCommand(CmdAdminUnclaimAll.class);
        cmd.registerCommand(CmdAccess.class);
        cmd.registerCommand(CmdName.class);
        cmd.registerCommand(CmdShow.class);
        cmd.registerCommand(CmdAlert.class);
        cmd.registerCommand(CmdReload.class);
        cmd.registerCommand(CmdTeam.class);
    }

}
