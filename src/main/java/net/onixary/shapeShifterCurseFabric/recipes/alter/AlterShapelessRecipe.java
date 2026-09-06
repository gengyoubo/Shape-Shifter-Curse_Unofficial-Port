package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlterShapelessRecipe extends AlterRecipe {
    public final ItemStack output;
    public final NonNullList<Ingredient> input;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;

    public final @Nullable ResourceLocation requireAdvancement;

    public AlterShapelessRecipe(ResourceLocation id, ItemStack output, NonNullList<Ingredient> input, Ingredient catalyst, int recipeTime, int fuelCostPerTick, ResourceLocation requireAdvancement) {
        this.id = id;
        this.output = output;
        this.input = input;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
    }

    @Override
    public int recipeTime() {
        return recipeTime;
    }

    // 进度锁
    @Override
    public boolean canCraft(PlayerEntity player) {
        return true;
    }

    @Override
    public boolean matches(RecipeInput recipeInput, Level world) {
        if (this.catalyst != null) {
            ItemStack itemStack = recipeInput.getItem(9);
            if (!this.catalyst.test(itemStack)) {
                return false;
            }
        }

        StackedContents recipeMatcher = new StackedContents();
        int i = 0;
        for (int j = 0; j < 9; ++j) {
            ItemStack itemStack = recipeInput.getItem(j);
            if (!itemStack.isEmpty()) {
                ++i;
                recipeMatcher.accountStack(itemStack, 1);
            }
        }
        return i == this.input.size() && recipeMatcher.canCraft(this, null);
    }

    // [1.21.1 修复] Recipe.getIngredients() 默认返回空 NonNullList，StackedContents.canCraft 会读空 ingredients →
    // shapeless 配方匹配必失败。改为返回 input。
    @Override
    public @NotNull NonNullList<Ingredient> getIngredients() {
        return this.input;
    }

    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public @NotNull ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= this.input.size();
    }

    @Override
    public @NotNull ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPELESS_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapelessRecipe> {
        public AlterShapelessRecipe read(ResourceLocation identifier, JsonObject jsonObject) {
            int time = JsonHelper.getInt(jsonObject, "time", 200);
            NonNullList<Ingredient> defaultedList = getIngredients(JsonHelper.getArray(jsonObject, "ingredients"));
            Ingredient catalyst = null;
            if (jsonObject.has("catalyst")) {
                catalyst = Ingredient.fromJson(jsonObject.get("catalyst"), true);
            }
            int fuelCost = JsonHelper.getInt(jsonObject, "fuel_cost", 1);
            if (defaultedList.isEmpty()) {
                throw new JsonParseException("No ingredients for alter shapeless recipe");
            } else if (defaultedList.size() > 9) {
                throw new JsonParseException("Too many ingredients for alter shapeless recipe");
            } else {
                ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
                return new AlterShapelessRecipe(identifier, itemStack, defaultedList, catalyst, time, fuelCost);
            }
        }

        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<AlterShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlterShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapelessRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (buf.readBoolean()) {
                catalyst = Ingredient.CONTENTS_STREAM_CODEC.decode(buf);
            }
            int i = packetByteBuf.readVarInt();
            NonNullList<Ingredient> defaultedList = NonNullList.ofSize(i, Ingredient.EMPTY);
    public boolean canCraft(PlayerEntity player) {
        if (requireAdvancement == null) {
            return true;
        }
        if (player instanceof ServerPlayerEntity playerEntity) {
            MinecraftServer server = playerEntity.getServer();
            if (server == null) {
                return false;
            }
            Advancement advancement = server.getAdvancementLoader().get(requireAdvancement);
            if (advancement == null) {
                return false;
            }
            AdvancementProgress advancementProgress = playerEntity.getAdvancementTracker().getProgress(advancement);
            if (advancementProgress == null) {
                return false;
            }
            return advancementProgress.isDone();
        }
        return false;
        public AlterShapelessRecipe read(Identifier identifier, JsonObject jsonObject) {
            int time = JsonHelper.getInt(jsonObject, "time", 200);
            NonNullList<Ingredient> defaultedList = getIngredients(JsonHelper.getArray(jsonObject, "ingredients"));
            Ingredient catalyst = null;
            if (jsonObject.has("catalyst")) {
                catalyst = Ingredient.fromJson(jsonObject.get("catalyst"), true);
            }
            Identifier requireAdvancement = null;
            if (jsonObject.has("require_advancement")) {
                requireAdvancement = new Identifier(JsonHelper.getString(jsonObject, "require_advancement"));
            }
            int fuelCost = JsonHelper.getInt(jsonObject, "fuel_cost", 1);
            if (defaultedList.isEmpty()) {
                throw new JsonParseException("No ingredients for alter shapeless recipe");
            } else if (defaultedList.size() > 9) {
                throw new JsonParseException("Too many ingredients for alter shapeless recipe");
            } else {
                ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
                return new AlterShapelessRecipe(identifier, itemStack, defaultedList, catalyst, time, fuelCost, requireAdvancement);
            }
        }
            Identifier requireAdvancement = null;
            if (packetByteBuf.readBoolean()) {
                requireAdvancement = packetByteBuf.readIdentifier();
            }
            int i = packetByteBuf.readVarInt();
            NonNullList<Ingredient> defaultedList = NonNullList.ofSize(i, Ingredient.EMPTY);
            for(int j = 0; j < defaultedList.size(); ++j) {
                defaultedList.set(j, Ingredient.fromPacket(packetByteBuf));
            }
            ItemStack itemStack = packetByteBuf.readItemStack();
            int time = packetByteBuf.readVarInt();
            int fuelCost = packetByteBuf.readVarInt();
            return new AlterShapelessRecipe(identifier, itemStack, defaultedList, catalyst, time, fuelCost, requireAdvancement);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapelessRecipe r) {
            if (r.catalyst != null) {
                buf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.catalyst);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            if (shapelessRecipe.requireAdvancement != null) {
                packetByteBuf.writeBoolean(true);
                packetByteBuf.writeIdentifier(shapelessRecipe.requireAdvancement);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            buf.writeVarInt(r.input.size());
            for (Ingredient ingredient : r.input) {
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, ingredient);
            }
            ItemStack.STREAM_CODEC.encode(buf, r.output);
            buf.writeVarInt(r.recipeTime);
            buf.writeVarInt(r.fuelCostPerTick);
        }
    }
}
