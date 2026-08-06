package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiContainerCreative;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 * Fix MC-179165: releasing the mouse over a creative inventory tab while
 * dragging the scrollbar accidentally switches to that tab.<br>
 * 修复 MC-179165：拖动创造模式物品栏滚动条时，若在某个标签上松开鼠标，
 * 会意外切换到该标签。
 * </p>
 * <p>
 * Root cause / 根本原因：<br>
 * {@code mouseMovedOrUp} unconditionally hit-tests every tab on a mouse
 * release, while the scrollbar drag state ({@code isScrolling}) is only
 * maintained inside {@code drawScreen}. Since input events are processed
 * before rendering each frame, releasing the button after a drag still has
 * {@code isScrolling == true} and the release is wrongly treated as a tab
 * click.<br>
 * {@code mouseMovedOrUp} 在鼠标释放时无条件对所有标签做碰撞检测，
 * 而滚动条拖动状态（{@code isScrolling}）只在 {@code drawScreen} 中维护。
 * 由于每帧先处理输入再渲染，拖动后松开鼠标时 {@code isScrolling} 仍为
 * {@code true}，释放事件因此被误判为标签点击。
 * </p>
 * <p>
 * Fix (same approach as Mojang's later vanilla fix) / 修复方案（与 Mojang
 * 后续版本的原版修复一致）：<br>
 * While the scrollbar is being dragged, consume the left-button release
 * entirely so it can never reach the tab hit-test.<br>
 * 滚动条拖动期间直接吞掉左键释放事件，使其永远不会进入标签碰撞检测。
 * </p>
 *
 * @author Seniye
 */
@Mixin(GuiContainerCreative.class)
public abstract class MixinGuiContainerCreative {

    /** True if the scrollbar is being dragged. 滚动条是否正在被拖动。 */
    @Shadow
    private boolean isScrolling;

    /**
     * Swallows the mouse release while the scrollbar is being dragged, so the
     * release position over a tab no longer triggers a tab switch.
     * 滚动条拖动期间吞掉鼠标释放事件，使落在标签上的释放位置不再触发标签切换。
     */
    @Inject(method = { "mouseMovedOrUp" }, at = { @At("HEAD") }, cancellable = true)
    private void bugfwxer$consumeReleaseWhileScrolling(int p_146286_1_, int p_146286_2_, int p_146286_3_,
            CallbackInfo ci) {
        // After dragging, the release coordinates may legitimately end up over a
        // tab; only clicks that began outside the scrollbar may select a tab.
        // 拖动后松开时，鼠标坐标完全可能恰好落在某个标签上；
        // 只有并非始于滚动条的点击才允许选中标签。
        if (p_146286_3_ == 0 && this.isScrolling) {
            this.isScrolling = false;
            ci.cancel();
        }
    }
}
