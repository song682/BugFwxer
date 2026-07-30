package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.client;

import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.Display;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * <p>
 *     MixinMinecraft<br>
 *     Blocks the window close request (Alt+F4 / title-bar close button) so the
 *     game can only be exited through the in-game menu.<br>
 *     阻拦窗口关闭请求（Alt+F4 / 标题栏关闭按钮），使游戏只能通过游戏内菜单退出。
 * </p>
 * <p>
 *     Vanilla checks {@code Display.isCloseRequested()} once per frame inside
 *     {@code runGameLoop()} and calls {@code shutdown()} when it returns true.
 *     This redirect consumes the pending close request (LWJGL clears its
 *     internal flag on read) and reports "no close requested" instead.<br>
 *     原版在 {@code runGameLoop()} 中每帧检查一次 {@code Display.isCloseRequested()}，
 *     为 true 时调用 {@code shutdown()}。本 Redirect 消费掉挂起的关闭请求
 *     （LWJGL 在读取时会重置内部标志位），并返回“未请求关闭”。
 * </p>
 *
 * @author Seniye
 * @see net.minecraft.client.Minecraft#shutdown() Minecraft.shutdown
 */
@Mixin(Minecraft.class)
public abstract class MixinMinecraft {

    /**
     * Redirects the close-request poll in the game loop to always report false.
     * 将游戏循环中的关闭请求轮询重定向为恒定返回 false。
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
        // Still poll LWJGL so the pending close request is consumed and the flag resets;
        // the return value is deliberately discarded to keep the game loop alive.
        // 仍然轮询 LWJGL 以消费掉挂起的关闭请求并重置标志位；
        // 有意丢弃返回值以维持游戏循环继续运行。
        Display.isCloseRequested();
        return false;
    }
}
