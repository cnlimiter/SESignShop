package cm.evolvefield.mods.sesignshop;

import cm.evolvefield.mods.sesignshop.core.SSRegistry;
import cn.evolvefield.mods.simpleeco.core.SERegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod("sesignshop")
public class SESignShop {

    public static final String MOD_ID = "sesignshop";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public SESignShop() {
        SSRegistry.ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());

    }


}
