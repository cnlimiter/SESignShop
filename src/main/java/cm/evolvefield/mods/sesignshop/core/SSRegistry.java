package cm.evolvefield.mods.sesignshop.core;

import cm.evolvefield.mods.sesignshop.core.items.ShopCreateItem;
import cn.evolvefield.mods.simpleeco.core.items.coins.CopperCoin;
import cn.evolvefield.mods.simpleeco.core.items.coins.GoldCoin;
import cn.evolvefield.mods.simpleeco.core.items.coins.SliverCoin;
import net.minecraft.item.Item;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static cm.evolvefield.mods.sesignshop.SESignShop.MOD_ID;


public class SSRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);




       public static final RegistryObject<Item> shopCreate = ITEMS.register("shop_create", ShopCreateItem::new);
}
