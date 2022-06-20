package mods.thecomputerizer.reputation.common.registration;

import mods.thecomputerizer.reputation.common.ModDefinitions;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Consumer;

public class Recipes {
    public static DeferredRegister<RecipeSerializer<?>> RECIPE_REGISTRY = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ModDefinitions.MODID);

    public static void register(IEventBus bus) {
        RECIPE_REGISTRY.register(bus);
    }

    public static void addDoughnutRecipe(Item ring, Item center, Item output, int count, ResourceLocation id, Consumer<FinishedRecipe> recipeConsumer) {
        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(output,count);
        builder.define('A',ring).define('B',center);
        builder.pattern("AAA").pattern("ABA").pattern("AAA");
        builder.unlockedBy("has_currency_item", new InventoryChangeTrigger.TriggerInstance(EntityPredicate.Composite.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, MinMaxBounds.Ints.ANY, new ItemPredicate[]{ItemPredicate.Builder.item().of(ring).build()}));
        builder.save(recipeConsumer,id);
    }
}
