package cn.evolvefield.mods.sesignshop.mixin;


import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.StandingSignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.RenderMaterial;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.tileentity.ChestTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.SignTileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IReorderingProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mixin(SignTileEntityRenderer.class)
public abstract class SignTileEntityRendererMixin  {


    @Shadow
    @Final
    private SignTileEntityRenderer.SignModel signModel;


    @Shadow
    public static RenderMaterial getMaterial(Block p_228877_0_) {
        return null;
    }


    /**
     * @author cnlimiter
     * @reason
     */
    @Overwrite
    public void render(SignTileEntity p_225616_1_, float p_225616_2_, MatrixStack p_225616_3_, IRenderTypeBuffer p_225616_4_, int p_225616_5_, int p_225616_6_) {
        BlockState blockstate = p_225616_1_.getBlockState();
        WallSignBlock  signBlock = (WallSignBlock) blockstate.getBlock();
        CompoundNBT signNbt =  p_225616_1_.getTileData();
        BlockPos backBlock = BlockPos.of(BlockPos.offset(p_225616_1_.getBlockPos().asLong(), blockstate.getValue(signBlock.FACING).getOpposite()));
        TileEntity chestTile = p_225616_1_.getLevel().getBlockEntity(backBlock);
        CompoundNBT chestNbt =  chestTile.getTileData();
        p_225616_3_.pushPose();
        float f = 0.6666667F;
        if (blockstate.getBlock() instanceof StandingSignBlock) {
            p_225616_3_.translate(0.5D, 0.5D, 0.5D);
            float f1 = -((float) (blockstate.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);
            p_225616_3_.mulPose(Vector3f.YP.rotationDegrees(f1));
            this.signModel.stick.visible = true;
        } else {
            p_225616_3_.translate(0.5D, 0.5D, 0.5D);
            float f4 = -blockstate.getValue(WallSignBlock.FACING).toYRot();
            p_225616_3_.mulPose(Vector3f.YP.rotationDegrees(f4));
            p_225616_3_.translate(0.0D, -0.3125D, -0.4375D);
            this.signModel.stick.visible = false;
        }

        p_225616_3_.pushPose();
        p_225616_3_.scale(0.6666667F, -0.6666667F, -0.6666667F);
        RenderMaterial rendermaterial = getMaterial(blockstate.getBlock());
        IVertexBuilder ivertexbuilder = rendermaterial.buffer(p_225616_4_, this.signModel::renderType);
        this.signModel.sign.render(p_225616_3_, ivertexbuilder, p_225616_5_, p_225616_6_);
        this.signModel.stick.render(p_225616_3_, ivertexbuilder, p_225616_5_, p_225616_6_);
        p_225616_3_.popPose();

        p_225616_3_.pushPose();
        if(blockstate.getBlock() instanceof WallSignBlock && chestTile instanceof ChestTileEntity
                && chestNbt.getBoolean("is-shop")
                && signNbt.getBoolean("shop-activated")
                && signNbt.contains("items")
        ){

            p_225616_3_.mulPose(Vector3f.YP.rotationDegrees(180));
            p_225616_3_.translate(0, 1.2d, 0.5d);
            p_225616_3_.scale(0.5F,0.5F,0.5F);
            ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
            List<ItemStack> transItems = new ArrayList<>();
            ListNBT itemsList = signNbt.getList("items", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < itemsList.size(); i++) {
                transItems.add(ItemStack.of(itemsList.getCompound(i)));
            }

            ItemStack stack = new ItemStack(transItems.stream().iterator().next().getItem());
            //ItemStack stack = new ItemStack(Items.DIAMOND);
            IBakedModel ibakedmodel = itemRenderer.getModel(stack, p_225616_1_.getLevel(), null);
            itemRenderer.render(stack, ItemCameraTransforms.TransformType.FIXED, true, p_225616_3_, p_225616_4_, p_225616_5_, p_225616_6_, ibakedmodel);


        }
        p_225616_3_.popPose();

        FontRenderer fontrenderer = Minecraft.getInstance().font;
        float f2 = 0.010416667F;
        p_225616_3_.translate(0.0D, (double) 0.33333334F, (double) 0.046666667F);
        p_225616_3_.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int i = p_225616_1_.getColor().getTextColor();
        double d0 = 0.4D;
        int j = (int) ((double) NativeImage.getR(i) * 0.4D);
        int k = (int) ((double) NativeImage.getG(i) * 0.4D);
        int l = (int) ((double) NativeImage.getB(i) * 0.4D);
        int i1 = NativeImage.combine(0, l, k, j);
        int j1 = 20;

        for (int k1 = 0; k1 < 4; ++k1) {
            IReorderingProcessor ireorderingprocessor = p_225616_1_.getRenderMessage(k1, (p_243502_1_) -> {
                List<IReorderingProcessor> list = fontrenderer.split(p_243502_1_, 90);
                return list.isEmpty() ? IReorderingProcessor.EMPTY : list.get(0);
            });
            if (ireorderingprocessor != null) {
                float f3 = (float) (-fontrenderer.width(ireorderingprocessor) / 2);
                fontrenderer.drawInBatch(ireorderingprocessor, f3, (float) (k1 * 10 - 20), i1, false, p_225616_3_.last().pose(), p_225616_4_, false, 0, p_225616_5_);
            }
        }

        p_225616_3_.popPose();

    }
}
