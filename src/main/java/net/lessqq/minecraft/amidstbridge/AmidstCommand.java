package net.lessqq.minecraft.amidstbridge;

import net.minecraft.command.ICommandSender;
import net.minecraftforge.server.command.CommandTreeBase;
import net.minecraftforge.server.command.CommandTreeHelp;

public class AmidstCommand extends CommandTreeBase {

  public AmidstCommand() {
    addSubcommand(new AmidstRunCommand());
    addSubcommand(new CommandTreeHelp(this));
  }
  
  @Override
  public String getName() {
    return "amidst";
  }

  @Override
  public String getUsage(ICommandSender sender) {
    return "amidstbridge.command.amidst.usage";
  }

}
