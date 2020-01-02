package net.lessqq.amidstforge;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class AmidstRunCommand {

    private UIEventHandler eventHandler;

    public AmidstRunCommand(UIEventHandler eventHandler, CommandDispatcher<CommandSource> dispatcher) {
        this.eventHandler = eventHandler;
        dispatcher.register(Commands.literal("amidst").executes(this::execute));
    }

    private int execute(CommandContext<CommandSource> ctx) {
        eventHandler.startAmidst(new IntegratedMinecraftInterface());
        return 0;
    }

}
