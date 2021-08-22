package cn.evolvefield.mods.sesignshop.core.network;


import cn.evolvefield.mods.sesignshop.SESignShop;
import cn.evolvefield.mods.simpleeco.core.network.TestPack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class PacketLoader {
    public static SimpleChannel INSTANCE;
    public static final String VERSION = "1.0";
    private static int ID = 0;

    private ClientWorld level;
    private Minecraft minecraft;

    public static int nextID() {
        return ID++;
    }

    public static void registerMessage() {
        INSTANCE = NetworkRegistry.newSimpleChannel(
                new ResourceLocation(SESignShop.MOD_ID, "main_networking"),
                () -> VERSION,
                (version) -> version.equals(VERSION),
                (version) -> version.equals(VERSION)
        );
        INSTANCE.messageBuilder(TestPack.class, nextID())
                .encoder(TestPack::encode)
                .decoder(TestPack::new)
                .consumer(TestPack::handle)
                .add();
//        INSTANCE.messageBuilder(EditSignPacket.class, nextID())
//                .encoder(EditSignPacket::encode)
//                .decoder(EditSignPacket::new)
//                .consumer(EditSignPacket::handle)
//                .add();

    }





}
