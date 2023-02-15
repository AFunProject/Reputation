package mods.thecomputerizer.reputation.util;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class NetworkUtil {

    public static void writeString(FriendlyByteBuf buf, String string) {
        buf.writeInt(string.length());
        buf.writeCharSequence(string, StandardCharsets.UTF_8);
    }

    public static String readString(FriendlyByteBuf buf) {
        int strLength = buf.readInt();
        return (String)buf.readCharSequence(strLength, StandardCharsets.UTF_8);
    }

    public static void writeEntityType(FriendlyByteBuf buf, EntityType<?> type) {
        ResourceLocation resource = Objects.nonNull(type) ? ForgeRegistries.ENTITIES.getKey(type) : null;
        buf.writeResourceLocation(Objects.nonNull(resource) ? resource : ModDefinitions.getResource("missing"));
    }

    public static Optional<EntityType<?>> readEntityType(FriendlyByteBuf buf) {
        ResourceLocation location = buf.readResourceLocation();
        return location.getPath().matches("missing") ? Optional.empty() :
                Optional.ofNullable(ForgeRegistries.ENTITIES.getValue(location));
    }

    public static <V> void writeGenericList(FriendlyByteBuf buf, List<V> list, BiConsumer<FriendlyByteBuf, V> valFunc) {
        buf.writeInt(list.size());
        for(V val : list) valFunc.accept(buf,val);
    }

    public static <V> List<V> readGenericList(FriendlyByteBuf buf, Function<FriendlyByteBuf, V> valFunc) {
        return IntStream.range(0,buf.readInt()).mapToObj(i -> valFunc.apply(buf)).toList();
    }

    public static <K, V> void writeGenericMap(FriendlyByteBuf buf, Map<K, V> map, BiConsumer<FriendlyByteBuf, K> keyFunc,
                                              BiConsumer<FriendlyByteBuf, V> valFunc) {
        buf.writeInt(map.size());
        for(Map.Entry<K, V> entry : map.entrySet()) {
            keyFunc.accept(buf,entry.getKey());
            valFunc.accept(buf,entry.getValue());
        }
    }

    public static <K, V> Map<K, V> readGenericMap(FriendlyByteBuf buf, Function<FriendlyByteBuf, K> keyFunc,
                                                  Function<FriendlyByteBuf, V> valFunc) {
        return IntStream.range(0,buf.readInt()).mapToObj(i -> Map.entry(keyFunc.apply(buf),valFunc.apply(buf)))
                .collect(Collectors.toMap(Map.Entry::getKey,Map.Entry::getValue));
    }
}
