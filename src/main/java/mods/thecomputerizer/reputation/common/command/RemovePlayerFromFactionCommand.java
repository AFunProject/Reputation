package mods.thecomputerizer.reputation.common.command;

import com.mojang.brigadier.CommandDispatcher;
import mods.thecomputerizer.reputation.api.PlayerFactionHandler;
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

public class RemovePlayerFromFactionCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("removeplayerfaction")
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                        .executes((ctx) -> removeSelf(ctx.getSource(), ResourceLocationArgument.getId(ctx,"resourcelocation"))))
                .then(Commands.argument("resourcelocation", ReputationFactionArgument.id())
                        .then(Commands.argument("entity", EntityArgument.entity())
                                .executes((ctx) -> removePlayer(EntityArgument.getEntity(ctx, "entity"), ResourceLocationArgument.getId(ctx,"resourcelocation"))))));
    }

    private static int removeSelf(CommandSourceStack cs, ResourceLocation faction) throws CommandRuntimeException {
        try {
            Player player = cs.getPlayerOrException();
            if(PlayerFactionHandler.removePlayerFromFaction(player, ReputationHandler.getFaction(faction)))
                player.sendMessage(new TextComponent("Added player "+player.getDisplayName()+" to the faction '"+faction+"'"),player.getUUID());
            else player.sendMessage(new TextComponent("Player "+player.getDisplayName()+" is not in faction '"+faction+"'!"),player.getUUID());
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    private static int removePlayer(Entity e, ResourceLocation faction) throws CommandRuntimeException {
        try {
            if(!(e instanceof Player player)) throw new CommandRuntimeException(new TextComponent("Entity was not a player!"));
            PlayerFactionHandler.removePlayerFromFaction(player, ReputationHandler.getFaction(faction));
            player.sendMessage(new TextComponent("Added player "+player.getDisplayName()+" to the faction '"+faction+"'"),player.getUUID());
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
        return 1;
    }
}
