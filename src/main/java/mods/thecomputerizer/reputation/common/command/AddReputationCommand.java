package mods.thecomputerizer.reputation.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class AddReputationCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("addreputation")
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                .then(Commands.argument("reputation", IntegerArgumentType.integer())
                .executes((ctx) -> addReputation(ctx.getSource(), ResourceLocationArgument.getId(ctx,"resourcelocation"), IntegerArgumentType.getInteger(ctx,"reputation")))))
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                .then(Commands.argument("entity", EntityArgument.entity())
                .then(Commands.argument("reputation", IntegerArgumentType.integer())
                .executes((ctx) -> addReputationPlayer(EntityArgument.getEntity(ctx, "entity"), ResourceLocationArgument.getId(ctx,"resourcelocation"), IntegerArgumentType.getInteger(ctx,"reputation")))))));
    }

    private static int addReputation(CommandSourceStack cs, ResourceLocation faction, int set) throws CommandRuntimeException {
        try {
            Player player = cs.getPlayerOrException();
            ReputationHandler.changeReputation(player,ReputationHandler.getFaction(faction),set);
            player.sendMessage(new TextComponent("Added "+set+" to the reputation of faction '"+faction+"'"),player.getUUID());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int addReputationPlayer(Entity e, ResourceLocation faction, int set) throws CommandRuntimeException {
        try {
            if(!(e instanceof Player player)) throw new CommandRuntimeException(new TextComponent("Entity was not a player!"));
            ReputationHandler.changeReputation(player,ReputationHandler.getFaction(faction),set);
            player.sendMessage(new TextComponent("Added "+set+" to the reputation of faction '"+faction+"'"),player.getUUID());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return 1;
    }
}
