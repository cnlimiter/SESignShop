package cn.evolvefield.mods.sesignshop.init.events;

import cn.evolvefield.mods.simpleeco.api.money.AccountManager;
import cn.evolvefield.mods.simpleeco.init.SEConfig;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
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
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import java.util.*;

import static cn.evolvefield.mods.sesignshop.SESignShop.MOD_ID;


@Mod.EventBusSubscriber(modid=MOD_ID, bus=Mod.EventBusSubscriber.Bus.FORGE)
public class ShopEvent {
    public static Map<UUID, Long> timeSinceClick = new HashMap<>();

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getPlayer().level.isClientSide && event.getPlayer() instanceof ServerPlayerEntity) {
            ServerPlayerEntity player = (ServerPlayerEntity) event.getEntity();
            String symbol = SEConfig.CURRENCY_SYMBOL.get();
            double balP = AccountManager.get(player.getServer().overworld()).getBalance( player.getUUID());
            player.sendMessage(new StringTextComponent(symbol+ balP), player.getUUID());
        }
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!event.getEntityLiving().getCommandSenderWorld().isClientSide && event.getEntityLiving() instanceof PlayerEntity) {
            PlayerEntity player = (PlayerEntity) event.getEntityLiving();
            double balp = AccountManager.get(player.getServer().overworld()).getBalance( player.getUUID());
            double loss = balp * SEConfig.LOSS_ON_DEATH.get();
            if (loss > 0) {
                AccountManager.get(player.getServer().overworld()).changeBalance(player.getUUID(), -loss);
                player.sendMessage(new TranslationTextComponent("message.death", loss), player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getWorld().isClientSide()) return;
        boolean cancel = false;
        if (event.getWorld().getBlockEntity(event.getPos().north()) != null
                &&event.getWorld().getBlockEntity(event.getPos().north()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getBlockEntity(event.getPos().south()) != null
                && event.getWorld().getBlockEntity(event.getPos().south()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getBlockEntity(event.getPos().east()) != null
                && event.getWorld().getBlockEntity(event.getPos().east()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getBlockEntity(event.getPos().west()) != null
                && event.getWorld().getBlockEntity(event.getPos().west()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getBlockEntity(event.getPos().above()) != null
                && event.getWorld().getBlockEntity(event.getPos().above()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        if (event.getWorld().getBlockEntity(event.getPos().below()) != null
                && event.getWorld().getBlockEntity(event.getPos().below()).getTileData().contains("is-shop")) {
            cancel = true;
        }
        event.setCanceled(cancel);
    }

    @SuppressWarnings("static-access")
    @SubscribeEvent
    public static void onShopBreak(BlockEvent.BreakEvent event) {
        if (!event.getWorld().isClientSide() && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
            CompoundNBT nbt = tile.getTileData();
            if (!nbt.isEmpty() && nbt.contains("shop-activated")) {
                PlayerEntity player = event.getPlayer();
                boolean hasPerms = player.hasPermissions(SEConfig.ADMIN_LEVEL.get());
                if (!nbt.getUUID("owner").equals(player.getUUID())) {
                    event.setCanceled(!hasPerms);
                }
                else if(nbt.getUUID("owner").equals(player.getUUID()) || hasPerms) {
                    BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), tile.getBlockState().getValue(((WallSignBlock)tile.getBlockState().getBlock()).FACING).getOpposite()));
                    event.getWorld().getBlockEntity(backBlock).getTileData().remove("is-shop");
                }
            }
        }
        else if (!event.getWorld().isClientSide() && event.getWorld().getBlockEntity(event.getPos()) != null) {
            if (event.getWorld().getBlockEntity(event.getPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                if (event.getWorld().getBlockEntity(event.getPos()).getTileData().contains("is-shop")) {
                    PlayerEntity player = event.getPlayer();
                    event.setCanceled(!player.hasPermissions(SEConfig.ADMIN_LEVEL.get()));
                }
            }
        }
    }

    @SubscribeEvent
    public static void onStorageOpen(PlayerInteractEvent.RightClickBlock event) {
        TileEntity invTile = event.getWorld().getBlockEntity(event.getPos());
        if (invTile != null && invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
            if (invTile.getTileData().contains("is-shop")) {
                if (!invTile.getTileData().getUUID("owner").equals(event.getPlayer().getUUID())) {
                    event.setCanceled(!event.getPlayer().hasPermissions(SEConfig.ADMIN_LEVEL.get()));
                }
            }
        }
    }

    @SuppressWarnings("resource")
    @SubscribeEvent
    public static void onSignLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isClientSide && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
            CompoundNBT nbt = tile.getTileData();
            if (nbt.contains("shop-activated"))
                getSaleInfo(nbt, event.getPlayer());
        }
    }




    @SuppressWarnings({ "resource", "static-access" })
    @SubscribeEvent
    public static void onSignRightClick(PlayerInteractEvent.RightClickBlock event) {

        if (!event.getWorld().isClientSide && event.getWorld().getBlockState(event.getPos()).getBlock() instanceof WallSignBlock) {
            BlockState state = event.getWorld().getBlockState(event.getPos());
            WallSignBlock sign = (WallSignBlock) state.getBlock();
            BlockPos backBlock = BlockPos.of(BlockPos.offset(event.getPos().asLong(), state.getValue(sign.FACING).getOpposite()));
            PlayerEntity player = event.getPlayer();

            if (event.getWorld().getBlockEntity(backBlock) != null ) {
                TileEntity invTile = event.getWorld().getBlockEntity(backBlock);
                if (invTile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).isPresent()) {
                    SignTileEntity tile = (SignTileEntity) event.getWorld().getBlockEntity(event.getPos());
                    CompoundNBT nbt = tile.serializeNBT();
                    if (!nbt.contains("ForgeData") || !nbt.getCompound("ForgeData").contains("shop-activated")
                    ) {
                            activateShop(invTile, tile, event.getWorld(), event.getPos(), nbt, event.getPlayer());
                    }
                    else if (!event.getPlayer().isCrouching())
                        processTransaction(invTile, tile, event.getPlayer());
                }
            }
        }
    }

    private static void activateShop(TileEntity storage, SignTileEntity tile, World world, BlockPos pos, CompoundNBT nbt, PlayerEntity player) {
        ITextComponent actionEntry = ITextComponent.Serializer.fromJson(nbt.getString("Text1"));
        ITextComponent priceEntry  = ITextComponent.Serializer.fromJson(nbt.getString("Text4"));
        //check if the storage block has an item in the first slot
        AccountManager wsd = AccountManager.get(player.getServer().overworld());
        double bal = wsd.getBalance(player.getUUID());
        LazyOptional<IItemHandler> inv = storage.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, Direction.UP);
        ItemStack srcStack = inv.map((c) -> {
            for (int i = 0; i < c.getSlots(); i++) {
                if (c.getStackInSlot(i).isEmpty()) continue;
                return c.getStackInSlot(i);
            }
            return ItemStack.EMPTY;
        }).orElse(ItemStack.EMPTY);
        if (srcStack.equals(ItemStack.EMPTY, true)) return;
        //first confirm the action type is valid
        if (actionEntry.getString().equalsIgnoreCase("[buy]")
                || actionEntry.getString().equalsIgnoreCase("[sell]")
                || actionEntry.getString().equalsIgnoreCase("[s-buy]")
                || actionEntry.getString().equalsIgnoreCase("[s-sell]")) {
            //second confirm the price value is valid
            if (actionEntry.getString().equalsIgnoreCase("[s-buy]") || actionEntry.getString().equalsIgnoreCase("[s-sell]")) {
                if (!player.hasPermissions(SEConfig.ADMIN_LEVEL.get())) {
                    player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUUID());
                    return;
                }
            }
            else if (!player.hasPermissions(SEConfig.SHOP_LEVEL.get())) {
                player.sendMessage(new TranslationTextComponent("message.activate.failure.admin"), player.getUUID());
                return;
            }
            try {
                double price = Math.abs(Double.valueOf(priceEntry.getString()));
                tile.getTileData().putDouble("price", price);
                StringTextComponent newAction = new StringTextComponent(actionEntry.getString());
                newAction.withStyle(TextFormatting.GREEN);
                tile.setMessage(0, newAction);
                StringTextComponent newPrice = new StringTextComponent(SEConfig.CURRENCY_SYMBOL.get()+ price);
                newPrice.withStyle(TextFormatting.GOLD);
                tile.setMessage(3, newPrice);
                System.out.println(actionEntry.getString());
                switch (actionEntry.getString()) {
                    case "[buy]": {tile.getTileData().putString("shop-type", "buy"); break;}
                    case "[sell]": {tile.getTileData().putString("shop-type", "sell");break;}
                    case "[s-buy]": {tile.getTileData().putString("shop-type", "server-buy");break;}
                    case "[s-sell]": {tile.getTileData().putString("shop-type", "server-sell");break;}
                    default:}
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

                wsd.setBalance(player.getUUID(),bal-SEConfig.SHOP_COST.get());
                player.sendMessage(new TranslationTextComponent("message.create.money" , SEConfig.CURRENCY_SYMBOL.get()+SEConfig.SHOP_COST.get()) .withStyle(TextFormatting.DARK_GRAY), player.getUUID());

            }
            catch(NumberFormatException e) {
                player.sendMessage(new TranslationTextComponent("message.activate.failure.common"), player.getUUID());
                world.destroyBlock(pos, true, player);
            }
        }
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

    private static void getSaleInfo(CompoundNBT nbt, PlayerEntity player) {
        if (System.currentTimeMillis() - timeSinceClick.getOrDefault(player.getUUID(), 0L) < 1500) return;
        String type = nbt.getString("shop-type");
        boolean isBuy = type.equalsIgnoreCase("buy") || type.equalsIgnoreCase("server-buy");
        List<ItemStack> transItems = new ArrayList<>();
        ListNBT itemsList = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            transItems.add(ItemStack.of(itemsList.getCompound(i)));
        }
        double value = nbt.getDouble("price");
        StringTextComponent itemComponent = getTransItemsDisplayString(transItems);
        if (isBuy)
            player.sendMessage(new TranslationTextComponent("message.shop.info.buy", itemComponent.withStyle(TextFormatting.BLUE), SEConfig.CURRENCY_SYMBOL.get()+ value), player.getUUID());
        else
            player.sendMessage(new TranslationTextComponent("message.shop.info.sell", itemComponent.withStyle(TextFormatting.RED),SEConfig.CURRENCY_SYMBOL.get()+ value ), player.getUUID());
        timeSinceClick.put(player.getUUID(), System.currentTimeMillis());
    }

    private static StringTextComponent getTransItemsDisplayString(List<ItemStack> list ) {
        List<ItemStack> items = new ArrayList<>();
        for (int l = 0; l < list.size(); l++) {
            boolean hadMatch = false;
            for (int i = 0; i < items.size(); i++) {
                if (list.get(l).equals(items.get(i)) && ItemStack.tagMatches(list.get(l), items.get(i))) {
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
        AccountManager wsd = AccountManager.get(player.getServer().overworld());
        CompoundNBT nbt = sign.getTileData();
        LazyOptional<IItemHandler> inv = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY);
        List<ItemStack> transItems = new ArrayList<>();
        ListNBT itemsList = nbt.getList("items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0; i < itemsList.size(); i++) {
            transItems.add(ItemStack.of(itemsList.getCompound(i)));
        }
        String action = nbt.getString("shop-type");
        double value = nbt.getDouble("price");
        //================BUY=================================================================================
        if (action.equalsIgnoreCase("buy")) { //BUY
            //First check the available funds and stock for trade
            UUID shopOwner = nbt.getUUID("owner");
            if(shopOwner == player.getUUID()){
                player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.same"), player.getUUID());
            }
            else
            {
                double balP = wsd.getBalance(player.getUUID());
                if (value > balP) {
                    player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
                    return;
                }
                Map<Integer, ItemStack> slotMap = new HashMap<>();
                for (int tf = 0; tf < transItems.size(); tf++) {
                    int[] stackSize = {transItems.get(tf).getCount()};
                    final int t = tf;
                    Optional<Boolean> test = inv.map((p) -> {
                        for (int i = 0; i < p.getSlots(); i++) {
                            ItemStack inSlot;
                            if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
                                inSlot = p.extractItem(i, stackSize[0] + slotMap.get(i).getCount(), true);
                                inSlot.shrink(slotMap.get(i).getCount());
                            } else inSlot = p.extractItem(i, stackSize[0], true);
                            if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
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
                        player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.stock"), player.getUUID());
                        return;
                    }
                }
                //Test if container has inventory to process.
                //If so, process transfer of items and funds.

                wsd.transferBalance(player.getUUID(), shopOwner, value);
                inv.ifPresent((p) -> {
                    for (Map.Entry<Integer, ItemStack> map : slotMap.entrySet()) {
                        player.inventory.add(p.extractItem(map.getKey(), map.getValue().getCount(), false));
                    }
                });
                TranslationTextComponent msg = new TranslationTextComponent("message.shop.buy.success"
                        , SEConfig.CURRENCY_SYMBOL.get() + value, getTransItemsDisplayString(transItems).withStyle(TextFormatting.BLUE));
                player.sendMessage(msg, player.getUUID());
                player.getServer().sendMessage(msg, player.getUUID());
            }
            return;
        }
        //================SELL=================================================================================
        else if (action.equalsIgnoreCase("sell")) { //SELL
            //First check the available funds and stock for trade
            UUID shopOwner = nbt.getUUID("owner");
            if(shopOwner == player.getUUID()){
                player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.same"), player.getUUID());
            }
            else
            {
                double balP = wsd.getBalance(shopOwner);
                if (value > balP) {
                    player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.funds"), player.getUUID());
                    return;
                }
                //test if player has item in inventory to sell
                //next test that the inventory has space
                Map<Integer, ItemStack> slotMap = new HashMap<>();
                for (int t = 0; t < transItems.size(); t++) {
                    int stackSize = transItems.get(t).getCount();
                    for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                        ItemStack inSlot = player.inventory.getItem(i).copy();
                        int count = Math.min(stackSize, inSlot.getCount());
                        inSlot.setCount(count);
                        if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
                            count = Math.min(stackSize + slotMap.get(i).getCount(), inSlot.getCount());
                            inSlot.setCount(count);
                        }
                        if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
                            slotMap.merge(i, inSlot, (s, o) -> {
                                s.grow(o.getCount());
                                return s;
                            });
                            stackSize -= inSlot.getCount();
                        }
                        if (stackSize <= 0) break;
                    }
                    if (stackSize > 0) {
                        player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
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
                            player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.space"), player.getUUID());
                            return false;
                        }
                        return true;
                    });
                    if (!test.get()) return;
                }
                //Process Transfers now that reqs have been met
                wsd.transferBalance(shopOwner, player.getUUID(), value);
                for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
                    player.inventory.removeItem(pSlots.getKey(), pSlots.getValue().getCount());
                }
                inv.ifPresent((p) -> {
                    for (Map.Entry<Integer, ItemStack> map : invSlotMap.entrySet()) {
                        p.insertItem(map.getKey(), map.getValue(), false);
                    }
                });
                player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
                        , getTransItemsDisplayString(transItems).withStyle(TextFormatting.RED), SEConfig.CURRENCY_SYMBOL.get() + value
                ), player.getUUID());
                return;
            }
        }
        //================SERVER BUY=================================================================================
        else if (action.equalsIgnoreCase("server-buy")) { //SERVER BUY
            //First check the available funds and stock for trade
            double balP = wsd.getBalance( player.getUUID());
            if (value > balP) {
                player.sendMessage(new TranslationTextComponent("message.shop.buy.failure.funds"), player.getUUID());
                return;
            }
            wsd.changeBalance( player.getUUID(), -value);
            for (int i = 0; i < transItems.size(); i++) {
                player.inventory.add(transItems.get(i).copy());
            }
            player.sendMessage(new TranslationTextComponent("message.shop.buy.success"
                    , SEConfig.CURRENCY_SYMBOL.get()+ value,getTransItemsDisplayString(transItems).withStyle(TextFormatting.BLUE)
            ), player.getUUID());
            return;
        }
        //================SERVER SELL=================================================================================
        else if (action.equalsIgnoreCase("server-sell")) { //SERVER SELL
            Map<Integer, ItemStack> slotMap = new HashMap<>();
            for (int t = 0; t < transItems.size(); t++) {
                int stackSize = transItems.get(t).getCount();
                for (int i = 0; i < player.inventory.getContainerSize(); i++) {
                    ItemStack inSlot = player.inventory.getItem(i).copy();
                    int count = stackSize > inSlot.getCount() ? inSlot.getCount() : stackSize;
                    inSlot.setCount(count);
                    if (slotMap.containsKey(i) && transItems.get(t).getItem().equals(slotMap.get(i).getItem()) && ItemStack.tagMatches(transItems.get(t), slotMap.get(i))) {
                        count = stackSize+slotMap.get(i).getCount() > inSlot.getCount() ? inSlot.getCount() : stackSize+slotMap.get(i).getCount();
                        inSlot.setCount(count);
                    }
                    if (inSlot.getItem().equals(transItems.get(t).getItem()) && ItemStack.tagMatches(inSlot, transItems.get(t))) {
                        slotMap.merge(i, inSlot, (s, o) -> {s.grow(o.getCount()); return s;});
                        stackSize -= inSlot.getCount();
                    }
                    if (stackSize <= 0) break;
                }
                if (stackSize > 0) {
                    player.sendMessage(new TranslationTextComponent("message.shop.sell.failure.stock"), player.getUUID());
                    return;
                }

            }
            wsd.changeBalance( player.getUUID(), value);
            for (Map.Entry<Integer, ItemStack> pSlots : slotMap.entrySet()) {
                player.inventory.getItem(pSlots.getKey()).shrink(pSlots.getValue().getCount());
            }
            player.sendMessage(new TranslationTextComponent("message.shop.sell.success"
                    , getTransItemsDisplayString(transItems).withStyle(TextFormatting.RED), SEConfig.CURRENCY_SYMBOL.get()+ value
            ), player.getUUID());
        }
    }
}
