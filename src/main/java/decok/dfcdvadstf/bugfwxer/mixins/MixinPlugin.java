package decok.dfcdvadstf.bugfwxer.mixins;

import com.gtnewhorizon.gtnhmixins.builders.IMixins;
import decok.dfcdvadstf.bugfwxer.BugFwxerConfig;
import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * <p>
 * Mixin config plugin of the main config. It loads config/bugfwxer.cfg before
 * any target class is transformed and supplies the default-phase mixin list
 * from the GTNH {@link Mixins} enum.<br>
 * 主配置的 Mixin 配置插件。在任何目标类被转换之前加载 config/bugfwxer.cfg，
 * 并从 GTNH {@link Mixins} 枚举提供默认阶段的 Mixin 列表。
 * </p>
 * <p>
 * Gating is declared once in {@link Mixins} via {@code setApplyIf} — the
 * disabled fixes are never handed out here, so no runtime flag checks are
 * needed inside the mixins themselves.<br>
 * 开关统一在 {@link Mixins} 中通过 {@code setApplyIf} 声明——被禁用的修复
 * 根本不会从这里分发，Mixin 内部无需任何运行时开关判断。
 * </p>
 *
 * @author Seniye
 */
public class MixinPlugin implements IMixinConfigPlugin {

    @Override
    public void onLoad(String mixinPackage) {
        // Load config/bugfwxer.cfg before any mixin gets applied
        // 在任何 Mixin 应用之前加载 config/bugfwxer.cfg
        BugFwxerConfig.load();
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // Gating is handled by Mixins.setApplyIf; everything handed out here is already enabled
        // 开关由 Mixins.setApplyIf 统一处理；这里分发的 Mixin 均已启用
        return true;
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        // Default-phase mixins (phase == null in the enum), already filtered by side and toggles
        // 默认阶段的 Mixin（枚举中 phase 为 null），已按 side 与开关过滤
        return IMixins.getMixins(Mixins.class);
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
