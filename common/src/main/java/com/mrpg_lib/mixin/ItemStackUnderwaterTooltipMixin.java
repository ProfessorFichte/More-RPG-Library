package com.mrpg_lib.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.mrpg_lib.util.UnderwaterProjectiles;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackUnderwaterTooltipMixin {
    @Inject(method = "getTooltip", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/item/Item;appendTooltip(Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/Item$TooltipContext;Ljava/util/List;Lnet/minecraft/item/tooltip/TooltipType;)V",
            shift = At.Shift.AFTER))
    private void mrpg$underwaterLine(Item.TooltipContext context, PlayerEntity player, TooltipType type,
                                     CallbackInfoReturnable<List<Text>> cir, @Local(ordinal = 0) List<Text> tooltip) {
        if (UnderwaterProjectiles.isWeapon((ItemStack) (Object) this)) {
            tooltip.add(UnderwaterProjectiles.tooltip());
        }
    }
}
