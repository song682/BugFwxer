package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.block.Block;
import net.minecraft.block.IGrowable;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemDye.class)
public abstract class MixinItemDye {
    
    /**
     * 修复植物在超出高度限制时使用骨粉继续生长的问题
     * 使用 MixinExtras 的 @ModifyExpressionValue 直接修改 applyBonemeal 的返回值
     * @author Seniye
     */
    @ModifyExpressionValue(
        method = "onItemUse",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemDye;applyBonemeal(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;IIILnet/minecraft/entity/player/EntityPlayer;)Z"
        )
    )
    private boolean checkPlantHeightLimit(boolean original, ItemStack p_77648_1_, EntityPlayer p_77648_2_, World p_77648_3_, int p_77648_4_, int p_77648_5_, int p_77648_6_) {
        // 动态获取世界高度限制，兼容修改世界高度的模组
        int maxHeight = p_77648_3_.getHeight();
        
        // 检查植物当前位置是否已经达到或接近高度限制
        if (p_77648_5_ >= maxHeight - 1) {
            // 检查这个方块是否是可生长的植物
            Block block = p_77648_3_.getBlock(p_77648_4_, p_77648_5_, p_77648_6_);
            
            if (block instanceof IGrowable) {
                IGrowable igrowable = (IGrowable) block;
                
                // 如果这个植物还可以生长
                if (igrowable.func_149851_a(p_77648_3_, p_77648_4_, p_77648_5_, p_77648_6_, p_77648_3_.isRemote)) {
                    // 在服务器端发送聊天消息
                    if (!p_77648_3_.isRemote && p_77648_2_ != null) {
                        p_77648_2_.addChatMessage(new ChatComponentText(I18n.format("chat.bone_meal.limit")));
                    }
                    
                    // 返回 false 阻止骨粉消耗和植物生长
                    return false;
                }
            }
        }
        
        // 否则返回原结果
        return original;
    }
}
