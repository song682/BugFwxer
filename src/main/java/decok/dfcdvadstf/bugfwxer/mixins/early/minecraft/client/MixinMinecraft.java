package decok.dfcdvadstf.bugfwxer.mixins.early.minecraft.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <p>
 *     MixinMinecraft<br>
 *     Blocks only the Alt+F4 window close request; the title-bar close button
 *     still works normally.<br>
 *     仅阻拦 Alt+F4 触发的窗口关闭请求；标题栏关闭按钮仍可正常退出。
 * </p>
 * <p>
 *     Vanilla checks {@code Display.isCloseRequested()} once per frame inside
 *     {@code runGameLoop()} and calls {@code shutdown()} when it returns true.
 *     Both Alt+F4 and the title-bar button raise the same WM_CLOSE request, so
 *     LWJGL cannot tell them apart directly; instead, when a close request
 *     arrives we inspect the polled keyboard state — Alt + F4 still being held
 *     down identifies an Alt+F4 close, which is then swallowed (LWJGL clears
 *     its internal flag on read).<br>
 *     原版在 {@code runGameLoop()} 中每帧检查一次 {@code Display.isCloseRequested()}，
 *     为 true 时调用 {@code shutdown()}。Alt+F4 与标题栏按钮产生的是同一个 WM_CLOSE
 *     请求，LWJGL 层面无法直接区分来源；因此在关闭请求到达时检查键盘轮询状态——
 *     Alt 与 F4 仍处于按下状态即判定为 Alt+F4 关闭，予以吞掉
 *     （LWJGL 在读取时会重置内部标志位）。
 * </p>
 *
 * @author Seniye
 * @see Minecraft#shutdown() Minecraft.shutdown
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /**
     * Redirects the close-request poll: swallow it only when Alt+F4 is held,
     * otherwise pass the real value through (title-bar close stays usable).
     * 重定向关闭请求轮询：仅在 Alt+F4 按下时吞掉请求，
     * 否则透传真实值（标题栏关闭按钮保持可用）。
     */
    @Redirect(
            method = "runGameLoop",
            at = @At(
                    value = "INVOKE",
                    target = "Lorg/lwjgl/opengl/Display;isCloseRequested()Z",
                    remap = false
            )
    )
    private boolean bugfwxer$blockAltF4Close() {
        // Poll LWJGL once: this consumes the pending close request (flag resets on read)
        // 只轮询一次 LWJGL：读取即消费掉挂起的关闭请求（标志位读后重置）
        boolean closeRequested = Display.isCloseRequested();

        // Alt + F4 still held down at this frame identifies an Alt+F4-triggered close
        // 本帧 Alt 与 F4 仍处于按下状态，即判定为 Alt+F4 触发的关闭
        if (closeRequested && Keyboard.isCreated()
                && Keyboard.isKeyDown(Keyboard.KEY_F4)
                && (Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU))) {
            return false;
        }

        // Title-bar close button (or any non-Alt+F4 source): let vanilla shut down normally
        // 标题栏关闭按钮（或其他非 Alt+F4 来源）：交给原版正常关闭
        return closeRequested;
    }
}
