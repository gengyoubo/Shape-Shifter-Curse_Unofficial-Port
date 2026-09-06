package net.onixary.shapeShifterCurseFabric.recipes.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.onixary.shapeShifterCurseFabric.recipes.RecipeSerializerRegister;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AlterShapedRecipe extends AlterRecipe {
    public final ShapedRecipePattern pattern;
    public final ItemStack output;
    public final @Nullable Ingredient catalyst;
    public final int recipeTime;
    public final int fuelCostPerTick;

    public final @Nullable ResourceLocation requireAdvancement;

    public AlterShapedRecipe(ResourceLocation id, int width, int height, NonNullList<Ingredient> input, Ingredient catalyst, ItemStack output, int recipeTime, int fuelCostPerTick, ResourceLocation requireAdvancement) {
        this.id = id;
        this.width = width;
        this.height = height;
        this.input = input;
        this.output = output;
        this.catalyst = catalyst;
        this.recipeTime = recipeTime;
        this.fuelCostPerTick = fuelCostPerTick;
        this.requireAdvancement = requireAdvancement;
    }

    @Override
    public int recipeTime() {
        return recipeTime;
    }

    @Override
    public boolean canCraft(@Nullable Player player) {
        if (requireAdvancement == null) {
            return true;
        }
        if (player instanceof ServerPlayer playerEntity) {
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
    }

    private boolean matchesPattern(RecipeInput inv, int offsetX, int offsetY, boolean flipped) {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                int k = i - offsetX;
                int l = j - offsetY;
                Ingredient ingredient = Ingredient.EMPTY;
                if (k >= 0 && l >= 0 && k < this.pattern.width() && l < this.pattern.height()) {
                    if (flipped) {
                        ingredient = this.pattern.ingredients().get(this.pattern.width() - k - 1 + l * this.pattern.width());
                    } else {
                        ingredient = this.pattern.ingredients().get(k + l * this.pattern.width());
                    }
                }
                if (!ingredient.test(inv.getItem(i + j * 3))) {
                    return false;
                }
            }
        }
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

        for (int i = 0; i <= 3 - this.pattern.width(); ++i) {
            for (int j = 0; j <= 3 - this.pattern.height(); ++j) {
                if (this.matchesPattern(recipeInput, i, j, true)) {
                    return true;
                }
                if (this.matchesPattern(recipeInput, i, j, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public int fuelUsage() {
        return fuelCostPerTick;
    }

    @Override
    public ItemStack assemble(RecipeInput recipeInput, HolderLookup.Provider provider) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= this.pattern.width() && height >= this.pattern.height();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return RecipeSerializerRegister.ALTER_SHAPED_RECIPE;
    }

    public static class Serializer implements RecipeSerializer<AlterShapedRecipe> {
        private static final MapCodec<AlterShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(r -> r.pattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(r -> r.output),
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("catalyst").forGetter(r -> Optional.ofNullable(r.catalyst)),
                Codec.INT.optionalFieldOf("time", 200).forGetter(r -> r.recipeTime),
                Codec.INT.optionalFieldOf("fuel_cost", 1).forGetter(r -> r.fuelCostPerTick)
            ).apply(instance, (pattern, output, catalyst, time, fuelCost) -> new AlterShapedRecipe(pattern, output, catalyst.orElse(null), time, fuelCost))
        );

        private static final StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> STREAM_CODEC = StreamCodec.of(
            Serializer::toNetwork, Serializer::fromNetwork
        );

        @Override
        public MapCodec<AlterShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, AlterShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }

        private static AlterShapedRecipe fromNetwork(RegistryFriendlyByteBuf buf) {
            Ingredient catalyst = null;
            if (jsonObject.has("catalyst")) {
                catalyst = Ingredient.fromJson(jsonObject.get("catalyst"), true);
            }
            Identifier requireAdvancement = null;
            if (jsonObject.has("require_advancement")) {
                requireAdvancement = new Identifier(JsonHelper.getString(jsonObject, "require_advancement"));
            }
            int fuelCost = JsonHelper.getInt(jsonObject, "fuel_cost", 1);
            Map<String, Ingredient> map = readSymbols(JsonHelper.getObject(jsonObject, "key"));
            String[] strings = removePadding(getPattern(JsonHelper.getArray(jsonObject, "pattern")));
            int i = strings[0].length();
            int j = strings.length;
            NonNullList<Ingredient> defaultedList = createPatternMatrix(strings, map, i, j);
            ItemStack itemStack = ShapedRecipe.outputFromJson(JsonHelper.getObject(jsonObject, "result"));
            return new AlterShapedRecipe(identifier, i, j, defaultedList, catalyst, itemStack, time, fuelCost, requireAdvancement);
        }

        public AlterShapedRecipe read(Identifier identifier, PacketByteBuf packetByteBuf) {
            Ingredient catalyst = null;
            if (packetByteBuf.readBoolean()) {
                catalyst = Ingredient.fromPacket(packetByteBuf);
            }
            Identifier requireAdvancement = null;
            if (packetByteBuf.readBoolean()) {
                requireAdvancement = packetByteBuf.readIdentifier();
            }
            int i = packetByteBuf.readVarInt();
            int j = packetByteBuf.readVarInt();
            NonNullList<Ingredient> defaultedList = NonNullList.ofSize(i * j, Ingredient.EMPTY);
            for(int k = 0; k < defaultedList.size(); ++k) {
                defaultedList.set(k, Ingredient.fromPacket(packetByteBuf));
            }
            ItemStack itemStack = packetByteBuf.readItemStack();
            int time = packetByteBuf.readVarInt();
            int fuelCost = packetByteBuf.readVarInt();
            return new AlterShapedRecipe(identifier, i, j, defaultedList, catalyst, itemStack, time, fuelCost, requireAdvancement);
        }

        private static void toNetwork(RegistryFriendlyByteBuf buf, AlterShapedRecipe r) {
            if (r.catalyst != null) {
                buf.writeBoolean(true);
                Ingredient.CONTENTS_STREAM_CODEC.encode(buf, r.catalyst);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            if (alterRecipe.requireAdvancement != null) {
                packetByteBuf.writeBoolean(true);
                packetByteBuf.writeIdentifier(alterRecipe.requireAdvancement);
            } else {
                packetByteBuf.writeBoolean(false);
            }
            ShapedRecipePattern.STREAM_CODEC.encode(buf, r.pattern);
            ItemStack.STREAM_CODEC.encode(buf, r.output);
            buf.writeVarInt(r.recipeTime);
            buf.writeVarInt(r.fuelCostPerTick);
        }
    }
}
