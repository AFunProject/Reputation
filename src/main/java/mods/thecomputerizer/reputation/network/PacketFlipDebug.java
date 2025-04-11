package mods.thecomputerizer.reputation.network;

import io.netty.buffer.ByteBuf;
import mods.thecomputerizer.theimpossiblelibrary.api.network.NetworkHelper;
import mods.thecomputerizer.theimpossiblelibrary.api.network.message.MessageAPI;
import net.minecraftforge.network.NetworkEvent.Context;

import javax.annotation.Nullable;
import java.util.Objects;

import static mods.thecomputerizer.reputation.client.ClientConfigHandler.DEBUG;

public class PacketFlipDebug extends MessageAPI<Context> {
    
    private final String value;
    
    public PacketFlipDebug(@Nullable String value) {
        super();
        this.value = value;
    }
    
    public PacketFlipDebug(ByteBuf buf) {
        this.value = buf.readBoolean() ? NetworkHelper.readString(buf) : null;
    }
    
    @Override public void encode(ByteBuf buf) {
        boolean hasValue = Objects.nonNull(this.value);
        buf.writeBoolean(hasValue);
        if(hasValue) NetworkHelper.writeString(buf,this.value);
    }
    
    @Override public MessageAPI<Context> handle(Context ctx) {
        DEBUG.set(Objects.nonNull(this.value) ? "true".equalsIgnoreCase(this.value) : !DEBUG.get());
        return null;
    }
}