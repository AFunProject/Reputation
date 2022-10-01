package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTRY = DeferredRegister.create(Registry.RECIPE_SERIALIZER_REGISTRY, ModDefinitions.MODID);
    public static RegistryObject<RecipeSerializer<?>> CURRENCY_RECIPE_SERIALIZER = register("currency_recipe");

    private static final List<Item> CURRENCY_ITEMS = new ArrayList<>();

    public static void register(IEventBus bus) {
        RECIPE_REGISTRY.register(bus);
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<RecipeSerializer<?>> register(String name) {
        return RECIPE_REGISTRY.register(name,() -> new SimpleRecipeSerializer<>(DoughnutRecipe::new));
    }

    public static void addCurrencyItem(Item item) {
        CURRENCY_ITEMS.add(item);
    }

    public static void resetCurrencyList() {
        CURRENCY_ITEMS.clear();
    }

    private static class DoughnutRecipe extends CustomRecipe {

        public DoughnutRecipe(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
            Item topLeft = container.getItem(0).getItem();
            if(CURRENCY_ITEMS.contains(topLeft)) {
                for (int i = 1; i < container.getContainerSize(); ++i) {
                    if(i==4) {
                        if(container.getItem(i) == ItemStack.EMPTY || container.getItem(i).getItem() != Items.EMERALD) return false;
                    } else if(container.getItem(i) == ItemStack.EMPTY || container.getItem(i).getItem() != topLeft) return false;
                }
            } else return false;
            return true;
        }

        @Override
        @NotNull
        public ItemStack assemble(@NotNull CraftingContainer container) {
            ItemStack stack = new ItemStack(mods.thecomputerizer.reputation.common.registration.Items.FACTION_BAG.get());
            ResourceLocation topLeft = container.getItem(0).getItem().getRegistryName();
            if(topLeft!=null) {
                stack.getOrCreateTag().putString("currency_item",topLeft.toString());
                return stack;
            }
            return ItemStack.EMPTY;
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width==3 && height==3;
        }

        @Override
        @NotNull
        public RecipeSerializer<?> getSerializer() {
            return CURRENCY_RECIPE_SERIALIZER.get();
        }
    }
}
