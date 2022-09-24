package mods.thecomputerizer.reputation.mixin;

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

@SuppressWarnings("MixinAnnotationTarget")
@Mixin(ReloadableServerResources.class)
public class MixinReloadableServerResources {

    @Final
    @Shadow
    private RecipeManager f_206848_;

    @Final
    @Shadow
    private TagManager f_206849_;

    @Final
    @Shadow
    private PredicateManager f_206850_;

    @Final
    @Shadow
    private LootTables f_206851_;

    @Final
    @Shadow
    private ItemModifierManager f_206852_;

    @Final
    @Shadow
    private ServerAdvancementManager f_206853_;

    @Final
    @Shadow
    private ServerFunctionLibrary f_206854_;

    @Inject(at = @At("HEAD"), method = "listeners()Ljava/util/List;", cancellable = true)
    private void listeners(CallbackInfoReturnable<List<PreparableReloadListener>> cir) {
        cir.setReturnValue(List.of(new FactionListener(), this.f_206849_, this.f_206850_, this.f_206848_, this.f_206851_, this.f_206852_, this.f_206854_, this.f_206853_));
    }
}
