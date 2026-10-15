package tactical_loadout.gear;

//? if >=1.20.5 {
import com.mojang.serialization.MapCodec;
//?}
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import tactical_loadout.registry.ModSounds;

/** A wire with tin cans across a passage: anything that walks through it sets off a loud rattle. */
public class TripwireAlarmBlock extends HorizontalDirectionalBlock {
    //? if >=1.20.5 {
    public static final MapCodec<TripwireAlarmBlock> CODEC = simpleCodec(TripwireAlarmBlock::new);
    //?}
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    private static final int REARM_TICKS = 30;
    private static final VoxelShape ACROSS_X = Block.box(0, 0, 6.5, 16, 4, 9.5);
    private static final VoxelShape ACROSS_Z = Block.box(6.5, 0, 0, 9.5, 4, 16);

    public TripwireAlarmBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TRIGGERED, false));
    }

    //? if >=1.20.5 {
    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }
    //?}

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    // The wire runs across the placer's view, so it blocks the passage they are looking down.
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    //? if >=1.20.5 {
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    //?} else {
    /*public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    *///?}
        return state.getValue(FACING).getAxis() == Direction.Axis.Z ? ACROSS_X : ACROSS_Z;
    }

    @Override
    //? if >=1.20.5 {
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    //?} else {
    /*public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
    *///?}
        return Shapes.empty();
    }

    @Override
    //? if >=1.20.5 {
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    //?} else {
    /*public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
    *///?}
        return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    //? if >=1.20.5 {
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos,
                                     BlockPos neighborPos) {
    //?} else {
    /*public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos,
                                  BlockPos neighborPos) {
    *///?}
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    //? if >=1.20.5 {
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    //?} else {
    /*public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
    *///?}
        if (level.isClientSide || state.getValue(TRIGGERED) || !(entity instanceof LivingEntity) || entity.isSpectator()) {
            return;
        }
        if (!entity.getBoundingBox().intersects(getShape(state, level, pos, CollisionContext.empty()).bounds().move(pos))) {
            return;
        }
        level.setBlock(pos, state.setValue(TRIGGERED, true), Block.UPDATE_ALL);
        level.scheduleTick(pos, this, REARM_TICKS);
        level.playSound(null, pos, ModSounds.TRIPWIRE_RATTLE.get(), SoundSource.BLOCKS, 4.0F, 0.9F + level.getRandom().nextFloat() * 0.2F);
        level.gameEvent(entity, GameEvent.BLOCK_ACTIVATE, pos);
    }

    @Override
    //? if >=1.20.5 {
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    //?} else {
    /*public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    *///?}
        if (state.getValue(TRIGGERED)) {
            level.setBlock(pos, state.setValue(TRIGGERED, false), Block.UPDATE_ALL);
        }
    }
}
