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
 * 而 hasEffect(int) 没有空值保护，直接调用 this.getItem().hasEffect(this, pass) 导致 NPE。
 * </p>
 * <p>
 * 崩溃路径：
 * <pre>
 *   ItemStack.func_94608_d (hasEffect) → hasEffect(int) → NPE: getItem() == null
 *     ← ForgeHooksClient.renderInventoryItem
 *       ← RenderItem.func_82406_b
 *         ← GuiContainerCreative.func_147051_a (渲染创造物品栏)
 * </pre>
 * 注意：原版的 hasEffect()（SRG: func_94608_d）已被 Forge 改为委托给
 * Forge 新增的 hasEffect(int)，因此只需守护 hasEffect(int) 即可同时覆盖两条入口。
 * </p>
 *
 * @see ItemStack#hasEffect(int)
 * @author Seniye
 */
@Mixin(ItemStack.class)
public class MixinItemStack {

    /**
     * Guard at the head of hasEffect(int): return false if getItem() is null to avoid the NPE.
     * 在 hasEffect(int) 开头检查 getItem() 是否为 null，若为 null 则直接返回 false，避免 NPE。
     * <p>
     * hasEffect(int) 是 Forge 新增方法，不参与 SRG 混淆，开发与生产环境同名，
     * 因此必须 remap = false，且不能使用 func_94608_d 这类 SRG 名。
     * </p>
     */
    @Inject(
        method = "hasEffect(I)Z",
        at = @At("HEAD"),
        cancellable = true,
        remap = false
    )
    private void bugfixer$checkItemNullBeforeHasEffect(CallbackInfoReturnable<Boolean> cir) {
        ItemStack self = (ItemStack) (Object) this;
        if (self.getItem() == null) {
            cir.setReturnValue(false);
        }
    }
}
