package mods.thecomputerizer.reputation.registry;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.capability.Faction;
import mods.thecomputerizer.reputation.capability.handlers.ReputationHandler;
import mods.thecomputerizer.reputation.util.HelperMethods;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleRecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Collectors;

import static mods.thecomputerizer.reputation.ReputationRef.MODID;
import static mods.thecomputerizer.reputation.registry.ItemRegistry.FACTION_BAG;
import static net.minecraft.core.Registry.RECIPE_SERIALIZER_REGISTRY;
import static net.minecraft.world.item.Items.LEATHER;

public class RecipeRegistry {
    
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTRY =
            DeferredRegister.create(RECIPE_SERIALIZER_REGISTRY,MODID);
    
    public static RegistryObject<RecipeSerializer<?>> CURRENCY_RECIPE_SERIALIZER = register("currency_recipe");

    private static Set<Item> CURRENCY_SET = Set.of();

    public static void updateCurrencySet(Set<Item> items) {
        CURRENCY_SET = items;
        Reputation.logStringCollection("Adding items to the currency set",
                CURRENCY_SET.stream()
                        .map(ForgeRegistryEntry::getRegistryName)
                        .filter(Objects::nonNull)
                        .map(ResourceLocation::toString)
                        .collect(Collectors.toSet()),10);
    }

    public static void register(IEventBus bus) {
        RECIPE_REGISTRY.register(bus);
    }

    @SuppressWarnings("SameParameterValue")
    private static RegistryObject<RecipeSerializer<?>> register(String name) {
        return RECIPE_REGISTRY.register(name,() -> new SimpleRecipeSerializer<>(DoughnutRecipe::new));
    }

    @MethodsReturnNonnullByDefault @ParametersAreNonnullByDefault
    private static class DoughnutRecipe extends CustomRecipe {
        
        private String faction;

        public DoughnutRecipe(ResourceLocation id) {
            super(id);
        }

        @Override public boolean matches(CraftingContainer container, Level level) {
            for(int i=0;i<=container.getHeight()-3;i++)
                for(int j=0;j<=container.getWidth()-3;j++) {
                    int flatIndex = HelperMethods.flatIndex(container.getWidth(),j,i);
                    if(!container.getItem(flatIndex).isEmpty())
                        return matches(container,container.getItem(flatIndex),j,i);
                }
            return false;
        }

        private boolean matches(CraftingContainer container, ItemStack topLeft, int x, int y) {
            if(!topLeft.isEmpty() && CURRENCY_SET.contains(topLeft.getItem()) && x>=0) {
                if(!container.getItem(HelperMethods.flatIndex(container.getWidth(),x+1,y+1)).is(LEATHER))
                    return false;
                for(int i=0;i<=y;i++) {
                    for(int j=0;j<=container.getWidth()-3;j++) {
                        if(j==x+1 && i==y+1) continue;
                        int temp = HelperMethods.flatIndex(container.getWidth(),j,i);
                        if((j<x || j>x+2 || i<y || i>y+2) && !container.getItem(temp).isEmpty()) return false;
                        if(!container.getItem(temp).is(topLeft.getItem())) return false;
                    }
                }
                Faction faction = ReputationHandler.getFactionFromCurrency(topLeft.getItem());
                if(Objects.nonNull(faction)) this.faction = faction.getID().toString();
                return true;
            } return false;
        }

        @Override public ItemStack assemble(CraftingContainer container) {
            ItemStack stack = new ItemStack(FACTION_BAG.get());
            if(Objects.nonNull(this.faction)) stack.getOrCreateTag().putString("factionID",this.faction);
            return stack;
        }

        @Override public boolean canCraftInDimensions(int width, int height) {
            return width>=3 && height>=3;
        }

        @Override public RecipeSerializer<?> getSerializer() {
            return CURRENCY_RECIPE_SERIALIZER.get();
        }
    }
}