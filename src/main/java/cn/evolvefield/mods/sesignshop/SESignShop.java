package cn.evolvefield.mods.sesignshop;

import cn.evolvefield.mods.sesignshop.init.ModBlocks;
import cn.evolvefield.mods.sesignshop.init.ModItems;
import cn.evolvefield.mods.sesignshop.init.ModTileEntities;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("sesignshop")
public class SESignShop {

    public static final String MOD_ID = "sesignshop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SESignShop() {
        ModItems.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModBlocks.BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModTileEntities.TILE_ENTITIES.register(FMLJavaModLoadingContext.get().getModEventBus());
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }


}
