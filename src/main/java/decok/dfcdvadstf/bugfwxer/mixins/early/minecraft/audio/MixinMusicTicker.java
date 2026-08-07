package decok.dfcdvadstf.bugfwxer.mixins.early.minecraft.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.util.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

/**
 * MixinMusicTicker
 * Fallback safety net for the GUI-switch music fix: if the background track
 * disappears abnormally shortly after it started (e.g. a paulscode streaming
 * hiccup, a driver quirk or a future mod interfering), restart it after a short
 * delay instead of letting the vanilla 10-20 minute silent gap kick in.
 * GUI 切换音乐修复的兜底保险：若背景曲目开播不久便异常消失
 * （如 paulscode 流式播放故障、驱动异常或未来其他模组干扰），
 * 用短延迟快速重播，而不是陷入原版 10~20 分钟的音乐静默。
 *
 * @author Seniye
 */
@Mixin(MusicTicker.class)
public abstract class MixinMusicTicker {

    /**
     * A track younger than this (in ms) is considered abnormally interrupted when
     * it disappears. 曲目短于该时长（毫秒）即消失时视为异常中断。
     */
    private static final long INTERRUPT_THRESHOLD_MS = 60000L;

    /** System time when the current track started playing. 当前曲目开始播放时的系统时间。 */
    private long musicStartedAt;

    @Shadow
    private Random field_147679_a;

    @Shadow
    private Minecraft field_147677_b;

    @Shadow
    private ISound field_147678_c;

    @Shadow
    private int field_147676_d;

    /**
     * Detects an abnormally early track loss and shrinks the restart delay, or
     * records the start time right before a new track is about to play.
     * 检测曲目过早消失并压缩重启延迟，或在即将开播新曲目前记录开始时间。
     */
    @Inject(method = "update", at = @At("HEAD"))
    private void bugfwxer$fastRestartOnEarlyInterrupt(CallbackInfo ci) {
        if (this.field_147678_c != null) {
            long playedMs = Minecraft.getSystemTime() - this.musicStartedAt;

            if (playedMs < INTERRUPT_THRESHOLD_MS
                    && !this.field_147677_b.getSoundHandler().isSoundPlaying(this.field_147678_c)) {
                // The track vanished way too early — treat it as an abnormal interruption and
                // shrink the restart delay; the vanilla Math.min(long delay, field_147676_d)
                // in update() then picks this short value, so music returns in a few seconds.
                // 曲目消失得过早——视为异常中断并压缩重启延迟；
                // update() 中原版的 Math.min(长延迟, field_147676_d) 会取到这个短值，音乐数秒内即可回归。
                this.field_147676_d = MathHelper.getRandomIntegerInRange(this.field_147679_a, 20, 200);
            }
        } else if (this.field_147676_d <= 0) {
            // A new track is about to start this tick; record its start time.
            // 本 tick 即将开播新曲目；记录其开始时间。
            this.musicStartedAt = Minecraft.getSystemTime();
        }
    }
}
