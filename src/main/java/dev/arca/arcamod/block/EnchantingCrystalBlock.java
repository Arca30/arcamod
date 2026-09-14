package dev.arca.arcamod.block;

import com.mojang.serialization.MapCodec;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.config.ArcaFeature;
import dev.arca.arcamod.mixin.PlayerAccessor;
import dev.arca.arcamod.registry.ModItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Le cristal d'enchantement.
 *
 * Pose sur une bibliotheque (ou n'importe quelle surface plane), il se charge
 * quand une creature meurt a moins de ArcaBalance.CRYSTAL_CHARGE_RADIUS blocs
 * (voir ModEvents). Une fois charge, un clic droit le vide et relance le
 * tirage des enchantements proposes au joueur par les tables d'enchantement
 * proches.
 *
 * Il a la taille et la forme d'une lanterne.
 */
public class EnchantingCrystalBlock extends Block {

	public static final MapCodec<EnchantingCrystalBlock> CODEC = simpleCodec(EnchantingCrystalBlock::new);

	/** Vrai quand le cristal a bu une mort et peut servir. */
	public static final BooleanProperty CHARGED = BooleanProperty.create("charged");

	private static final VoxelShape SHAPE = Block.box(5.0, 0.0, 5.0, 11.0, 7.0, 11.0);

	public EnchantingCrystalBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.stateDefinition.any().setValue(CHARGED, false));
	}

	@Override
	protected MapCodec<EnchantingCrystalBlock> codec() {
		return CODEC;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(CHARGED);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return SHAPE;
	}

	@Override
	protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		BlockPos below = pos.below();
		return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
	}

	@Override
	protected BlockState updateShape(BlockState state, LevelReader level,
			net.minecraft.world.level.ScheduledTickAccess ticks, BlockPos pos, Direction direction,
			BlockPos neighbourPos, BlockState neighbourState, RandomSource random) {
		return !state.canSurvive(level, pos)
				? Blocks.AIR.defaultBlockState()
				: super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbourState, random);
	}

	/** Clic molette en creatif : on recupere la version qui correspond a l'etat. */
	@Override
	protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
		return new ItemStack(state.getValue(CHARGED) ? ModItems.CHARGED_ENCHANTING_CRYSTAL : ModItems.ENCHANTING_CRYSTAL);
	}

	/** Charge le cristal s'il ne l'est pas deja. Renvoie false sinon. */
	public static boolean charge(LevelAccessor level, BlockPos pos) {
		BlockState state = level.getBlockState(pos);

		if (!(state.getBlock() instanceof EnchantingCrystalBlock) || state.getValue(CHARGED)) {
			return false;
		}

		level.setBlock(pos, state.setValue(CHARGED, true), 3);
		level.playSound(null, pos, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 0.8F, 1.4F);
		return true;
	}

	@Override
	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
			BlockHitResult hit) {
		if (!ArcaFeature.ENCHANTING_CRYSTAL.isEnabled()) {
			return InteractionResult.PASS;
		}

		if (!state.getValue(CHARGED)) {
			if (!level.isClientSide()) {
				player.sendSystemMessage(Component.translatable("message.arcamod.crystal_not_charged")
						.withStyle(ChatFormatting.GRAY));
			}

			return InteractionResult.CONSUME;
		}

		if (!hasEnchantingTableNearby(level, pos)) {
			if (!level.isClientSide()) {
				player.sendSystemMessage(Component.translatable("message.arcamod.crystal_no_table",
						ArcaBalance.CRYSTAL_TABLE_RADIUS).withStyle(ChatFormatting.GRAY));
			}

			return InteractionResult.CONSUME;
		}

		if (!level.isClientSide()) {
			// Les offres d'une table dependent de la graine d'enchantement du
			// joueur : la changer revient a retirer entierement le pool.
			((PlayerAccessor) player).arcamod$setEnchantmentSeed(player.getRandom().nextInt());

			level.setBlock(pos, state.setValue(CHARGED, false), 3);
			level.playSound(null, pos, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.9F, 1.2F);
			player.sendSystemMessage(Component.translatable("message.arcamod.crystal_reroll")
					.withStyle(ChatFormatting.LIGHT_PURPLE));
		}

		spawnActivationParticles(level, pos);
		return InteractionResult.SUCCESS;
	}

	private static boolean hasEnchantingTableNearby(Level level, BlockPos pos) {
		int radius = ArcaBalance.CRYSTAL_TABLE_RADIUS;

		for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius),
				pos.offset(radius, radius, radius))) {
			if (level.getBlockState(candidate).is(Blocks.ENCHANTING_TABLE)) {
				return true;
			}
		}

		return false;
	}

	private static void spawnActivationParticles(Level level, BlockPos pos) {
		RandomSource random = level.getRandom();

		for (int i = 0; i < 12; i++) {
			level.addParticle(ParticleTypes.ENCHANT,
					pos.getX() + 0.5, pos.getY() + 0.4, pos.getZ() + 0.5,
					(random.nextDouble() - 0.5) * 0.6, random.nextDouble() * 0.4, (random.nextDouble() - 0.5) * 0.6);
		}
	}

	/** Quelques particules discretes tant qu'il est charge. */
	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (!state.getValue(CHARGED) || random.nextInt(4) != 0) {
			return;
		}

		level.addParticle(ParticleTypes.ENCHANT,
				pos.getX() + 0.3 + random.nextDouble() * 0.4,
				pos.getY() + 0.3 + random.nextDouble() * 0.3,
				pos.getZ() + 0.3 + random.nextDouble() * 0.4,
				0.0, 0.02, 0.0);
	}
}
