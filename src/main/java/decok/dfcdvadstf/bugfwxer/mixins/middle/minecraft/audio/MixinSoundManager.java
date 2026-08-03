package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import paulscode.sound.SoundSystem;

import java.util.Map;

/**
 * MixinSoundManager
 * Keeps background music (SoundCategory.MUSIC) alive across GUI switches:
 * {@code pauseAllSounds()} / {@code resumeAllSounds()} now skip music channels,
 * so opening and closing the pause menu no longer round-trips the MusicTicker
 * track through paulscode's streaming pause/resume — which is what killed the
 * music and left a 10-20 minute silent gap behind.
 * 保持背景音乐（SoundCategory.MUSIC）在 GUI 切换期间持续播放：
 * {@code pauseAllSounds()} / {@code resumeAllSounds()} 跳过音乐类别的声道，
 * 打开/关闭暂停菜单不再让 MusicTicker 曲目经历 paulscode 流式暂停/恢复的往返——
 * 正是该往返导致音乐中断并留下 10~20 分钟的音乐静默。
 *
 * @author Seniye
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    /**
     * Inverse map of currently playing sounds, mapping channel id back to the
     * ISound. 声道 id 反查 ISound 的映射表。
     */
    @Shadow
    private Map invPlayingSounds;

    /**
     * Reference to the sound handler, used to resolve a sound's category.
     * 用于解析声音类别的音效处理器引用。
     */
    @Shadow
    private SoundHandler sndHandler;

    /**
     * Skips the pause for music channels while still pausing every other sound.
     * 暂停所有非音乐声道，音乐声道保持继续播放。
     */
    @Redirect(method = "pauseAllSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;pause(Ljava/lang/String;)V", remap = false))
    private void bugfwxer$pauseExceptMusic(SoundSystem sndSystem, String s) {
        ISound isound = (ISound) this.invPlayingSounds.get(s);

        if (isound == null || !this.bugfwxer$isMusicSound(isound)) {
            sndSystem.pause(s);
        }
    }

    /**
     * Skips the resume for music channels; they were never paused, and calling
     * play() again on an already-streaming source is exactly the kind of extra
     * round-trip this fix wants to avoid.
     * 恢复所有非音乐声道；音乐声道从未被暂停，对仍在流式播放的源再次 play()
     * 正是本修复希望避免的多余往返。
     */
    @Redirect(method = "resumeAllSounds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/audio/SoundManager$SoundSystemStarterThread;play(Ljava/lang/String;)V", remap = false))
    private void bugfwxer$resumeExceptMusic(SoundSystem sndSystem, String s) {
        ISound isound = (ISound) this.invPlayingSounds.get(s);

        if (isound == null || !this.bugfwxer$isMusicSound(isound)) {
            sndSystem.play(s);
        }
    }

    /**
     * Resolves whether the given sound belongs to the music category.
     * 判断给定声音是否属于音乐类别。
     */
    private boolean bugfwxer$isMusicSound(ISound sound) {
        try {
            return this.sndHandler.getSound(sound.getPositionedSoundLocation())
                    .getSoundCategory() == SoundCategory.MUSIC;
        } catch (RuntimeException e) {
            // Registry lookup can fail for exotic sounds; treat them as non-music
            // 罕见声音的注册表查询可能失败；按非音乐处理
            return false;
        }
    }
}
