package net.lessqq.amidstforge;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

public class AmidstRunCommand extends CommandBase {

    private UIEventHandler eventHandler;

    public AmidstRunCommand(UIEventHandler eventHandler) {
        this.eventHandler = eventHandler;
    }

    @Override
    public String getName() {
        return "amidst";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "amidstforge.command.amidst.usage";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        eventHandler.startAmidst(new IntegratedBiomeProviderAccess());
    }

}
