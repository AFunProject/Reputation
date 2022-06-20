package mods.thecomputerizer.reputation.common.objects.items;

import mods.thecomputerizer.reputation.api.Faction;
import net.minecraft.world.item.Item;

public class FactionCurrencyBag extends Item {

    private Faction faction;
    public FactionCurrencyBag(Properties properties) {
        super(properties);
    }
}
