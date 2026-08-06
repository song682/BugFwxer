package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.audio;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
 * <p>
 * Implementation note: the music entries are temporarily removed from
 * {@code playingSounds} before the vanilla loop runs (HEAD) and put back
 * afterwards (TAIL). Because the map is a Guava HashBiMap the inverse map is
 * maintained automatically, and the vanilla code simply never sees the music
 * channel ids, so it cannot pause/resume them.
 * </p>
 * <p>
 * 实现说明：在原版遍历（HEAD）之前把音乐条目临时从 {@code playingSounds}
 * 中移除，遍历结束（TAIL）再放回。由于该映射是 Guava HashBiMap，反向映射会
 * 自动同步维护，原版代码根本看不到音乐声道 id，自然无法暂停/恢复它们。
 * </p>
 *
 * @author Seniye
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManager {

    /**
     * Map of currently playing sounds, channel id to ISound. A Guava HashBiMap,
     * so the inverse map stays in sync on remove/put.
     * 当前播放中的声音映射表（声道 id → ISound）。Guava HashBiMap，
     * remove/put 时反向映射自动同步。
     */
    @Shadow
    private Map playingSounds;

    /**
     * Reference to the sound handler, used to resolve a sound's category.
     * 用于解析声音类别的音效处理器引用。
     */
    @Shadow
    private SoundHandler sndHandler;

    /**
     * Music entries hidden from the vanilla pause/resume loop; restored at TAIL.
     * 从原版暂停/恢复循环中隐藏的音乐条目，TAIL 时恢复。
     */
    private final List<Map.Entry> bugfwxer$hiddenMusic = new ArrayList<Map.Entry>();

    /**
     * Removes music entries from playingSounds before the vanilla pause loop.
     * 在原版暂停循环前把音乐条目从 playingSounds 中移除。
     */
    @Inject(method = "pauseAllSounds", at = @At("HEAD"))
    private void bugfwxer$hideMusicBeforePause(CallbackInfo ci) {
        this.bugfwxer$hideMusic();
    }

    /**
     * Puts the hidden music entries back after the vanilla pause loop.
     * 原版暂停循环结束后恢复被隐藏的音乐条目。
     */
    @Inject(method = "pauseAllSounds", at = @At("TAIL"))
    private void bugfwxer$restoreMusicAfterPause(CallbackInfo ci) {
        this.bugfwxer$restoreMusic();
    }

    /**
     * Removes music entries from playingSounds before the vanilla resume loop.
     * 在原版恢复循环前把音乐条目从 playingSounds 中移除。
     */
    @Inject(method = "resumeAllSounds", at = @At("HEAD"))
    private void bugfwxer$hideMusicBeforeResume(CallbackInfo ci) {
        this.bugfwxer$hideMusic();
    }

    /**
     * Puts the hidden music entries back after the vanilla resume loop.
     * 原版恢复循环结束后恢复被隐藏的音乐条目。
     */
    @Inject(method = "resumeAllSounds", at = @At("TAIL"))
    private void bugfwxer$restoreMusicAfterResume(CallbackInfo ci) {
        this.bugfwxer$restoreMusic();
    }

    /**
     * Removes every music entry from playingSounds and stashes it for restore.
     * 把全部音乐条目从 playingSounds 中移除并暂存待恢复。
     */
    private void bugfwxer$hideMusic() {
        this.bugfwxer$hiddenMusic.clear();
        Iterator iterator = this.playingSounds.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (this.bugfwxer$isMusicSound((ISound) entry.getValue())) {
                this.bugfwxer$hiddenMusic.add(entry);
                iterator.remove();
            }
        }
    }

    /**
     * Restores the stashed music entries into playingSounds.
     * 把暂存的音乐条目恢复进 playingSounds。
     */
    private void bugfwxer$restoreMusic() {
        for (Map.Entry entry : this.bugfwxer$hiddenMusic) {
            this.playingSounds.put(entry.getKey(), entry.getValue());
        }
        this.bugfwxer$hiddenMusic.clear();
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
