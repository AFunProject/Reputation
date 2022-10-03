package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class Tags {
    public static final TagKey<Item> CURRENCY_ITEMS = registerItemTag("currency_items");
    private static Set<Item> TEMP_CURRENCY_SET = Set.of();

    @SuppressWarnings("SameParameterValue")
    private static TagKey<Item> registerItemTag(String name) {
        return TagKey.create(Registry.ITEM_REGISTRY,new ResourceLocation(ModDefinitions.MODID,name));
    }

    public static void queueCurrencyTagUpdate(Item ... items) {
        TEMP_CURRENCY_SET = Arrays.stream(items).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static CurrencyTagProvider createProvider(DataGenerator generator, Registry<Item> registry, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        return new CurrencyTagProvider(generator, registry, modId, existingFileHelper);
    }

    private static class CurrencyTagProvider extends TagsProvider<Item> {

        private CurrencyTagProvider(DataGenerator generator, Registry<Item> registry, String modId, @Nullable ExistingFileHelper existingFileHelper) {
            super(generator, registry, modId, existingFileHelper);
        }

        @Override
        protected void addTags() {
            tag(CURRENCY_ITEMS).add(TEMP_CURRENCY_SET.toArray(new Item[0]));
        }

        @Override
        public @NotNull String getName() {
            return "currency_items";
        }
    }
}
