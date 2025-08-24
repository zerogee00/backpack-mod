
package com.example.backpack.client.model;

import com.example.backpack.BackpackMod;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class BackpackModel extends Model {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(BackpackMod.MODID, "backpack"), "main");
    private final ModelPart root;

    public BackpackModel(ModelPart root) {
        super(RenderType::entityCutout);
        this.root = root;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Main pack: 8w x 10h x 4d
        root.addOrReplaceChild("pack",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-4.0F, -10.0F, 2.0F, 8.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // Flap
        root.addOrReplaceChild("flap",
                CubeListBuilder.create()
                        .texOffs(0, 32).addBox(-4.0F, -12.0F, 2.2F, 8.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // Buckle (iron)
        root.addOrReplaceChild("buckle",
                CubeListBuilder.create()
                        .texOffs(40, 32).addBox(-1.0F, -6.0F, 6.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
                PartPose.ZERO);

        // Gold rivets (four small studs on flap/body corners)
        root.addOrReplaceChild("rivet_tl",
                CubeListBuilder.create().texOffs(46, 32).addBox(-3.5F, -10.5F, 3.1F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("rivet_tr",
                CubeListBuilder.create().texOffs(46, 32).addBox( 2.5F, -10.5F, 3.1F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("rivet_bl",
                CubeListBuilder.create().texOffs(46, 32).addBox(-3.5F, -5.0F, 5.9F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);
        root.addOrReplaceChild("rivet_br",
                CubeListBuilder.create().texOffs(46, 32).addBox( 2.5F, -5.0F, 5.9F, 1.0F, 1.0F, 1.0F), PartPose.ZERO);

        // Side straps (vertical)
        root.addOrReplaceChild("strap_l",
                CubeListBuilder.create().texOffs(48, 0).addBox(-2.5F, -10.0F, 2.4F, 1.0F, 10.0F, 0.8F), PartPose.ZERO);
        root.addOrReplaceChild("strap_r",
                CubeListBuilder.create().texOffs(52, 0).addBox( 1.5F, -10.0F, 2.4F, 1.0F, 10.0F, 0.8F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public void render(PoseStack poseStack, net.minecraft.client.renderer.MultiBufferSource buffers, int packedLight, int packedOverlay) {
        var vertexConsumer = buffers.getBuffer(RenderType.entityCutout(getTexture()));
        this.root.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public ResourceLocation getTexture() {
        return ResourceLocation.fromNamespaceAndPath(BackpackMod.MODID, "textures/entity/backpack.png");
    }
}
