package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class TagKeys {
    public static final DeferredRegister<EntityType<?>> entityTypeDeferredRegister = DeferredRegister.create(ForgeRegistries.Keys.ENTITY_TYPES.location(), ModDefinitions.MODID);
    public static TagKey<EntityType<?>> FLEE;
    public static TagKey<EntityType<?>> PASSIVE_NEUTRAL;
    public static TagKey<EntityType<?>> PASSIVE_GOOD;
    public static TagKey<EntityType<?>> HOSTILE;

    public static void init(IEventBus eventBus) {
        entityTypeDeferredRegister.register(eventBus);
        FLEE = entityTypeDeferredRegister.createTagKey("flee");
        PASSIVE_NEUTRAL = entityTypeDeferredRegister.createTagKey("passive_neutral");
        PASSIVE_GOOD = entityTypeDeferredRegister.createTagKey("passive_good");
        HOSTILE = entityTypeDeferredRegister.createTagKey("hostile");
    }

}
