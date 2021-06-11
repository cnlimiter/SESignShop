package cn.evolvefield.mods.sesignshop.core.items;

import cn.evolvefield.mods.simpleeco.core.SETab;
import cn.evolvefield.mods.simpleeco.utils.LoreUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class ShopCreateItem extends Item {
    public ShopCreateItem() {
        super(new Properties()
                .tab(SETab.itemTab)
                .stacksTo(1)

        );
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable World world, List<ITextComponent> tooltip, ITooltipFlag tag) {

        tooltip.add(new TranslationTextComponent("tooltip.item.shopCreate.use"));


    }


}
