package cn.evolvefield.mods.sesignshop.client.gui;

import cn.evolvefield.mods.simpleeco.client.gui.base.ButtonRect;
import cn.evolvefield.mods.simpleeco.client.gui.base.TextFieldRect;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.text.TranslationTextComponent;

public class ShopSignGUI extends AbstractGui {

    private ButtonRect shopType;
    private boolean type = true;

    private TextFieldRect shopIntroduce;
    private TextFieldRect shopValue;
    FontRenderer font = Minecraft.getInstance().font;
    MatrixStack matrixStack ;
    private final int width;
    private final int height;

    public ShopSignGUI (MatrixStack matrixStack){
        this.width = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        this.height = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        this.matrixStack = matrixStack;
    }

    public void setMatrixStack(MatrixStack stack) {
        this.matrixStack = stack;
    }

    public void render() {

        drawCenteredString(matrixStack,this.font,new TranslationTextComponent("message.gui.title"),this.width,this.height- 60,0xFFFFFF);
        drawString(matrixStack,this.font,new TranslationTextComponent("message.gui.introduce"), this.width -110 ,this.height-20 ,0xFFFFFF);
        drawString(matrixStack,this.font,new TranslationTextComponent("message.gui.value"), this.width -110 ,this.height+10 ,0xFFFFFF);
        //shopType.setMessage(type ? new TranslationTextComponent("message.gui.type.buy") : new TranslationTextComponent("message.gui.type.sell"));
        drawString(matrixStack,this.font,new TranslationTextComponent("message.gui.type"),this.width -110 ,this.height+10 ,0xFFFFFF);
    }


}
