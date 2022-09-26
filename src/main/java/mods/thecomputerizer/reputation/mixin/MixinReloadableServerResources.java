package mods.thecomputerizer.reputation.mixin;

import mods.thecomputerizer.reputation.Reputation;
import mods.thecomputerizer.reputation.util.FactionListener;
import net.minecraft.server.ReloadableServerResources;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.ServerFunctionLibrary;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.tags.TagManager;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.storage.loot.ItemModifierManager;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.PredicateManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ReloadableServerResources.class)
public class MixinReloadableServerResources {

    @Final
    @Shadow
    private RecipeManager recipes;

    @Final
    @Shadow
    private TagManager tagManager;

    @Final
    @Shadow
    private PredicateManager predicateManager;

    @Final
    @Shadow
    private LootTables lootTables;

    @Final
    @Shadow
    private ItemModifierManager itemModifierManager;

    @Final
    @Shadow
    private ServerAdvancementManager advancements;

    @Final
    @Shadow
    private ServerFunctionLibrary functionLibrary;

    @Inject(at = @At("HEAD"), method = "listeners()Ljava/util/List;", cancellable = true)
    private void listeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        Reputation.logInfo("bruh");
        cir.setReturnValue(List.of(new FactionListener(), this.tagManager, this.predicateManager, this.recipes, this.lootTables, this.itemModifierManager, this.functionLibrary, this.advancements));
    }
}
