package com.cjburkey.claimchunk.cmd;

import com.cjburkey.claimchunk.cmds.CmdAccess;
import com.cjburkey.claimchunk.cmds.CmdAdminUnclaim;
import com.cjburkey.claimchunk.cmds.CmdAlert;
import com.cjburkey.claimchunk.cmds.CmdAuto;
import com.cjburkey.claimchunk.cmds.CmdClaim;
import com.cjburkey.claimchunk.cmds.CmdHelp;
import com.cjburkey.claimchunk.cmds.CmdInfo;
import com.cjburkey.claimchunk.cmds.CmdList;
import com.cjburkey.claimchunk.cmds.CmdName;
import com.cjburkey.claimchunk.cmds.CmdReload;
import com.cjburkey.claimchunk.cmds.CmdShow;
import com.cjburkey.claimchunk.cmds.CmdUnclaim;

public class Commands {

    public void register(CommandHandler cmd) {
        cmd.registerCommand(CmdHelp.class);
        cmd.registerCommand(CmdInfo.class);
        cmd.registerCommand(CmdList.class);
        cmd.registerCommand(CmdClaim.class);
        cmd.registerCommand(CmdAuto.class);
        cmd.registerCommand(CmdUnclaim.class);
        cmd.registerCommand(CmdAdminUnclaim.class);
        cmd.registerCommand(CmdAccess.class);
        cmd.registerCommand(CmdName.class);
        cmd.registerCommand(CmdShow.class);
        cmd.registerCommand(CmdAlert.class);
        cmd.registerCommand(CmdReload.class);
    }

}
