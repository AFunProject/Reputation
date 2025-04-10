package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHandler;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageWrapperAPI;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;

import static mods.thecomputerizer.reputation.ReputationRef.LOGGER;
import static net.minecraftforge.registries.ForgeRegistries.ENTITIES;

public class ReputationNetwork {
    
    public static void initClient() {
    
    }
    
    public static void initCommon() {
        NetworkHandler.registerMsgToClient(PacketChatIcon.class, PacketChatIcon::new);
        NetworkHandler.registerMsgToClient(PacketFleeIcon.class,PacketFleeIcon::new);
        NetworkHandler.registerMsgToClient(PacketSetIcon.class,PacketSetIcon::new);
        NetworkHandler.registerMsgToClient(PacketSyncChatIcons.class,PacketSyncChatIcons::new);
        NetworkHandler.registerMsgToClient(PacketSyncFactionPlayers.class,PacketSyncFactionPlayers::new);
        NetworkHandler.registerMsgToClient(PacketSyncFactions.class,PacketSyncFactions::new);
        NetworkHandler.registerMsgToClient(PacketSyncReputation.class,PacketSyncReputation::new);
    }
    
    public static @Nullable EntityType<?> readEntityType(ByteBuf buf) {
        ResourceLocation location = readResourceLocation(buf);
        return ENTITIES.containsKey(location) ? ENTITIES.getValue(location) : null; //We don't want the default value
    }
    
    @SuppressWarnings("removal")
    public static ResourceLocation readResourceLocation(ByteBuf buf) {
        return new ResourceLocation(NetworkHelper.readString(buf));
    }
    
    public static UUID readUUID(ByteBuf buf) {
        return UUID.fromString(NetworkHelper.readString(buf));
    }
    
    @SuppressWarnings("unchecked")
    public static void sendToClient(MessageAPI<?> msg, Player ... players) {
        if(Objects.isNull(msg) || Objects.isNull(players) || players.length==0) return;
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(NetworkHelper.getDirToClient(), msg);
        if(Objects.isNull(wrapper)) {
            LOGGER.error("Failed to wrap message from server {}",msg);
            return;
        }
        ((MessageWrapperAPI<Player,?>)wrapper).setPlayers(players);
        wrapper.send();
    }
    
    @SuppressWarnings("unchecked")
    public static void sendToClient(MessageAPI<?> msg, Collection<Player> players) {
        if(Objects.isNull(msg) || Objects.isNull(players) || players.isEmpty()) return;
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(NetworkHelper.getDirToClient(),msg);
        if(Objects.isNull(wrapper)) {
            LOGGER.error("Failed to wrap message from server {}",msg);
            return;
        }
        ((MessageWrapperAPI<Player,?>)wrapper).setPlayers(players);
        wrapper.send();
    }
    
    public static void sendToServer(MessageAPI<?> msg) {
        if(Objects.isNull(msg)) return;
        MessageWrapperAPI<?,?> wrapper = NetworkHelper.wrapMessage(NetworkHelper.getDirToServer(),msg);
        if(Objects.isNull(wrapper)) {
            LOGGER.error("Failed to wrap message from client {}",msg);
            return;
        }
        wrapper.send();
    }
    
    public static void writeEntityType(ByteBuf buf, EntityType<?> type) {
        writeResourceLocation(buf,type.getRegistryName());
    }
    
    public static void writeResourceLocation(ByteBuf buf, @Nullable ResourceLocation location) {
        if(Objects.nonNull(location)) NetworkHelper.writeString(buf,location.toString());
        else LOGGER.error("Tried to write null ResourceLocation to packet!");
    }
    
    public static void writeUUID(ByteBuf buf, UUID uuid) {
        NetworkHelper.writeString(buf,uuid.toString());
    }
}