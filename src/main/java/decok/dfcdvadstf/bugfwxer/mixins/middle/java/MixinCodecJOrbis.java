package decok.dfcdvadstf.bugfwxer.mixins.middle.java;

import decok.dfcdvadstf.bugfwxer.BugFwxer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;

import javax.sound.sampled.AudioFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * Mixin for {@code paulscode.sound.codecs.CodecJOrbis}: replaces the quadratic
 * copy storm in {@code readAll()} with a chunked read.<br>
 * 针对 {@code paulscode.sound.codecs.CodecJOrbis} 的 Mixin：把
 * {@code readAll()} 中的二次方复制风暴替换为分块读取。
 * </p>
 * <p>
 * The vanilla implementation reallocates and copies the whole accumulated
 * array for every ~16 KB chunk it decodes, while paulscode holds its global
 * lock — decoding a single large OGG sound can therefore copy hundreds of
 * megabytes and stall the entire sound system (all play/stop commands and
 * streamed music queue behind the lock). Looping {@code read()} (which stops
 * at the configured streaming-buffer size, 128 KB by default) and joining the
 * chunks once yields the same decoded data for a fraction of the work.<br>
 * 原版实现每解码一个约 16KB 的块就把整个累计数组重新分配并复制一遍，
 * 且全程持有 paulscode 全局锁——解码单个大体积 OGG 音效会复制数百 MB 数据，
 * 使整个声音系统停顿（所有播放/停止命令与流式音乐都在锁后排队）。
 * 循环调用 {@code read()}（默认在配置的流式缓冲大小 128KB 处停止）
 * 并一次性拼接各块，即可用少得多的复制量得到相同的解码数据。
 * </p>
 * <p>
 * Only the non-streaming path ({@code readAll()}, used for cached sound
 * effects) is touched; streamed music keeps using {@code read()} unchanged.
 * The {@code maxFileSize} limit from {@link SoundSystemConfig} is respected,
 * matching the intent of the upstream setting.<br>
 * 仅修改非流式路径（{@code readAll()}，用于缓存音效）；流式音乐仍走
 * 未修改的 {@code read()}。同时遵循 {@link SoundSystemConfig} 中的
 * {@code maxFileSize} 限制，与上游设置的本意一致。
 * </p>
 *
 * @author Seniye
 */
@Mixin(value = CodecJOrbis.class, remap = false)
public abstract class MixinCodecJOrbis {

    /**
     * Reads one stream buffer worth of audio data (up to the configured
     * streaming-buffer size). 读取一块音频数据（最多到配置的流式缓冲大小）。
     */
    @Shadow
    public abstract SoundBuffer read();

    /**
     * Returns true once the whole stream has been decoded.
     * 整个流解码完毕后返回 true。
     */
    @Shadow
    public abstract boolean endOfStream();

    /**
     * Returns the format of the decoded data. 返回解码数据的格式。
     */
    @Shadow
    public abstract AudioFormat getAudioFormat();

    /**
     * Intercepts {@code readAll()} at HEAD and fully replaces it with the
     * chunked implementation; the quadratic vanilla loop is never executed.
     * 在 HEAD 处拦截 {@code readAll()} 并完全替换为分块实现，
     * 原版二次方循环不会被执行。
     */
    @Inject(method = "readAll", at = @At("HEAD"), cancellable = true, remap = false)
    private void bugfwxer$readAllChunked(CallbackInfoReturnable<SoundBuffer> cir) {
        cir.setReturnValue(bugfwxer$decodeChunked());
    }

    /**
     * Chunked replacement for {@code CodecJOrbis#readAll()}: loops {@code read()}
     * (each call accumulates at most the configured streaming-buffer size) and
     * joins the chunks once, turning the quadratic copy storm into a single
     * pass. Returns {@code null} when no data could be decoded, like the
     * original.  {@code readAll()} 的分块替代实现：循环调用 {@code read()}
     * （每次最多累积到配置的流式缓冲大小）后一次性拼接，把二次方复制风暴
     * 降为单次复制。与原实现一致，解码不到数据时返回 {@code null}。
     */
    private SoundBuffer bugfwxer$decodeChunked() {
        final List<byte[]> chunks = new ArrayList<byte[]>();
        final AudioFormat format = this.getAudioFormat();
        final int maxBytes = bugfwxer$frameAlignedLimit(
                SoundSystemConfig.getMaxFileSize(),
                format == null ? 1 : format.getFrameSize());
        int total = 0;
        boolean truncated = false;

        while (!this.endOfStream() && total < maxBytes) {
            final SoundBuffer chunk = this.read();
            if (chunk == null || chunk.audioData == null || chunk.audioData.length == 0) {
                break;
            }
            final int keep = Math.min(chunk.audioData.length, maxBytes - total);
            chunks.add(keep == chunk.audioData.length
                    ? chunk.audioData
                    : Arrays.copyOf(chunk.audioData, keep));
            total += keep;
            if (keep < chunk.audioData.length) {
                truncated = true;
                break;
            }
        }
        truncated |= !this.endOfStream();
        if (truncated) {
            BugFwxer.logger.warn("Truncated OGG sound at the configured decoded size limit of "
                    + maxBytes + " bytes");
        }
        if (chunks.isEmpty()) {
            return null;
        }
        if (chunks.size() == 1) {
            return new SoundBuffer(chunks.get(0), format);
        }

        final byte[] all = new byte[total];
        int offset = 0;
        for (byte[] chunk : chunks) {
            System.arraycopy(chunk, 0, all, offset, chunk.length);
            offset += chunk.length;
        }
        return new SoundBuffer(all, format);
    }

    /**
     * Rounds the byte limit down to a whole number of frames, so the decoded
     * data never ends in the middle of a sample. 把字节上限向下对齐到整帧，
     * 避免解码数据在采样中间被截断。
     */
    private static int bugfwxer$frameAlignedLimit(int maxBytes, int frameSize) {
        final int alignment = Math.max(frameSize, 1);
        final int limit = maxBytes <= 0 ? Integer.MAX_VALUE : maxBytes;
        return limit - limit % alignment;
    }
}
