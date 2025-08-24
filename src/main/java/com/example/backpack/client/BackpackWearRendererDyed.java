package com.example.backpack.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;
import top.theillusivec4.curios.api.client.ICurioRenderer;

public class BackpackWearRendererDyed implements ICurioRenderer {

    private static final ModelResourceLocation BASE =
        new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath("backpack", "backpack_wear_base"),
            "inventory"
        );
    private static final ModelResourceLocation OVER =
        new ModelResourceLocation(
            ResourceLocation.fromNamespaceAndPath("backpack", "backpack_wear_overlay"),
            "inventory"
        );

    public static void register(ItemStack backpackItem) {
        CuriosRendererRegistry.register(backpackItem.getItem(), () -> new BackpackWearRendererDyed());
    }

    @Override
    public <T extends LivingEntity, M extends EntityModel<T>> void render(
            ItemStack stack,
            SlotContext slotContext,
            PoseStack poseStack,
            RenderLayerParent<T, M> parentModel,
            MultiBufferSource buffers,
            int packedLight,
            float partialTicks,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch) {
        // render BASE + OVER
    }
}
