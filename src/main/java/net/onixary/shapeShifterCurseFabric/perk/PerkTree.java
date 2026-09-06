package net.onixary.shapeShifterCurseFabric.perk;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Common Side
public class PerkTree {
    public static class PerkNode {
        public final ResourceLocation perkID;
        public final int tier;
        public final int y;
        public final @Nullable ResourceLocation dependentPerkID;

        public PerkNode(ResourceLocation perkID, int tier, int y, @Nullable ResourceLocation dependentPerkID) {
            this.perkID = perkID;
            this.tier = tier;
            this.y = y;
            this.dependentPerkID = dependentPerkID;
        }
    }

    public final ResourceLocation treeID;
    public final List<PerkNode> perkNodes = new ArrayList<>();
    public final Map<ResourceLocation, PerkNode> perkNodeMap = new HashMap<>();

    public PerkTree(ResourceLocation treeID) {
        this.treeID = treeID;
    }

    public ResourceLocation getID() {
        return treeID;
    }

    public PerkTree addNode(ResourceLocation perkID, int tier, int y, @Nullable ResourceLocation dependentPerkID) {
        return this.addNode(new PerkNode(perkID, tier, y, dependentPerkID));
    }

    public PerkTree addNode(PerkNode perkNode) {
        perkNodes.add(perkNode);
        perkNodeMap.put(perkNode.perkID, perkNode);
        return this;
    }

    public @Nullable PerkNode getNode(ResourceLocation perkID) {
        return perkNodeMap.get(perkID);
    }

    public @Nullable PerkNode getDependentNode(ResourceLocation perkID) {
        PerkNode perkNode = getNode(perkID);
        if (perkNode != null && perkNode.dependentPerkID != null) {
            return getNode(perkNode.dependentPerkID);
        }
        return null;
    }
}
