package mods.thecomputerizer.reputation.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.api.capability.IReputation;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.LazyOptional;

public class SetReputationCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("setreputation")
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                .then(Commands.argument("reputation", IntegerArgumentType.integer())
                .executes((ctx) -> setReputation(ctx.getSource(), ResourceLocationArgument.getId(ctx,"resourcelocation"), IntegerArgumentType.getInteger(ctx,"reputation")))))
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                .then(Commands.argument("entity", EntityArgument.entity())
                .then(Commands.argument("reputation", IntegerArgumentType.integer())
                .executes((ctx) -> setReputationPlayer(EntityArgument.getEntity(ctx, "entity"), ResourceLocationArgument.getId(ctx,"resourcelocation"), IntegerArgumentType.getInteger(ctx,"reputation")))))));
    }

    private static int setReputation(CommandSourceStack cs, ResourceLocation faction, int set) throws CommandRuntimeException {
        try {
            Player player = cs.getPlayerOrException();
            LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
            if (optional.isPresent() && optional.resolve().isPresent()) {
                IReputation reputation = optional.resolve().get();
                reputation.setReputation(player,ReputationHandler.getFaction(faction),set);
            }
            player.sendMessage(new TextComponent("Set reputation for faction '"+faction+"' to "+set),player.getUUID());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int setReputationPlayer(Entity e, ResourceLocation faction, int set) throws CommandRuntimeException {
        try {
            if(!(e instanceof Player player)) throw new CommandRuntimeException(new TextComponent("Entity was not a player!"));
            LazyOptional<IReputation> optional = player.getCapability(ReputationHandler.REPUTATION_CAPABILITY);
            if (optional.isPresent() && optional.resolve().isPresent()) {
                IReputation reputation = optional.resolve().get();
                reputation.setReputation(player,ReputationHandler.getFaction(faction),set);
            }
            player.sendMessage(new TextComponent("Set reputation for faction '"+faction+"' to "+set),player.getUUID());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return 1;
    }

}
