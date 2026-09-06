package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class RegPerks {
    public static final HashMap<ResourceLocation, IPerk> PerkRegistry = new HashMap<>();
    public static final HashMap<ResourceLocation, PerkTree> PerkTreeRegistry = new HashMap<>();

    public static ResourceLocation registerPerk(IPerk perk) {
        PerkRegistry.put(perk.getID(), perk);
        return perk.getID();
    }

    public static @Nullable IPerk getPerk(ResourceLocation perkID) {
        return PerkRegistry.get(perkID);
    }

    public static ResourceLocation registerPerkTree(PerkTree perkTree) {
        PerkTreeRegistry.put(perkTree.getID(), perkTree);
        return perkTree.getID();
    }

    public static @Nullable PerkTree getPerkTree(ResourceLocation perkTreeID) {
        return PerkTreeRegistry.get(perkTreeID);
    }
}
