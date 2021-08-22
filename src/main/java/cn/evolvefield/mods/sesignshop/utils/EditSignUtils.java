package cn.evolvefield.mods.sesignshop.utils;

import cn.evolvefield.mods.simpleeco.init.SEConfig;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tags.ITag;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static cn.evolvefield.mods.simpleeco.init.SEConfig.REQUIRED_ITEM;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.empty;
import static net.minecraftforge.registries.ForgeRegistries.ITEMS;

public class EditSignUtils {

    public void setRequiredItemId(String s){
        REQUIRED_ITEM.set(s);
    }

    public String getRequiredItemStr(){
        return REQUIRED_ITEM.get();
    }

    public static Collection<Item> getRequiredItem(){
        return getAsItems(REQUIRED_ITEM.get());
    }

    public static boolean canPlayerEdit(PlayerEntity playerEntity, ItemStack itemStack){
        return playerEntity.mayBuild()  && !isHoldingDye(itemStack)
                && hasRightItem(itemStack)
                ;
    }

    private static boolean isHoldingDye(ItemStack itemStack){
        return itemStack.getItem() instanceof DyeItem;
    }

    private static boolean hasRightItem(ItemStack itemStack){
        Collection<Item> requiredItem = getRequiredItem();
        if(requiredItem.isEmpty()){
            return true;
        }

        Item playerItem = itemStack.getItem();
        return requiredItem.stream().anyMatch(item -> item.equals(playerItem));
    }

    public static Set<Item> getAsItems(String name){
        return Stream.of(name)
                .filter(Objects::nonNull)
                .filter(val -> !val.isEmpty())
                .flatMap(EditSignUtils::getItem)
                .filter(Objects::nonNull)
                .collect(toSet());
    }

    @Nonnull
    public static Stream<Item> getItem(String name){
        try{
            boolean isTag = name.startsWith("#");
            if(isTag){
                name = name.substring(1);
            }
            ResourceLocation resourceLocation = new ResourceLocation(name);
            if(isTag){
                return Optional.ofNullable(ItemTags.getAllTags().getTag(resourceLocation))
                        .map(ITag::getValues)
                        .map(Collection::stream)
                        .orElse(empty());
            }
            return Stream.of(ITEMS.getValue(resourceLocation));
        }
        catch(Exception e){
            return empty();
        }
    }
}
