package cn.evolvefield.mods.sesignshop;

import cn.evolvefield.mods.sesignshop.core.SSRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("sesignshop")
public class SESignShop {

    public static final String MOD_ID = "sesignshop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SESignShop() {
        SSRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

    }


}
