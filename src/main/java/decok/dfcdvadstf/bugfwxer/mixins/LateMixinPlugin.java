package decok.dfcdvadstf.bugfwxer.mixins;

import com.gtnewhorizon.gtnhmixins.ILateMixinLoader;
import com.gtnewhorizon.gtnhmixins.LateMixin;
import com.gtnewhorizon.gtnhmixins.builders.IMixins;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;

/**
 * <p>
 * GTNH late-mixin loader: registered by the UniMixins orchestration mixin
 * during {@code LoaderState.CONSTRUCTING}, right before any mod class is
 * loaded. It supplies the LATE-phase mixin list from the {@link Mixins}
 * enum, which is then written into mixins.bugfwxer.late.json.<br>
 * GTNH 后期 Mixin 加载器：由 UniMixins 编排 Mixin 在
 * {@code LoaderState.CONSTRUCTING} 阶段注册，恰在任意模组类加载之前。
 * 它从 {@link Mixins} 枚举提供 LATE 阶段的 Mixin 列表，
 * 该列表会被写入 mixins.bugfwxer.late.json。
 * </p>
 * <p>
 * No LATE mixins are declared yet — the class is a ready-to-use scaffold for
 * future fixes targeting mod classes.<br>
 * 目前尚未声明任何 LATE Mixin——本类是为将来修复模组类而准备的现成骨架。
 * </p>
 *
 * @author Seniye
 */
@LateMixin
public class LateMixinPlugin implements ILateMixinLoader {
    @Override
    public String getMixinConfig() {
        return "mixins.bugfwxer.late.json";
    }

    @Nonnull
    @Override
    public List<String> getMixins(Set<String> loadedMods) {
        // LATE-phase mixins (mod classes), filtered by side and toggles
        // LATE 阶段的 Mixin（模组类），已按 side 与开关过滤
        return IMixins.getLateMixins(Mixins.class, loadedMods);
    }
}
