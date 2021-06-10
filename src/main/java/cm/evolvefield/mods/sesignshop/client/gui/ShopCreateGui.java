package cm.evolvefield.mods.sesignshop.client.gui;

import cm.evolvefield.mods.sesignshop.client.gui.base.ButtonRect;
import cm.evolvefield.mods.sesignshop.client.gui.base.GuiScreenBase;
import cm.evolvefield.mods.sesignshop.client.gui.base.TextFieldRect;
import cm.evolvefield.mods.sesignshop.utils.ScreenUtil;
import cn.evolvefield.mods.simpleeco.core.SEConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ChestTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class ShopCreateGui extends GuiScreenBase {


    private ButtonRect createShopBtn;
    private ButtonRect shopType;
    private boolean type = false;

    private TextFieldRect shopIntroduce;
    private TextFieldRect shopValue;
    TileEntity storage;
    SignTileEntity tile;
    World world;
    BlockPos pos;
    CompoundNBT nbt;



    public ShopCreateGui(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt ,PlayerEntity player) {
        super(player, Hand.MAIN_HAND);
        this.storage = storage;
        this.tile = tile;
        this.world = world;
        this.pos = pos;
        this.nbt = nbt;
    }


    private void setType(boolean value){
        type = value;
    }

    private void create(){
        //check if the storage block has an item in the first slot
        LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        ItemStack srcStack = inv.map((c) -> c.getStackInSlot(0)).orElse(ItemStack.EMPTY);
        if (srcStack.equals(ItemStack.EMPTY, true)) return;
        try{
            double price = Math.abs(Double.valueOf(shopValue.getValue()));
            tile.getTileData().putDouble("price", price);

            if(type = true){
                StringTextComponent buyAction = new StringTextComponent("[buy]");
                buyAction.withStyle(TextFormatting.GREEN);
                tile.setMessage(0, buyAction);
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
            shopIntroduce =new TextFieldRect(minecraft.font, getScreenX() -80, getScreenY() -20 ,160, 16, "");
            children.add(shopIntroduce);
            shopValue =new TextFieldRect(minecraft.font, getScreenX() -80, getScreenY() +10 ,160, 16, "");
            children.add(shopValue);

            shopType =addButton(new ButtonRect(getScreenX() , getScreenY() -50, 28, 16,"",(btn)->setType(!type)));


            createShopBtn =addButton(new ButtonRect(getScreenX(), getScreenY() + 30, 56, 16,"",(btn)->create()));

        }
    }
    @Override
    protected void drawGuiBackground(MatrixStack matrixStack, int mouseX, int mouseY) {
        shopIntroduce.render(matrixStack, mouseX, mouseY, 0);
        shopValue.render(matrixStack, mouseX, mouseY, 0);
        ScreenUtil.drawCenteredString(matrixStack, new TranslationTextComponent("message.gui.title").toString(), getScreenX(), getScreenY() - 60, 0, 0xFFFFFF);
        shopType.setMessage(new StringTextComponent("Type: " + (type ? "Buy" : "Sell")));
    }



    @Override
    protected String getGuiTextureName() {
        return null;
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
