package decok.dfcdvadstf.bugfwxer.mixins.middle.java;

import decok.dfcdvadstf.bugfwxer.api.BugFwxerSoundApi;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import paulscode.sound.Channel;
import paulscode.sound.Library;

import java.util.List;

/**
 * <p>
 * MixinLibraryChannelUsage<br>
 * Bridges paulscode's {@code Library} channel lists into the public
 * {@link BugFwxerSoundApi}, so external mods can display how many of the
 * normal/streaming channels are currently in use and playing.<br>
 * 将 paulscode {@code Library} 的声道列表桥接到公开的
 * {@link BugFwxerSoundApi}，使外部模组能显示当前普通/流式声道的
 * 占用数与播放中数。
 * </p>
 * <p>
 * {@code Library#init()} creates the normal and streaming channel lists and
 * is the last step of sound library initialization; every backend either
 * inherits it or (like {@code LibraryJavaSound}) calls {@code super.init()}
 * at the end, so this TAIL injection always sees fully built lists. The lists
 * are stable for the lifetime of the library (recreated on re-init), so
 * registering the references once per init is enough.<br>
 * {@code Library#init()} 创建普通与流式声道列表，是声音库初始化的最后一步；
 * 所有后端要么继承它，要么（如 {@code LibraryJavaSound}）在末尾调用
 * {@code super.init()}，因此本 TAIL 注入总能拿到已建好的列表。
 * 列表在声音库生命周期内保持稳定（重新初始化时重建），
 * 所以每次 init 注册一次引用即可。
 * </p>
 *
 * @author Seniye
 */
@Mixin(value = Library.class, remap = false)
public abstract class MixinLibraryChannelUsage {

    /** Streaming channel list of the target Library. 目标 Library 的流式声道列表。 */
    @Shadow
    protected List<Channel> streamingChannels;

    /** Normal (non-streaming) channel list of the target Library. 目标 Library 的普通声道列表。 */
    @Shadow
    protected List<Channel> normalChannels;

    /**
     * Registers the freshly created channel lists into
     * {@link BugFwxerSoundApi} right after {@code Library#init()} returns.<br>
     * 在 {@code Library#init()} 返回后立即把刚创建好的声道列表
     * 注册进 {@link BugFwxerSoundApi}。
     */
    @Inject(method = "init", at = @At("TAIL"), remap = false)
    private void bugfwxer$registerChannelLists(CallbackInfo ci) {
        BugFwxerSoundApi.registerChannels(normalChannels, streamingChannels);
    }
}
