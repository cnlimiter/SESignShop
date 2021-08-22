package cn.evolvefield.mods.sesignshop.init.events;

import cn.evolvefield.mods.sesignshop.client.gui.ShopSignGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class ModClientEvent {

//    @SubscribeEvent
//    public static void onOverlayRender(RenderGameOverlayEvent event) {
//        event.getPhase().invoke();
//        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) {
//            return;
//        }
//        if (Minecraft.getInstance().player == null ) {
//            return;
//        }
//        if (doesSignHaveCommand())
//        ShopSignGUI shopSignGUI = new ShopSignGUI(event.getMatrixStack());
//        shopSignGUI.render();
//    }


    private static boolean doesSignHaveCommand(SignTileEntity sign) {
        for(ITextComponent itextcomponent : sign.messages) {
            Style style = itextcomponent == null ? null : itextcomponent.getStyle();
            if (style != null && style.getClickEvent() != null) {
                ClickEvent clickevent = style.getClickEvent();
                if (clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                    return true;
                }
            }
        }

        return false;
    }

}
