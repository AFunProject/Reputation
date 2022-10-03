package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
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

import java.util.Objects;

import static mods.thecomputerizer.reputation.common.registration.Items.FACTION_BAG;

public class Recipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTRY = DeferredRegister.create(Registry.RECIPE_SERIALIZER_REGISTRY, ModDefinitions.MODID);
    public static RegistryObject<RecipeSerializer<?>> CURRENCY_RECIPE_SERIALIZER = register("currency_recipe");

    public static void register(IEventBus bus) {
        RECIPE_REGISTRY.register(bus);
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<RecipeSerializer<?>> register(String name) {
        return RECIPE_REGISTRY.register(name,() -> new SimpleRecipeSerializer<>(DoughnutRecipe::new));
    }
    private static class DoughnutRecipe extends CustomRecipe {
        private String assembledWith;

        public DoughnutRecipe(ResourceLocation id) {
            super(id);
        }

        @Override
        public boolean matches(@NotNull CraftingContainer container, @NotNull Level level) {
            for(int i = 0; i <= container.getHeight()-3; i++)
                for(int j = 0; j <= container.getWidth()-3; j++) {
                    int flatIndex = HelperMethods.flatIndex(container.getWidth(),j,i);
                    if (!container.getItem(flatIndex).isEmpty())
                        return matches(container, container.getItem(flatIndex), j, i);
                }
            return false;
        }

        private boolean matches(@NotNull CraftingContainer container, ItemStack topLeft, int x, int y) {
            if(!topLeft.isEmpty() && topLeft.is(Tags.CURRENCY_ITEMS) && x>=0) {
                if(!container.getItem(HelperMethods.flatIndex(container.getWidth(),x+1,y+1)).is(Items.LEATHER)) return false;
                for(int i = 0; i <= y; i++) {
                    for(int j = 0; j <= container.getWidth()-3; j++) {
                        if(j==x+1 && i==y+1) continue;
                        int temp = i*container.getWidth()+j;
                        if((j<x || j>x+2 || i<y || i>y+2) && !container.getItem(temp).isEmpty()) return false;
                        if(!container.getItem(temp).is(topLeft.getItem())) return false;
                    }
                }
                this.assembledWith = Objects.requireNonNull(topLeft.getItem().getRegistryName()).toString();
                return true;
            } return false;
        }

        @Override
        @NotNull
        public ItemStack assemble(@NotNull CraftingContainer container) {
            ItemStack stack = new ItemStack(FACTION_BAG.get());
            if(Objects.nonNull(this.assembledWith)) stack.getOrCreateTag().putString("currency_item",this.assembledWith);
            return stack;
        }

        @Override
        public boolean canCraftInDimensions(int width, int height) {
            return width >= 3 && height >= 3;
        }

        @Override
        @NotNull
        public RecipeSerializer<?> getSerializer() {
            return CURRENCY_RECIPE_SERIALIZER.get();
        }
    }
}
