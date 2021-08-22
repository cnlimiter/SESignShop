package cn.evolvefield.mods.sesignshop.init.events;

import cn.evolvefield.mods.sesignshop.SESignShop;
import cn.evolvefield.mods.sesignshop.utils.EditSignUtils;
import cn.evolvefield.mods.simpleeco.init.SEConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

@Mod.EventBusSubscriber(modid = SESignShop.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModForgeEvent {
    private static final String[] IS_EDITABLE_FIELDS = {
            "field_145916_j",
            "isEditable",
    };

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event){
        PlayerEntity player = event.getPlayer();
        if(EditSignUtils.canPlayerEdit(player, event.getItemStack())
            && player.isCrouching()
        ){

            TileEntity tileentity = event.getWorld().getBlockEntity(event.getPos());
            if(tileentity instanceof SignTileEntity){
                SignTileEntity sign = (SignTileEntity) tileentity;
                setSignEditable(sign);
                CompoundNBT nbt = sign.serializeNBT();
                if(!nbt.contains("ForgeData") || !nbt.getCompound("ForgeData").contains("shop-activated")) {
                    if (sign.isEditable()) {
                        //sign.serializeNBT().remove("Fo");
                        //sign.getTileData().remove("shop-activated");
                        player.openTextEdit(sign);

                    }

                }
                else{
                    player.sendMessage(new TranslationTextComponent("message.action.not_editable"), Util.NIL_UUID);
                }

            }
        }
    }

    private static void setSignEditable(SignTileEntity sign){
        for(String field : IS_EDITABLE_FIELDS){
            try{
                ObfuscationReflectionHelper.setPrivateValue(SignTileEntity.class, sign, true, field);
                return;
            }
            catch(ObfuscationReflectionHelper.UnableToFindFieldException e){
                SESignShop.LOGGER.debug("Failed to get field {}", field);
            }
        }
        SESignShop.LOGGER.debug("Couldn't set sign editable");
    }
}
