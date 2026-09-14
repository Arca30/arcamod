package dev.arca.arcamod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.arca.arcamod.ArcaBalance;
import dev.arca.arcamod.entity.ThrownDagger;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Affiche la dague lancee comme le trident : orientee dans le sens du vol,
 * lame en avant.
 *
 * Convention importante : la position de l'entite EST la pointe de la lame
 * (c'est ce que calcule ThrownDagger a l'impact). Le rendu dessine donc la
 * dague entierement derriere ce point.
 *
 * Le modele d'item est rendu SANS transformation d'affichage
 * (ItemDisplayContext.NONE). L'ancien rendu utilisait GROUND, dont le
 * "translation": [0, 2, 0] de item/generated decalait toute la dague de
 * 2 pixels perpendiculairement a la lame : c'est ce qui la faisait flotter
 * au-dessus du sol au lieu d'y etre plantee.
 *
 * Le modele est plat, dans le plan XY, centre sur l'origine, lame vers le
 * coin haut-droit. On l'aligne sur la trajectoire (comme la fleche et le
 * trident : lacet puis tangage, l'axe X local pointant vers l'avant), puis on
 * tourne de BLADE_ANGLE pour que la diagonale de la texture pointe vers
 * l'avant.
 */
public class ThrownDaggerRenderer extends EntityRenderer<ThrownDagger, ThrownDaggerRenderState> {

	// Reglages visuels : ArcaBalance.DAGGER_RENDER_*.
	private static final float SCALE = ArcaBalance.DAGGER_RENDER_SCALE;
	private static final float TIP_PIXEL_INSET = ArcaBalance.DAGGER_RENDER_TIP_PIXEL_INSET;
	private static final float TIP_EXTRA_OFFSET = ArcaBalance.DAGGER_RENDER_TIP_EXTRA_OFFSET;

	/** La lame est dessinee en diagonale dans la texture : on la redresse. */
	private static final float BLADE_ANGLE = -45.0F;

	/**
	 * Distance, dans le modele non mis a l'echelle, entre le centre et la
	 * pointe le long de la diagonale : (8 - inset - 0.5) pixels par axe,
	 * multiplie par racine de 2.
	 */
	private static final float TIP_DISTANCE = (8.0F - TIP_PIXEL_INSET - 0.5F) / 16.0F * Mth.SQRT_OF_TWO;

	private final ItemModelResolver itemModelResolver;

	public ThrownDaggerRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public ThrownDaggerRenderState createRenderState() {
		return new ThrownDaggerRenderState();
	}

	@Override
	public void extractRenderState(ThrownDagger entity, ThrownDaggerRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.NONE, entity);
	}

	@Override
	public void submit(ThrownDaggerRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		poseStack.pushPose();

		// Meme enchainement que la fleche et le trident : lacet puis tangage.
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));

		// On recule la dague pour que sa pointe tombe pile sur l'origine
		// (= la position de l'entite), puis on redresse la diagonale.
		poseStack.translate(TIP_EXTRA_OFFSET - TIP_DISTANCE * SCALE, 0.0F, 0.0F);
		poseStack.mulPose(Axis.ZP.rotationDegrees(BLADE_ANGLE));
		poseStack.scale(SCALE, SCALE, SCALE);

		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();

		super.submit(state, poseStack, collector, camera);
	}
}
