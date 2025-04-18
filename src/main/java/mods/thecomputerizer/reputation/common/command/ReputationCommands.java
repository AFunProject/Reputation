package mods.thecomputerizer.reputation.common.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.PlayerFactionHandler;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.network.PacketFlipDebug;
import mods.thecomputerizer.reputation.network.ReputationNetwork;
import net.minecraft.commands.CommandRuntimeException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Objects;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;

public class ReputationCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(MODID)
                        .then(reputationArguments("addreputation",true))
                        .then(reputationArguments("setreputation",false))
                        .then(playerFactionArguments("addplayerfaction",true))
                        .then(playerFactionArguments("removeplayerfaction",false))
                        .then(debugArguments()));
    }

    private static ArgumentBuilder<CommandSourceStack,?> reputationArguments(String type, boolean isAdd) {
        return Commands.literal(type)
                .then(Commands.argument("resourcelocation",ReputationFactionArgument.id())
                        .then(Commands.argument("reputation",IntegerArgumentType.integer())
                                .executes(ctx -> addReputation(ctx,type,isAdd)))
                        .then(Commands.argument("entity",EntityArgument.entity())
                                .then(Commands.argument("reputation",IntegerArgumentType.integer())
                                        .executes(ctx -> addReputation(ctx,type,isAdd))))
                );
    }

    private static ArgumentBuilder<CommandSourceStack,?> playerFactionArguments(String type, boolean isAdd) {
        return Commands.literal(type)
                .then(Commands.argument("resourcelocation",ReputationFactionArgument.id())
                        .then(Commands.argument("reputation",IntegerArgumentType.integer())
                                .executes(ctx -> playerFaction(ctx,type,isAdd)))
                        .then(Commands.argument("entity",EntityArgument.entity())
                                .then(Commands.argument("reputation",IntegerArgumentType.integer())
                                        .executes(ctx -> playerFaction(ctx,type,isAdd))))
                );
    }
    
    private static ArgumentBuilder<CommandSourceStack,?> debugArguments() {
        return Commands.literal("debug")
                .executes(ReputationCommands::flipDebug)
                .then(Commands.argument("value",BoolArgumentType.bool())
                              .executes(ReputationCommands::flipDebug))
                .then(Commands.argument("entity",EntityArgument.entity())
                              .executes(ReputationCommands::flipDebug))
                .then(Commands.argument("value",BoolArgumentType.bool())
                              .then(Commands.argument("entity",EntityArgument.entity())
                                            .executes(ReputationCommands::flipDebug)));
    }

    private static int addReputation(CommandContext<CommandSourceStack> ctx, String type, boolean isAdd)
            throws CommandRuntimeException {
        try {
            String lang = isAdd ? "add_reputation" : "set_reputation";
            boolean hasPlayerSelector = false;
            Player player = null;
            try {
                Entity entity = EntityArgument.getEntity(ctx,"entity");
                if(entity instanceof Player) player = (Player)entity;
                hasPlayerSelector = true;
            } catch (IllegalArgumentException ignored) {
                player = ctx.getSource().getPlayerOrException();
            }
            if(Objects.nonNull(player)) {
                ResourceLocation factionRes = ResourceLocationArgument.getId(ctx, "resourcelocation");
                int amount = IntegerArgumentType.getInteger(ctx,"reputation");
                Faction faction = ReputationHandler.getFaction(factionRes);
                if(Objects.nonNull(faction)) {
                    ReputationHandler.changeReputation(player,faction,amount);
                    if(hasPlayerSelector) sendSuccess(player,lang+".success.player",amount,
                            player.getDisplayName().getString(),factionRes);
                    else sendSuccess(player,lang+".success",amount,factionRes);
                } else throwException("failure.modify.faction",factionRes);
            } else throwException("failure.player");
        } catch(Exception ex) {
            throwException("failure.unknown",type,ex);
        }
        return 1;
    }

    private static int playerFaction(CommandContext<CommandSourceStack> ctx, String type, boolean isAdd) throws CommandRuntimeException {
        try {
            String lang = isAdd ? "add_player_faction" : "remove_player_faction";
            Player player = null;
            try {
                Entity entity = EntityArgument.getEntity(ctx,"entity");
                if(entity instanceof Player) player = (Player)entity;
            } catch (IllegalArgumentException ignored) {
                player = ctx.getSource().getPlayerOrException();
            }
            if(Objects.nonNull(player)) {
                ResourceLocation factionRes = ResourceLocationArgument.getId(ctx, "resourcelocation");
                Faction faction = ReputationHandler.getFaction(factionRes);
                if(Objects.nonNull(faction)) {
                    boolean success;
                    if(isAdd) success = PlayerFactionHandler.addPlayerToFaction(player,faction);
                    else success = PlayerFactionHandler.removePlayerFromFaction(player,faction);
                    sendSuccess(player,lang+(success ? ".success" : ".fail"),player.getDisplayName().getString(),factionRes);
                } else throwException("failure.modify.faction",factionRes);
            } else throwException("failure.player");
        }
        catch(Exception ex) {
            throwException("failure.unknown",type,ex);
        }
        return 1;
    }
    
    private static int flipDebug(CommandContext<CommandSourceStack> ctx) {
        try {
            Player player = null;
            try {
                Entity entity = EntityArgument.getEntity(ctx,"entity");
                if(entity instanceof Player) player = (Player)entity;
            } catch(IllegalArgumentException ignored) {
                player = ctx.getSource().getPlayerOrException();
            }
            if(Objects.nonNull(player)) {
                String value = null;
                try {
                    value = ((Boolean)BoolArgumentType.getBool(ctx,"value")).toString();
                } catch(IllegalArgumentException ignored) {}
                ReputationNetwork.sendToClient(new PacketFlipDebug(value),player);
            }
        } catch(Exception ex) {
            throwException("failure.unknown","debug",ex);
        }
        return 1;
    }

    private static void sendSuccess(Player player, String lang, Object ... parameters) {
        player.sendMessage(new TranslatableComponent("commands."+MODID+"."+lang, parameters), player.getUUID());
    }

    private static void throwException(String lang, Object ... parameters) {
        throw new CommandRuntimeException(new TranslatableComponent("commands."+MODID+"."+lang, parameters));
    }
}