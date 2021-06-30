package cn.evolvefield.mods.sesignshop.core.events;

import cn.evolvefield.mods.sesignshop.client.gui.ShopCreateGui;
import cn.evolvefield.mods.sesignshop.core.SSRegistry;
import cn.evolvefield.mods.simpleeco.core.money.AccountManager;
import cn.evolvefield.mods.simpleeco.main.SEConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.*;
import net.minecraft.client.Minecraft;
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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static cn.evolvefield.mods.sesignshop.SESignShop.MOD_ID;


@Mod.EventBusSubscriber( modid=MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class SShopLoader {
    public static Map<UUID, Long> timeSinceClick = new HashMap<>();

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().getEntityWorld().isRemote && event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            String symbol = SEConfig.CURRENCY_SYMBOL.get();
            double balP = AccountManager.get(player.getServer().func_241755_D_()).getBalance( player.getUniqueID());
            player.sendMessage(new StringTextComponent(symbol+ balP), player.getUniqueID());
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isRemote()) return;
        boolean cancel = false;
        if (event.getWorld().getTileEntity(event.getPos().north()) != null
                &&event.getWorld().getTileEntity(event.getPos().north()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getTileEntity(event.getPos().south()) != null
                && event.getWorld().getTileEntity(event.getPos().south()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getTileEntity(event.getPos().east()) != null
                && event.getWorld().getTileEntity(event.getPos().east()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getTileEntity(event.getPos().west()) != null
                && event.getWorld().getTileEntity(event.getPos().west()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getTileEntity(event.getPos().up()) != null
                && event.getWorld().getTileEntity(event.getPos().up()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getTileEntity(event.getPos().down()) != null
                && event.getWorld().getTileEntity(event.getPos().down()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        event.setCanceled(cancel);
    }

    @SuppressWarnings("static-access")
    @SubscribeEvent
    public static void onShopBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isRemote() && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            SignTileEntity tile = (SignTileEntity) event.getWorld().getTileEntity(event.getPos());
            CompoundNBT nbt = tile.getTileData();
            if (!nbt.isEmpty() && nbt.contains("shop-activated")) {
                PlayerEntity player = event.getPlayer();
                boolean hasPerms = player.hasPermissionLevel(SEConfig.ADMIN_LEVEL.get());
                if (!nbt.getUniqueId("owner").equals(player.getUniqueID())) {
                    event.setCanceled(!hasPerms);
                }
                else if(nbt.getUniqueId("owner").equals(player.getUniqueID()) || hasPerms) {
                    BlockPos backBlock = BlockPos.fromLong(BlockPos.offset(event.getPos().toLong(), tile.getBlockState().get(((WallSignBlock)tile.getBlockState().getBlock()).FACING).getOpposite()));
                    event.getWorld().getTileEntity(backBlock).getTileData().remove("is-shop");
                }
            }
        }
        else if (!event.getWorld().isRemote() && event.getWorld().getTileEntity(event.getPos()) != null) {
            if (event.getWorld().getTileEntity(event.getPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                if (event.getWorld().getTileEntity(event.getPos()).getTileData().contains("is-shop")) {
                    PlayerEntity player = event.getPlayer();
                    event.setCanceled(!player.hasPermissionLevel(SEConfig.ADMIN_LEVEL.get()));
                }
				BlockPos shop = event.getPos();
				if (event.getWorld().getTileEntity(event.getPos().north()) != null
						&&event.getWorld().getTileEntity(event.getPos().north()).getBlockState().getBlock() instanceof WallSignBlock
						&& !event.getWorld().getTileEntity(event.getPos().north()).getTileData().isEmpty()
						&& event.getWorld().getTileEntity(event.getPos().north()).getTileData().getBoolean("shop-activated")) {
					shop = event.getPos().north();
				}
				else if (event.getWorld().getTileEntity(event.getPos().south()) != null
						&& event.getWorld().getTileEntity(event.getPos().south()).getBlockState().getBlock() instanceof WallSignBlock
						&& !event.getWorld().getTileEntity(event.getPos().south()).getTileData().isEmpty()
						&& event.getWorld().getTileEntity(event.getPos().south()).getTileData().getBoolean("shop-activated")) {
					shop = event.getPos().south();
				}
				else if (event.getWorld().getTileEntity(event.getPos().east()) != null
						&& event.getWorld().getTileEntity(event.getPos().east()).getBlockState().getBlock() instanceof WallSignBlock
						&& !event.getWorld().getTileEntity(event.getPos().east()).getTileData().isEmpty()
						&& event.getWorld().getTileEntity(event.getPos().east()).getTileData().getBoolean("shop-activated")) {
					shop = event.getPos().east();
				}
				else if (event.getWorld().getTileEntity(event.getPos().west()) != null
						&& event.getWorld().getTileEntity(event.getPos().west()).getBlockState().getBlock() instanceof WallSignBlock
						&& !event.getWorld().getTileEntity(event.getPos().west()).getTileData().isEmpty()
						&& event.getWorld().getTileEntity(event.getPos().west()).getTileData().getBoolean("shop-activated")) {
					shop = event.getPos().west();
				}
//				if (!shop.equals(event.getPos()) && !event.getWorld().getTileEntity(shop).getTileData().getUniqueID("owner").equals(player.getUniqueID())) {
//
//				}
            }
        }
    }

    @SubscribeEvent
    public static void onStorageOpen(PlayerInteractEvent.RightClickBlock event) {
        TileEntity invTile = event.getWorld().getTileEntity(event.getPos());
        if (invTile != null && invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            if (invTile.getTileData().contains("is-shop")) {
                if (!invTile.getTileData().getUniqueId("owner").equals(event.getPlayer().getUniqueID())) {
                    event.setCanceled(!event.getPlayer().hasPermissionLevel(SEConfig.ADMIN_LEVEL.get()));
                }
            }
        }
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onSignLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isRemote && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            SignTileEntity tile = (SignTileEntity) event.getWorld().getTileEntity(event.getPos());
            CompoundNBT nbt = tile.getTileData();
            if (nbt.contains("shop-activated"))
                getSaleInfo(nbt, event.getPlayer());
        }
    }

    @SubscribeEvent
    public static void onChestRightClick(PlayerInteractEvent.RightClickBlock event){
        if (!event.getWorld().isRemote &&
                event.getWorld().getBlockState(event.getPos()).getBlock() instanceof ChestBlock
                && event.getPlayer().getHeldItemMainhand().getItem() == SSRegistry.shopCreate.get()
                && event.getPlayer().isCrouching()
        )
        {
            PlayerEntity player = event.getPlayer();
            BlockState state = event.getWorld().getBlockState(event.getPos());
            BlockPos backBlock = BlockPos.fromLong(BlockPos.offset(event.getPos().toLong(), Direction.NORTH));
            BlockState signState = event.getWorld().getBlockState(backBlock);
            TileEntity chestTile = event.getWorld().getTileEntity(event.getPos());
            AccountManager wsd = AccountManager.get(player.getServer().func_241755_D_());
            double bal = wsd.getBalance(player.getUniqueID());

            if(signState == Blocks.AIR.getDefaultState() && chestTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()){
                event.getWorld().setBlockState(backBlock, Blocks.OAK_WALL_SIGN.getDefaultState(), 2);
                SignTileEntity signTile = (SignTileEntity) event.getWorld().getTileEntity(backBlock);
                CompoundNBT nbt = signTile.serializeNBT();
                Minecraft.getInstance().displayGuiScreen(new ShopCreateGui(chestTile,signTile,event.getWorld(),backBlock,nbt,event.getPlayer()));
                wsd.setBalance(player.getUniqueID(), bal - 10);
                player.sendMessage(new TranslationTextComponent("message.create.money", SEConfig.CURRENCY_SYMBOL.get()+10), player.getUniqueID());
            }


        }

    }

    @SuppressWarnings({ "resource", "static-access" })
    @SubscribeEvent
    public static void onSignRightClick(PlayerInteractEvent.RightClickBlock event) {

        if (!event.getWorld().isRemote && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            BlockState state = event.getWorld().getBlockState(event.getPos());
            WallSignBlock sign = (WallSignBlock) state.getBlock();
            BlockPos backBlock = BlockPos.fromLong(BlockPos.offset(event.getPos().toLong(), state.get(sign.FACING).getOpposite()));
            if (event.getWorld().getTileEntity(backBlock) != null ) {
                TileEntity invTile = event.getWorld().getTileEntity(backBlock);
                if (invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                    SignTileEntity tile = (SignTileEntity) event.getWorld().getTileEntity(event.getPos());
                    CompoundNBT nbt = tile.serializeNBT();
                    if (!nbt.contains("ForgeData") || !nbt.getCompound("ForgeData").contains("shop-activated") ) {
                        if(event.getPlayer().getHeldItemMainhand().getItem() == SSRegistry.shopCreate.get()) {//new ShopCreateGui(invTile,tile,event.getWorld(),event.getPos(),nbt,event.getPlayer());
                            activateShop(invTile, tile, event.getWorld(), event.getPos(), nbt, event.getPlayer());
                        }
                    }
                    else processTransaction(invTile, tile, event.getPlayer());
                }
            }
        }
    }

    private static void activateShop(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt, PlayerEntity player) {
        ITextComponent actionEntry = ITextComponent.Serializer.getComponentFromJson(nbt.getString("Text1"));
        ITextComponent priceEntry  = ITextComponent.Serializer.getComponentFromJson(nbt.getString("Text4"));
        //check if the storage block has an item in the first slot
        LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        ItemStack srcStack = inv.map((c) -> c.getStackInSlot(0)).orElse(ItemStack.EMPTY);
        if (srcStack.equals(ItemStack.EMPTY, true)) return;
        //first confirm the action type is valid
        if (actionEntry.getString().equalsIgnoreCase("[buy]")
                || actionEntry.getString().equalsIgnoreCase("[sell]")
                || actionEntry.getString().equalsIgnoreCase("[s-buy]")
                || actionEntry.getString().equalsIgnoreCase("[s-sell]")) {
            //second confirm the price value is valid
            if (actionEntry.getString().equalsIgnoreCase("[s-buy]") || actionEntry.getString().equalsIgnoreCase("[s-sell]")) {
                if (!player.hasPermissionLevel(SEConfig.ADMIN_LEVEL.get())) {
                    player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUniqueID());
                    return;
                }
            }
            try {
                double price = Math.abs(Double.valueOf(priceEntry.getString()));
                tile.getTileData().putDouble("price", price);
                StringTextComponent newAction = new StringTextComponent(actionEntry.getString());
                newAction.mergeStyle(TextFormatting.GREEN);
                tile.setText(0, newAction);
                StringTextComponent newPrice = new StringTextComponent(SEConfig.CURRENCY_SYMBOL.get()+String.valueOf(price));
                newPrice.mergeStyle(TextFormatting.GOLD);
                tile.setText(3, newPrice);
                System.out.println(actionEntry.getString());
                switch (actionEntry.getString()) {
                    case "[buy]": {tile.getTileData().putString("shop-type", "buy"); break;}
                    case "[sell]": {tile.getTileData().putString("shop-type", "sell");break;}
                    case "[s-buy]": {tile.getTileData().putString("shop-type", "server-buy");break;}
                    case "[s-sell]": {tile.getTileData().putString("shop-type", "server-sell");break;}
                    default:}
                tile.getTileData().putBoolean("shop-activated", true);
                tile.getTileData().putUniqueId("owner", player.getUniqueID());
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
                tile.write(nbt);
                tile.markDirty();
                storage.getTileData().putBoolean("is-shop", true);
                storage.getTileData().putUniqueId("owner", player.getUniqueID());
                storage.write(new CompoundNBT());
                BlockState state = world.getBlockState(pos);
                world.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.DEFAULT_AND_RERENDER);
            }
            catch(NumberFormatException e) {
                world.destroyBlock(pos, true, player);
            }
        }
    }

    private static CompoundNBT getItemFromBook(ItemStack stack) {
        CompoundNBT nbt = stack.getTag();
        if (nbt.isEmpty()) return stack.serializeNBT();
        String page = nbt.getList("pages", Constants.NBT.TAG_STRING).get(0).getString();
        if (page.substring(0, 7).equalsIgnoreCase("vending")) {
            String subStr = page.substring(8);
            try {
                stack = ItemStack.read(JsonToNBT.getTagFromJson(subStr));
                return stack.serializeNBT();
            }
            catch(CommandSyntaxException e) {e.printStackTrace();}

        }
        return stack.serializeNBT();
    }

    private static void getSaleInfo(CompoundNBT nbt, PlayerEntity player) {
        if (System.currentTimeMillis() - timeSinceClick.getOrDefault(player.getUniqueID(), 0l) < 1500) return;
        String type = nbt.getString("shop-type");
        boolean isBuy = type.equalsIgnoreCase("buy") || type.equalsIgnoreCase("server-buy");
        List<ItemStack> transItems = new ArrayList<>();
        ListNBT itemsList = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            transItems.add(ItemStack.read(itemsList.getCompound(i)));
        }
        double value = nbt.getDouble("price");
        StringTextComponent itemComponent = getTransItemsDisplayString(transItems);
        if (isBuy)
            player.sendMessage(new TranslationTextComponent("message.shop.info.buy", itemComponent, SEConfig.CURRENCY_SYMBOL.get()+ value), player.getUniqueID());
        else
            player.sendMessage(new TranslationTextComponent("message.shop.info.sell", itemComponent,SEConfig.CURRENCY_SYMBOL.get()+ value ), player.getUniqueID());
        timeSinceClick.put(player.getUniqueID(), System.currentTimeMillis());
    }

    private static StringTextComponent getTransItemsDisplayString(List<ItemStack> list ) {
        List<ItemStack> items = new ArrayList<>();
        for (int l = 0; l < list.size(); l++) {
            boolean hadMatch = false;
            for (int i = 0; i < items.size(); i++) {
                if (list.get(l).isItemEqual(items.get(i)) && ItemStack.areItemStackTagsEqual(list.get(l), items.get(i))) {
                    items.get(i).grow(list.get(l).getCount());
                    hadMatch = true;
                    break;
                }
            }
            if (!hadMatch) items.add(list.get(l));
        }
        StringTextComponent itemComponent = new StringTextComponent("");
        boolean isFirst = true;
        for (ItemStack item : items) {
            if (!isFirst) itemComponent.append( new StringTextComponent(", ") );
            itemComponent.append( new StringTextComponent(item.getCount() +"x ")  );
            itemComponent.append(item.getDisplayName());
            isFirst = false;
        }
        return itemComponent;
    }

    private static void processTransaction(TileEntity tile, SignTileEntity sign, PlayerEntity player) {
        AccountManager wsd = AccountManager.get(player.getServer().func_241755_D_());
        CompoundNBT nbt = sign.getTileData();
        LazyOptional<IItemHandler> inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        List<ItemStack> transItems = new ArrayList<>();
        ListNBT itemsList = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            transItems.add(ItemStack.read(itemsList.getCompound(i)));
        }
        //ItemStack transItem = ItemStack.of(nbt.getCompound("item"));
        String action = nbt.getString("shop-type");
        double value = nbt.getDouble("price");
        //================BUY=================================================================================
        if (action.equalsIgnoreCase("buy")) { //BUY
            //First check the available funds and stock for trade
            UUID shopOwner = nbt.getUniqueId("owner");
            if(shopOwner == player.getUniqueID()){
                player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.same"), player.getUniqueID());
            }
            else
            {
                double balP = wsd.getBalance(player.getUniqueID());
                if (value > balP) {
                    player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUniqueID());
                    return;
                }
                Map<Integer, ItemStack> slotMap = new HashMap<>();
                for (int tf = 0; tf < transItems.size(); tf++) {
                    int[] stackSize = {transItems.get(tf).getCount()};
                    final int t = tf;
                    Optional<Boolean> test = inv.map((p) -> {
                        for (int i = 0; i < p.getSlots(); i++) {
                            ItemStack inSlot = ItemStack.EMPTY;
                            if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.areItemStackTagsEqual(transItems.get(t), slotMap.get(i))) {
                                inSlot = p.extractItem(i, stackSize[0] + slotMap.get(i).getCount(), true);
                                inSlot.shrink(slotMap.get(i).getCount());
                            } else inSlot = p.extractItem(i, stackSize[0], true);
                            if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.areItemStackTagsEqual(inSlot, transItems.get(t))) {
                                slotMap.merge(i, inSlot, (s, o) -> {
                                    s.grow(o.getCount());
                                    return s;
                                });
                                stackSize[0] -= inSlot.getCount();
                            }
                            if (stackSize[0] <= 0) break;
                        }
                        return stackSize[0] <= 0;
                    });
                    if (!test.get()) {
                        player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.stock"), player.getUniqueID());
                        return;
                    }
                }
                //Test if container has inventory to process.
                //If so, process transfer of items and funds.

                wsd.transferBalance(player.getUniqueID(), shopOwner, value);
                inv.ifPresent((p) -> {
                    for (Map.Entry<Integer, ItemStack> map : slotMap.entrySet()) {
                        player.inventory.addItemStackToInventory(p.extractItem(map.getKey(), map.getValue().getCount(), false));
                    }
                });
                TranslationTextComponent msg = new TranslationTextComponent("message.shop.buy.success"
                        , SEConfig.CURRENCY_SYMBOL.get() + value, getTransItemsDisplayString(transItems));
                player.sendMessage(msg, player.getUniqueID());
                player.getServer().sendMessage(msg, player.getUniqueID());
            }
            return;
        }
        //================SELL=================================================================================
        else if (action.equalsIgnoreCase("sell")) { //SELL
            //First check the available funds and stock for trade
            UUID shopOwner = nbt.getUniqueId("owner");
            if(shopOwner == player.getUniqueID()){
                player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.same"), player.getUniqueID());
            }
            else
            {
                double balP = wsd.getBalance(shopOwner);
                if (value > balP) {
                    player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.funds"), player.getUniqueID());
                    return;
                }
                //test if player has item in inventory to sell
                //next test that the inventory has space
                Map<Integer, ItemStack> slotMap = new HashMap<>();
                for (int t = 0; t < transItems.size(); t++) {
                    int stackSize = transItems.get(t).getCount();
                    for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                        ItemStack inSlot = player.inventory.getStackInSlot(i).copy();
                        int count = Math.min(stackSize, inSlot.getCount());
                        inSlot.setCount(count);
                        if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.areItemStackTagsEqual(transItems.get(t), slotMap.get(i))) {
                            count = Math.min(stackSize + slotMap.get(i).getCount(), inSlot.getCount());
                            inSlot.setCount(count);
                        }
                        if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.areItemStackTagsEqual(inSlot, transItems.get(t))) {
                            slotMap.merge(i, inSlot, (s, o) -> {
                                s.grow(o.getCount());
                                return s;
                            });
                            stackSize -= inSlot.getCount();
                        }
                        if (stackSize <= 0) break;
                    }
                    if (stackSize > 0) {
                        player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUniqueID());
                        return;
                    }

                }
                Map<Integer, ItemStack> invSlotMap = new HashMap<>();
                for (int t = 0; t < transItems.size(); t++) {
                    ItemStack sim = transItems.get(t).copy();
                    Optional<Boolean> test = inv.map((p) -> {
                        for (int i = 0; i < p.getSlots(); i++) {
                            ItemStack insertResult = p.insertItem(i, sim, true);
                            if (insertResult.isEmpty()) {
                                invSlotMap.merge(i, sim.copy(), (s, o) -> {
                                    s.grow(o.getCount());
                                    return s;
                                });
                                sim.setCount(0);
                                break;
                            } else if (insertResult.getCount() == sim.getCount()) {
                                continue;
                            } else {
                                ItemStack insertSuccess = sim.copy();
                                insertSuccess.shrink(insertResult.getCount());
                                sim.setCount(insertResult.getCount());
                                invSlotMap.merge(i, insertSuccess, (s, o) -> {
                                    s.grow(insertSuccess.getCount());
                                    return s;
                                });
                            }
                        }
                        if (!sim.isEmpty()) {
                            player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.space"), player.getUniqueID());
                            return false;
                        }
                        return true;
                    });
                    if (!test.get()) return;
                }
                //Process Transfers now that reqs have been met
                wsd.transferBalance(shopOwner, player.getUniqueID(), value);
                for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
                    player.inventory.decrStackSize(pSlots.getKey(), pSlots.getValue().getCount());
                }
                inv.ifPresent((p) -> {
                    for (Map.Entry<Integer, ItemStack> map : invSlotMap.entrySet()) {
                        p.insertItem(map.getKey(), map.getValue(), false);
                    }
                });
                player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
                        , getTransItemsDisplayString(transItems), SEConfig.CURRENCY_SYMBOL.get() + value
                ), player.getUniqueID());
                return;
            }
        }
        //================SERVER BUY=================================================================================
        else if (action.equalsIgnoreCase("server-buy")) { //SERVER BUY
            //First check the available funds and stock for trade
            double balP = wsd.getBalance( player.getUniqueID());
            if (value > balP) {
                player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUniqueID());
                return;
            }
            wsd.changeBalance( player.getUniqueID(), -value);
            for (int i = 0; i < transItems.size(); i++) {
                player.inventory.addItemStackToInventory(transItems.get(i).copy());
            }
            player.sendMessage(new TranslationTextComponent("message.shop.buy.success"
                    , SEConfig.CURRENCY_SYMBOL.get()+String.valueOf(value),getTransItemsDisplayString(transItems)
            ), player.getUniqueID());
            return;
        }
        //================SERVER SELL=================================================================================
        else if (action.equalsIgnoreCase("server-sell")) { //SERVER SELL
            Map<Integer, ItemStack> slotMap = new HashMap<>();
            for (int t = 0; t < transItems.size(); t++) {
                int stackSize = transItems.get(t).getCount();
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack inSlot = player.inventory.getStackInSlot(i).copy();
                    int count = stackSize > inSlot.getCount() ? inSlot.getCount() : stackSize;
                    inSlot.setCount(count);
                    if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.areItemStackTagsEqual(transItems.get(t), slotMap.get(i))) {
                        count = stackSize+slotMap.get(i).getCount() > inSlot.getCount() ? inSlot.getCount() : stackSize+slotMap.get(i).getCount();
                        inSlot.setCount(count);
                    }
                    if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.areItemStackTagsEqual(inSlot, transItems.get(t))) {
                        slotMap.merge(i, inSlot, (s, o) -> {s.grow(o.getCount()); return s;});
                        stackSize -= inSlot.getCount();
                    }
                    if (stackSize <= 0) break;
                }
                if (stackSize > 0) {
                    player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUniqueID());
                    return;
                }

            }
            wsd.changeBalance( player.getUniqueID(), value);
            for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
                player.inventory.getStackInSlot(pSlots.getKey()).shrink(pSlots.getValue().getCount());
            }
            player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
                    , getTransItemsDisplayString(transItems), SEConfig.CURRENCY_SYMBOL.get()+ value
            ), player.getUniqueID());
            return;
        }
    }
}
