package net.onixary.shapeShifterCurseFabric.blocks;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.onixary.shapeShifterCurseFabric.blocks.block_entity.AltarBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


// 渲染先用透明方案吧 BlockEntity类方块由BlockEntity动态渲染
public class AltarBlock extends BlockWithEntity {
    protected AltarBlock(Settings settings) {
        super(settings);
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new AltarBlockEntity(pos, state);
    }


    @Override
    protected @NotNull InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            this.openScreen(world, pos, player);
            return InteractionResult.CONSUME;
        }
    }

    protected void openScreen(Level world, BlockPos pos, Player player) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof AltarBlockEntity altarBlockEntity) {
            altarBlockEntity.lastUser = player.getUuid();
            player.openHandledScreen(altarBlockEntity);
        }
    }

    @Override
    public @NotNull RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // 1.21 BaseEntityBlock 要求实现抽象的 codec()（用于 Block 的注册/网络同步）
    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(world, type, RegCustomBlock.ALTER_BLOCK_ENTITY);
    }

    @Nullable
    public static <T extends BlockEntity> BlockEntityTicker<T> checkType(World world, BlockEntityType<T> givenType, BlockEntityType<? extends AltarBlockEntity> expectedType) {
        return world.isClient ? null : checkType(givenType, expectedType, (world1, pos, state, blockEntity) -> {
            blockEntity.tick(world1, pos, state, blockEntity);
        });
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return world.isClientSide ? null : createTickerHelper(type, RegCustomBlock.ALTER_BLOCK_ENTITY, (world1, pos, blockState, blockEntity) -> blockEntity.tick(world1, pos, blockState, blockEntity));
    }

    @Override
    public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof AltarBlockEntity altarBlockEntity) {
                if (world instanceof ServerWorld) {
                    ItemScatterer.spawn(world, pos, altarBlockEntity);
                }
            }
            super.onRemove(state, world, pos, newState, moved);
        }
    }
}
