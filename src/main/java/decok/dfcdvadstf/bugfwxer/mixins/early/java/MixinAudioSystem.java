package decok.dfcdvadstf.bugfwxer.mixins.early.java;

import decok.dfcdvadstf.bugfwxer.BugFwxer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.sound.sampled.Mixer;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Early Mixin for {@code javax.sound.sampled.AudioSystem}: filters virtual and
 * recording-only mixers out of {@code getMixerInfo()} and repairs mojibake in
 * device names, so the audio output device switcher sees a clean, readable
 * device list.<br>
 * 针对 {@code javax.sound.sampled.AudioSystem} 的早期 Mixin：从
 * {@code getMixerInfo()} 中过滤掉虚拟设备与纯录音混音器，并修复设备名乱码，
 * 让音频输出设备切换器拿到干净可读的设备列表。
 * </p>
 * <p>
 * Note: the "Java Sound Audio Engine" mixer is deliberately NOT filtered —
 * paulscode's {@code LibraryJavaSound.libraryCompatible()} requires it to be
 * present, otherwise SoundSystem falls back to OpenAL and mixer switching
 * silently stops working.<br>
 * 注意：刻意不过滤 "Java Sound Audio Engine" 混音器——paulscode 的
 * {@code LibraryJavaSound.libraryCompatible()} 依赖它存在，
 * 否则 SoundSystem 会退回 OpenAL，混音器切换将悄然失效。
 * </p>
 *
 * @author Seniye
 */
@Mixin(value = javax.sound.sampled.AudioSystem.class, remap = false)
public class MixinAudioSystem {

    /**
     * Filters the mixer list returned by {@code getMixerInfo()} and repairs
     * mojibake device names in place.
     * 过滤 {@code getMixerInfo()} 返回的混音器列表，并就地修复乱码设备名。
     */
    @Inject(method = "getMixerInfo", at = @At("RETURN"), cancellable = true)
    private static void bugfwxer$onGetMixerInfo(CallbackInfoReturnable<Mixer.Info[]> cir) {
        Mixer.Info[] original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        List<Mixer.Info> filtered = new ArrayList<>();

        for (Mixer.Info info : original) {
            String name = info.getName();

            // Skip null or empty names 跳过 null 或空名称
            if (name == null || name.trim().isEmpty()) {
                continue;
            }

            // Filter virtual/recording-only devices 过滤虚拟设备与录音设备
            if (bugfwxer$shouldFilterDevice(name)) {
                BugFwxer.logger.debug("Filter virtual audio device: " + name);
                continue;
            }

            // Fix encoding issues - replace the Mixer.Info if the name changed
            // 修复编码问题——名称变化时用新的 Mixer.Info 替换原对象
            String fixedName = bugfwxer$fixEncoding(name);
            if (!fixedName.equals(name)) {
                BugFwxer.logger.debug("Fix audio device name: " + name + " -> " + fixedName);
                info = bugfwxer$createFixedMixerInfo(info, fixedName);
            }

            filtered.add(info);
        }

        cir.setReturnValue(filtered.toArray(new Mixer.Info[0]));
    }

    /**
     * Decides whether the device should be filtered out. Kept in sync with
     * {@code AudioDeviceMonitor.shouldFilterDevice}.
     * 判断设备是否应被过滤。与 {@code AudioDeviceMonitor.shouldFilterDevice} 保持同步。
     */
    private static boolean bugfwxer$shouldFilterDevice(String deviceName) {
        String lowerName = deviceName.toLowerCase();

        // Extended filter list (shared with AudioDeviceMonitor) 扩展过滤列表（与 AudioDeviceMonitor 共享）
        String[] filters = {
            "primary sound capture",   // Main recording device 主录音设备
            "port",                     // MIDI port MIDI 端口
            "microsoft gs wavetable",  // MIDI synthesizer MIDI 合成器
            "unknown",                  // Unknown device 未知设备
            "default",                  // Default placeholder 默认占位符
            "sndvol",                   // Volume control virtual device 音量控制虚拟设备
            "stereomix",               // Stereo mix (recording) 立体声混音（录音设备）
            "what u hear",             // Recording device 录音设备
            "wave",                    // Wave device 波形设备
            "aux",                     // Auxiliary device 辅助设备
            "cd",                      // CD device CD 设备
            "line in",                 // Line input 线路输入
            "microphone",              // Microphone input 麦克风输入
            "phone",                   // Phone / hands-free device 电话设备
        };

        for (String filter : filters) {
            if (lowerName.contains(filter)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Tries to repair mojibake in a device name.
     * 尝试修复设备名中的乱码。
     */
    private static String bugfwxer$fixEncoding(String name) {
        // Normal ASCII or common characters need no repair
        // 正常 ASCII 或常见字符无需修复
        if (bugfwxer$isValidName(name)) {
            return name;
        }

        // Drop replacement/unknown characters and keep only printable ASCII + CJK
        // 去除替换字符/未知字符，仅保留可打印 ASCII 与中日韩字符
        String fixed = name
            .replace("��", "")
            .replace("�", "")
            .replaceAll("[^\\x20-\\x7E\\u4e00-\\u9fa5]", "");

        return fixed.trim();
    }

    /**
     * Checks whether the name looks clean (no obvious mojibake).
     * 检查名称是否干净（无明显乱码）。
     */
    private static boolean bugfwxer$isValidName(String name) {
        for (char c : name.toCharArray()) {
            // Control characters (besides tab/newline/carriage return) may be mojibake
            // 控制字符（制表符/换行/回车除外）可能是乱码
            if (c < 32 && c != '\t' && c != '\n' && c != '\r') {
                return false;
            }
            // The Unicode replacement character marks an encoding problem
            // Unicode 替换字符说明存在编码问题
            if (c == '\uFFFD') {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a replacement Mixer.Info with the repaired name.
     * Mixer.Info 是抽象类，因此创建一个带修复后名称的新实例。
     */
    private static Mixer.Info bugfwxer$createFixedMixerInfo(Mixer.Info original, String fixedName) {
        return new Mixer.Info(
            fixedName,
            original.getVendor(),
            original.getDescription(),
            original.getVersion()
        ) {};
    }
}
