package cn.evolvefield.mods.sesignshop.client.gui;


import cn.evolvefield.mods.simpleeco.SimpleEco;
import cn.evolvefield.mods.simpleeco.client.gui.base.ButtonRect;
import cn.evolvefield.mods.simpleeco.client.gui.base.GuiScreenBase;
import cn.evolvefield.mods.simpleeco.client.gui.base.TextFieldRect;
import cn.evolvefield.mods.simpleeco.init.SEConfig;
import cn.evolvefield.mods.simpleeco.utils.ScreenUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

//@OnlyIn(Dist.CLIENT)
public class ShopCreateGui extends GuiScreenBase {


    private ButtonRect createShopBtn;
    private ButtonRect shopType;
    private boolean type = true;

    private TextFieldRect shopIntroduce;
    private TextFieldRect shopValue;
    private final TileEntity storage;
    private final SignTileEntity tile;
    private final World world;
    private final BlockPos pos;
    private final CompoundNBT nbt;



    public ShopCreateGui(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt,PlayerEntity player ) {
        super(player,Hand.MAIN_HAND);
        this.storage = storage;
        this.tile = tile;
        this.world = world;
        this.pos = pos;
        this.nbt = nbt;
    }




    private void setType(){
        boolean value = !type;
        type = value;
    }

    private void create(){
        //check if the storage block has an item in the first slot
        LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        ItemStack srcStack = inv.map((c) -> c.getStackInSlot(0)).orElse(ItemStack.EMPTY);
        if (srcStack.equals(ItemStack.EMPTY, true)) return;
        try{
            double price = Math.abs(Double.parseDouble(shopValue.getValue()));
            tile.getTileData().putDouble("price", price);

            if(type == true){
                StringTextComponent buyAction = new StringTextComponent("[buy]");
                buyAction.withStyle(TextFormatting.GREEN);
                tile.setMessage(0, buyAction);
                StringTextComponent desc = new StringTextComponent(shopIntroduce.getValue());
                desc.withStyle(TextFormatting.LIGHT_PURPLE);
                tile.setMessage(2, desc);
                StringTextComponent buyPrice = new StringTextComponent(SEConfig.CURRENCY_SYMBOL.get()+String.valueOf(price));
                buyPrice.withStyle(TextFormatting.GOLD);
                tile.setMessage(3, buyPrice);
                System.out.println("[buy]");
                tile.getTileData().putString("shop-type", "buy");
            }
            else{
                StringTextComponent sellAction = new StringTextComponent("[sell]");
                sellAction.withStyle(TextFormatting.GREEN);
                tile.setMessage(0, sellAction);
                StringTextComponent desc = new StringTextComponent(shopIntroduce.getValue());
                desc.withStyle(TextFormatting.LIGHT_PURPLE);
                tile.setMessage(2, desc);
                StringTextComponent sellPrice = new StringTextComponent(SEConfig.CURRENCY_SYMBOL.get()+String.valueOf(price));
                sellPrice.withStyle(TextFormatting.GOLD);
                tile.setMessage(3, sellPrice);
                System.out.println("[buy]");
                tile.getTileData().putString("shop-type", "sell");
            }

            tile.getTileData().putBoolean("shop-activated", true);
            tile.getTileData().putUUID("owner", player.getUUID());
            //Serialize all items in the TE and store them in a ListNBT
            ListNBT lnbt = new ListNBT();
            inv.ifPresent((p) -> {
                for (int i = 0; i < p.getSlots(); i++) {
                    ItemStack inSlot = p.getStackInSlot(i);
                    if (inSlot.isEmpty()) continue;
                    if (inSlot.getItem() instanceof WritableBookItem)
                        lnbt.add(getItemFromBook(inSlot));
                    else
                        lnbt.add(inSlot.serializeNBT());
                }
            });
            tile.getTileData().put("items", lnbt);
            tile.save(nbt);
            tile.setChanged();
            storage.getTileData().putBoolean("is-shop", true);
            storage.getTileData().putUUID("owner", player.getUUID());
            storage.save(new CompoundNBT());
            BlockState state = world.getBlockState(pos);
            world.sendBlockUpdated(pos, state, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
        }
        catch(NumberFormatException e) {
            world.destroyBlock(pos, true, player);
        }
        player.closeContainer();
    }

    private static CompoundNBT getItemFromBook(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt.isEmpty()) return stack.serializeNBT();
        String page = nbt.getList("pages", Constants.NBT.TAG_STRING).get(0).getAsString();
        if (page.substring(0, 7).equalsIgnoreCase("vending")) {
            String subStr = page.substring(8);
            try {
                stack = ItemStack.of(JsonToNBT.parseTag(subStr));
                return stack.serializeNBT();
            }
            catch(CommandSyntaxException e) {e.printStackTrace();}

        }
        return stack.serializeNBT();
    }





    @Override
    protected void init() {
        super.init();
        if (minecraft != null){
            minecraft.keyboardHandler.setSendRepeatsToGui(true);
            shopIntroduce =new TextFieldRect(minecraft.font,  -50, getScreenY() -20 ,100, 16, "");
            children.add(shopIntroduce);
            shopValue =new TextFieldRect(minecraft.font, getScreenX() -50, getScreenY() +10 ,100, 16, "");
            children.add(shopValue);

            shopType =addButton(new ButtonRect(getScreenX() -30 , getScreenY() -50, 60, 16,"",(a)->setType()));

            createShopBtn =addButton(new ButtonRect(getScreenX()- 30, getScreenY() + 30, 60, 16,"",(btn)->create()));

        }
    }
    @Override
    protected void drawGuiBackground(MatrixStack matrixStack, int mouseX, int mouseY) {

        ScreenUtil.bindTexture(SimpleEco.MOD_ID,"gui_textures");
        ScreenUtil.drawRect(getScreenX(), getScreenY(), 0, 0, 0, getGuiSizeX(), getGuiSizeY());
        shopIntroduce.render(matrixStack, mouseX, mouseY, 0);
        shopValue.render(matrixStack, mouseX, mouseY, 0);
        drawCenteredString(matrixStack,this.font,new TranslationTextComponent("message.gui.title"),getScreenX(),getScreenY()- 60,0xFFFFFF);
        drawString(matrixStack,this.font,new TranslationTextComponent("message.gui.introduce"), getScreenX() -110 ,getScreenY()-20 ,0xFFFFFF);
        drawString(matrixStack,this.font,new TranslationTextComponent("message.gui.value"), getScreenX() -110 ,getScreenY()+10 ,0xFFFFFF);
        //ScreenUtil.drawCenteredString(matrixStack, "Shop", getScreenX(), getScreenY() - 60, 0, 0xFFFFFF);
        shopType.setMessage(type ? new TranslationTextComponent("message.gui.type.buy") : new TranslationTextComponent("message.gui.type.sell"));
        createShopBtn.setMessage(new TranslationTextComponent("message.gui.create"));
    }


    @Override
    protected void drawGuiForeground(MatrixStack matrixStack, int mouseX, int mouseY) {
    }
    @Override
    protected int getGuiSizeX() {
        return 0;
    }
    @Override
    protected int getGuiSizeY() {
        return 0;
    }
    @Override
    protected boolean canCloseWithInvKey() {
        return false;
    }
    @Override
    public boolean isPauseScreen () {
        return false;
    }
}
