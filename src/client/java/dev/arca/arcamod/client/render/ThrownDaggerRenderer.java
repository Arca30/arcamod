package dev.arca.arcamod.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.arca.arcamod.entity.ThrownDagger;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * Affiche la dague lancee comme une fleche : orientee dans le sens du vol,
 * lame en avant, et non pas face au joueur comme une boule de neige.
 *
 * Le modele d'item est plat, dessine dans le plan XY, lame vers le coin
 * haut-droit. On le fait donc pivoter deux fois : d'abord pour l'aligner sur
 * la trajectoire (comme une fleche), puis de BLADE_ANGLE pour que la diagonale
 * de la texture pointe vers l'avant.
 */
public class ThrownDaggerRenderer extends EntityRenderer<ThrownDagger, ThrownDaggerRenderState> {

	/** La lame est dessinee en diagonale dans la texture : on la redresse. */
	private static final float BLADE_ANGLE = -45.0F;

	/** Recule la dague pour que la pointe, et non le centre, soit au contact. */
	private static final float TIP_OFFSET = -0.3F;

	private static final float SCALE = 1.1F;

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
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
	}

	@Override
	public void submit(ThrownDaggerRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
			CameraRenderState camera) {
		poseStack.pushPose();

		// Meme enchainement qu'une fleche : lacet puis tangage.
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot));
		poseStack.mulPose(Axis.ZP.rotationDegrees(BLADE_ANGLE));
		poseStack.translate(TIP_OFFSET, 0.0F, 0.0F);
		poseStack.scale(SCALE, SCALE, SCALE);

		state.item.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();

		super.submit(state, poseStack, collector, camera);
	}
}
