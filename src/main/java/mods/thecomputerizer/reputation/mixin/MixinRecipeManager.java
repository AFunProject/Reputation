package mods.thecomputerizer.reputation.mixin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mods.thecomputerizer.reputation.api.ReputationHandler;
import mods.thecomputerizer.reputation.common.ModDefinitions;
import mods.thecomputerizer.reputation.common.registration.Recipes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(RecipeManager.class)
public class MixinRecipeManager {

    @Inject(at = @At("HEAD"), method = "apply(Ljava/util/Map;Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)V")
    private void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller profiler, CallbackInfo ci) {
        for(Item item : ReputationHandler.FACTION_CURRENCY_MAP.keySet()) {
            ResourceLocation recipeID = new ResourceLocation(ModDefinitions.MODID, ReputationHandler.FACTION_CURRENCY_MAP.get(item).getName()+"_faction_currency");
            map.put(recipeID,createJsonRecipeForCurrencyItem(item,recipeID));
        }
    }

    private JsonElement createJsonRecipeForCurrencyItem(Item item, ResourceLocation recipeID) {
        final JsonElement[] ret = {new JsonObject()};
        Recipes.addDoughnutRecipe(item, Items.LEATHER, mods.thecomputerizer.reputation.common.registration.Items.FACTION_BAG.get(), 1, recipeID,
                (recipe) -> {
                    JsonObject output = recipe.serializeRecipe();
                    JsonObject result = output.getAsJsonObject("result");
                    JsonObject nbtData = new JsonObject();
                    JsonObject nbtItemData = new JsonObject();
                    nbtItemData.addProperty("id",item.getRegistryName().toString());
                    nbtData.add("Item",nbtItemData);
                    result.add("nbt",nbtData);
                    output.remove("result");
                    output.add("result",result);
                    ret[0] = output;
                });
        return ret[0];
    }
}
