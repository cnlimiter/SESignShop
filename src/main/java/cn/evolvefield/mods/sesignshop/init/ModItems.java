package cn.evolvefield.mods.sesignshop.init;

import net.minecraft.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import static cn.evolvefield.mods.sesignshop.SESignShop.MOD_ID;


public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);



       //public static final RegistryObject<Item> shopCreate = ITEMS.register("shop_create", ShopCreateItem::new);
}
