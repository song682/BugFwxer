package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemMonsterPlacer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemMonsterPlacer.class)
public abstract class MixinItemMonsterPlacer {

    /**
     * <p>
     *     在生物蛋的工具提示中添加和平模式警告。<br>
     *     当游戏难度为和平模式且该生物蛋生成的是怪物时，在 tooltip 中显示红色提示
     * </p>
     * <p>
     *     使用 @Inject 注入到 ItemMonsterPlacer 从父类继承的 addInformation 方法末尾
     *     每次鼠标悬停都会重新调用，因此难度变化时提示会实时更新
     * </p>
     *
     * @author Seniye
     */
    @Intrinsic
    public void addInformation(ItemStack itemStack, EntityPlayer player, List tooltipList, boolean advancedTooltips) {
        // 直接从客户端 GameSettings 读取难度，玩家改难度时立刻生效
        EnumDifficulty difficulty = Minecraft.getMinecraft().gameSettings.difficulty;

        if (difficulty == EnumDifficulty.PEACEFUL) {
            World world = Minecraft.getMinecraft().theWorld;
            if (world != null) {
                // 获取生物蛋对应的实体 ID
                int entityID = itemStack.getItemDamage();

                // 检查这个实体是否是怪物
                if (isMonster(entityID, world)) {
                    // 在 tooltip 中添加警告信息
                    tooltipList.add("§c" + I18n.format("tooltip.spawn_egg.peaceful"));
                }
            }
        }
    }
    
    /**
     * 判断指定的实体 ID 是否对应怪物
     */
    @Unique
    private boolean isMonster(int entityID, World world) {
        // 尝试创建实体实例来判断类型
        Entity entity = EntityList.createEntityByID(entityID, world);
        
        if (entity != null) {
            boolean isMonster = entity instanceof EntityMob;
            // 创建后立即移除，避免真的生成出来
            entity.setDead();
            return isMonster;
        }
        return false;
    }
    /**
     * 在 onItemUse 方法开头拦截，和平模式下阻止怪物蛋使用。
     * 必须在 HEAD 取消，否则生成逻辑已经执行完毕，怪物已经刷出来了。
     */
    @Inject(method = "onItemUse", at = @At("HEAD"), cancellable = true)
    private void onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, CallbackInfoReturnable<Boolean> cir) {
        if (world.difficultySetting == EnumDifficulty.PEACEFUL && isMonster(stack.getItemDamage(), world)) {
            cir.setReturnValue(false);
        }
    }
}
