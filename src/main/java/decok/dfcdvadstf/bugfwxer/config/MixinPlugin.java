package decok.dfcdvadstf.bugfwxer.config;

import org.spongepowered.asm.lib.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 *     Mixin config plugin that gates every mixin behind its config toggle.<br>
 *     通过配置开关控制每个 Mixin 是否应用的 Mixin 配置插件。
 * </p>
 * <p>
 *     {@link #onLoad(String)} runs before any target class is transformed, so
 *     disabled fixes are never injected at all — no runtime flag checks needed
 *     inside the mixins themselves.<br>
 *     {@link #onLoad(String)} 在任何目标类被转换之前执行，被禁用的修复根本不会注入，
 *     Mixin 内部也就无需任何运行时开关判断。
 * </p>
 *
 * @author Seniye
 */
public class MixinPlugin implements IMixinConfigPlugin {

    /**
     * Maps each mixin class name to whether its fix is enabled.
     * 记录每个 Mixin 类名对应的修复是否启用。
     */
    private final Map<String, Boolean> mixinToggles = new HashMap<String, Boolean>();

    @Override
    public void onLoad(String mixinPackage) {
        // Load config/bugfwxer.cfg before any mixin gets applied
        // 在任何 Mixin 应用之前加载 config/bugfwxer.cfg
        BugFwxerConfig.load();

        String prefix = mixinPackage + ".";
        mixinToggles.put(prefix + "middle.minecraft.block.MixinBlockSapling", BugFwxerConfig.fixDarkOakSaplingBonemeal);
        // The height-limit fix spans two mixins that must toggle together
        // 高度上限修复由两个 Mixin 共同组成，必须一起开关
        mixinToggles.put(prefix + "middle.minecraft.block.MixinBlockTallGrass", BugFwxerConfig.fixBonemealHeightLimit);
        mixinToggles.put(prefix + "middle.minecraft.item.MixinItemDye", BugFwxerConfig.fixBonemealHeightLimit);
        mixinToggles.put(prefix + "middle.minecraft.entity.MixinEntityItem", BugFwxerConfig.fixItemPositionDesync);
        mixinToggles.put(prefix + "middle.minecraft.item.MixinItemMonsterPlacer", BugFwxerConfig.peacefulMonsterEggRestriction);
        mixinToggles.put(prefix + "middle.minecraft.item.MixinItemStack", BugFwxerConfig.fixItemStackHasEffectNpe);
        mixinToggles.put(prefix + "middle.minecraft.entity.MixinEntityBat", BugFwxerConfig.fixBatWingAnimationOverflow);
        mixinToggles.put(prefix + "middle.minecraft.gui.MixinGuiSlot", BugFwxerConfig.fixGuiSlotNegativeScroll);
        mixinToggles.put(prefix + "middle.minecraft.client.MixinMinecraft", BugFwxerConfig.blockAltF4WindowClose);
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        Boolean enabled = mixinToggles.get(mixinClassName);
        // Mixins without a registered toggle (future additions) default to enabled
        // 未登记开关的 Mixin（未来新增项）默认启用
        return enabled == null || enabled;
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
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }
}
