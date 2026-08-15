package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.audio;

import decok.dfcdvadstf.bugfwxer.BugFwxer;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystemConfig;

/**
 * <p>
 * MixinSoundManagerChannels<br>
 * Raises paulscode's channel counts from the 1.7.10 defaults (28 normal + 4
 * streaming) to the modern Minecraft standard (120 normal + 8 streaming),
 * which reduces sounds being cut off or dropped when many effects play at
 * once.<br>
 * 将 paulscode 的声道数从 1.7.10 默认值（28 普通 + 4 流式）提升到现代
 * Minecraft 标准（120 普通 + 8 流式），减少多个音效同时播放时
 * 声音被截断或丢失的问题。
 * </p>
 * <p>
 * Minecraft 1.8+ applies exactly these numbers in its own SoundManager; the
 * same values are safe here. paulscode creates as many channels as the backend
 * can provide when it cannot create all of them, so limited drivers never
 * crash, they just cap the concurrency.<br>
 * Minecraft 1.8+ 的 SoundManager 正是使用这组数值；此处沿用同样安全。
 * 当后端无法创建全部声道时 paulscode 会尽量多建，因此驱动受限时不会崩溃，
 * 只是并发声道数被封顶。
 * </p>
 *
 * @author Seniye
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManagerChannels {

    /**
     * Normal (non-streaming) channel count used by modern Minecraft.
     * 现代 Minecraft 使用的普通声道数。
     */
    private static final int MODERN_NORMAL_CHANNELS = 120;

    /**
     * Streaming channel count used by modern Minecraft.
     * 现代 Minecraft 使用的流式声道数。
     */
    private static final int MODERN_STREAMING_CHANNELS = 8;

    /**
     * Applies the modern channel counts at the end of the SoundManager
     * constructor; the SoundSystem itself is created later in
     * {@code loadSoundSettings()}, so it picks up the new values when its
     * channels are allocated.<br>
     * 在 SoundManager 构造函数末尾应用现代声道数；SoundSystem 本身在
     * {@code loadSoundSettings()} 中才创建，因此分配声道时会读取到新值。
     */
    @Inject(method = "<init>(Lnet/minecraft/client/audio/SoundHandler;Lnet/minecraft/client/settings/GameSettings;)V",
            at = @At("TAIL"))
    private void bugfwxer$onConstructTail(SoundHandler handler, GameSettings settings, CallbackInfo ci) {
        SoundSystemConfig.setNumberNormalChannels(MODERN_NORMAL_CHANNELS);
        SoundSystemConfig.setNumberStreamingChannels(MODERN_STREAMING_CHANNELS);
        BugFwxer.logger.info("Sound channel counts set to " + MODERN_NORMAL_CHANNELS
                + " normal + " + MODERN_STREAMING_CHANNELS + " streaming (modern Minecraft standard)");
    }
}
