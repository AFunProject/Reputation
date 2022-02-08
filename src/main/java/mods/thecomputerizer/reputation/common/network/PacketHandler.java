package mods.thecomputerizer.reputation.common.network;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import mods.thecomputerizer.reputation.common.ModDefinitions;

public class PacketHandler {

	private  static  final  String  PROTOCOL_VERSION  =  "1.0";
	public static final SimpleChannel NETWORK_INSTANCE = NetworkRegistry.ChannelBuilder
			.named(ModDefinitions.getResource("main"))
			.clientAcceptedVersions ( PROTOCOL_VERSION :: equals)
			.serverAcceptedVersions ( PROTOCOL_VERSION :: equals)
			.networkProtocolVersion (() ->  PROTOCOL_VERSION )
			.simpleChannel ();
	public static int disc = 0;

	public static void initPackets() {	//I really did not like how the packets were done before, so I redid them
		NETWORK_INSTANCE.registerMessage(disc++, SyncFactionsMessage.class, SyncFactionsMessage::encode, SyncFactionsMessage::new, SyncFactionsMessage::handle);
		NETWORK_INSTANCE.registerMessage(disc++, SyncReputationMessage.class, SyncReputationMessage::encode, SyncReputationMessage::new, SyncReputationMessage::handle);
	}

	public static void sendTo(Object message, ServerPlayer player) {
		NETWORK_INSTANCE.sendTo(message, player.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
	}

	@OnlyIn(Dist.CLIENT)
	public static void sendToServer(Object message) {
		NETWORK_INSTANCE.sendToServer(message);
	}

}
