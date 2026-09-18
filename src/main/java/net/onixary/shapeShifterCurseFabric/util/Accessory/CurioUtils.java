package net.onixary.shapeShifterCurseFabric.util.Accessory;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// XuHaoNan:
// 新API未测试(大概率能用 基本CV旧API) 得等我互联版更新后用我拓展测试 我这边kilt老有问题
public class CurioUtils {
    public static boolean isLoaded() { return false; }

    public static boolean isEquipped(LivingEntity entity, Item item) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return false;
        }
        for (ICurioStacksHandler stacksHandler : handler.getCurios().values()) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                if (stacks.getStackInSlot(i).is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static Map<String, List<ItemStack>> getEntitySlots(LivingEntity entity) {
        Map<String, List<ItemStack>> map = new HashMap<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return map;
        }
        for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
            IDynamicStackHandler stacks = entry.getValue().getStacks();
            List<ItemStack> list = new ArrayList<>();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
            map.put(entry.getKey(), list);
        }
        return map;
    }

    public static List<ItemStack> getEntitySlot(LivingEntity entity, String SlotName) {
        List<ItemStack> list = new ArrayList<>();
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return list;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null) {
            IDynamicStackHandler stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                list.add(stacks.getStackInSlot(i));
            }
        }
        return list;
    }

    public static void setEntitySlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) {
        ICuriosItemHandler handler = CuriosApi.getCuriosInventory(entity).orElse(null);
        if (handler == null) {
            return;
        }
        ICurioStacksHandler stacksHandler = handler.getCurios().get(SlotName);
        if (stacksHandler != null && Index >= 0 && Index < stacksHandler.getStacks().getSlots()) {
            stacksHandler.getStacks().setStackInSlot(Index, stack);
        }
    }

    public static Map<String, List<ItemStack>> getEntityCosmeticSlots(LivingEntity entity) { return Map.of(); }

    public static List<ItemStack> getEntityCosmeticSlot(LivingEntity entity, String SlotName) { return List.of(); }

    public static void setEntityCosmeticSlot(LivingEntity entity, String SlotName, int Index, ItemStack stack) { return; }

    public static Map<String, List<Boolean>> getEntitySlotRenders(LivingEntity entity) { return Map.of(); }

    public static List<Boolean> getEntitySlotRender(LivingEntity entity, String SlotName) { return List.of(); }

    public static void setEntitySlotRender(LivingEntity entity, String SlotName, int Index, boolean render) { return; }
}
