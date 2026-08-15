package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.audio;


import decok.dfcdvadstf.bugfwxer.BugFwxer;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.settings.GameSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.SoundSystemConfig;

import java.util.LinkedList;

/**
 * <p>
 * MixinSoundManagerJavaSound<br>
 * Forces paulscode {@code LibraryJavaSound} to the front of the library list
 * so the audio output device switcher actually works.<br>
 * 将 paulscode {@code LibraryJavaSound} 强制放到库列表最前，
 * 使音频输出设备切换功能真正生效。
 * </p>
 * <p>
 * Minecraft 1.7.10 registers {@code LibraryLWJGLOpenAL} as the only library in
 * a static block, which makes {@code LibraryJavaSound.setMixer()} useless
 * (no instance, no channel migration). This Mixin inserts LibraryJavaSound at
 * the front of the list after the constructor finishes, so SoundSystem tries
 * it before OpenAL.<br>
 * Minecraft 1.7.10 在静态块中只注册了 {@code LibraryLWJGLOpenAL}，
 * 这会让 {@code LibraryJavaSound.setMixer()} 失去作用
 * （没有实例，声道无法迁移）。本 Mixin 在构造函数结束后把
 * LibraryJavaSound 插到列表最前，SoundSystem 会在 OpenAL 之前尝试它。
 * </p>
 *
 * @author Seniye
 */
@Mixin(SoundManager.class)
public abstract class MixinSoundManagerJavaSound {

    /**
     * Prepend LibraryJavaSound to the SoundSystem library list at the end of
     * the SoundManager constructor; the SoundSystem itself is created later in
     * {@code loadSoundSettings()}, so the first-tried library is now JavaSound.
     * 在 SoundManager 构造函数末尾把 LibraryJavaSound 前置到 SoundSystem
     * 库列表；SoundSystem 本身在 {@code loadSoundSettings()} 中才创建，
     * 因此首选库现在变为 JavaSound。
     */
    @Inject(method = "<init>(Lnet/minecraft/client/audio/SoundHandler;Lnet/minecraft/client/settings/GameSettings;)V",
            at = @At("TAIL"))
    private void bugfwxer$onConstructTail(SoundHandler handler, GameSettings settings, CallbackInfo ci) {
        try {
            Class<?> javaSoundLib = Class.forName("paulscode.sound.libraries.LibraryJavaSound");
            LinkedList<Class> libs = SoundSystemConfig.getLibraries();
            if (libs != null) {
                // Remove if already present (shouldn't be, but just in case)
                // 若已存在则先移除（正常情况下不会，仅作防御）
                libs.remove(javaSoundLib);
                // Add to front so SoundSystem tries it before OpenAL
                // 添加到最前，让 SoundSystem 在 OpenAL 之前尝试它
                libs.addFirst(javaSoundLib);
                BugFwxer.logger.info("LibraryJavaSound set as primary audio library");
            }
        } catch (Exception e) {
            BugFwxer.logger.warn("Failed to configure LibraryJavaSound as primary library", e);
        }
    }
}
