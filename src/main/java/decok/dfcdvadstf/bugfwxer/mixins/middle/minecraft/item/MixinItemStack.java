package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.item;

import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 修复创造模式物品栏快速切换标签页时的 NPE 崩溃。
 * <p>
 * 当玩家在创造模式物品栏中快速切换标签页时，物品列表的重建与渲染之间
 * 存在竞态条件：渲染线程可能拿到一个 getItem() == null 的 ItemStack，
 * 而 hasEffect() 没有空值保护，直接调用 null.getItem().hasEffect(this) 导致 NPE。
 * </p>
 * <p>
 * 崩溃路径：
 * <pre>
 *   ItemStack.func_94608_d (hasEffect) → NPE: getItem() == null
 *     ← ForgeHooksClient.renderInventoryItem
 *       ← RenderItem.func_82406_b
 *         ← GuiContainerCreative.func_147051_a (渲染创造物品栏)
 * </pre>
 * </p>
 *
 * @see ItemStack#func_94608_d() hasEffect()
 * @author Seniye
 */
@Mixin(ItemStack.class)
public class MixinItemStack {

    /**
     * 在 hasEffect() 开头检查 getItem() 是否为 null，
     * 若为 null 则直接返回 false，避免 NPE。
     */
    @Inject(
        method = "func_94608_d",
        at = @At("HEAD"),
        cancellable = true
    )
    private void bugfixer$checkItemNullBeforeHasEffect(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() == null) {
            cir.setReturnValue(false);
        }
    }
}
