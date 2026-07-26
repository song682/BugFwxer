package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BlockTallGrass.class)
public abstract class MixinBlockTallGrass {

    /**
     * <p>
     *     Fix tall grass reporting itself as growable at the world height limit.<br>
     *     修复草方块在世界高度上限处仍声称可生长的问题。<br>
     *     Bonemeal turns tall grass into a double plant occupying (y) and (y+1);
     *     at y >= maxHeight - 1 the top half would exceed the build limit,
     *     so the IGrowable check {@code func_149851_a} must return false here.<br>
     *     骨粉会把草催成占据 (y) 和 (y+1) 两格的双层植物；
     *     当 y >= maxHeight - 1 时顶部会超出建筑高度上限，
     *     因此在此处让 IGrowable 的判定方法 {@code func_149851_a} 直接返回 false。<br>
     *     Uses MixinExtras {@code @ModifyReturnValue}: the original body still runs,
     *     only the return value is suppressed, so it chains with other mods' handlers.<br>
     *     使用 MixinExtras 的 {@code @ModifyReturnValue}：原方法体照常执行，
     *     仅压制返回值，可与其他模组的处理器链式共存。
     * </p>
     * @author Seniye
     */
    @ModifyReturnValue(
        method = "func_149851_a",
        at = @At("RETURN")
    )
    private boolean denyGrowAtHeightLimit(boolean original, World world, int x, int y, int z, boolean isClient) {
        // 动态获取世界高度限制，兼容修改世界高度的模组
        // Dynamically read the world height limit, compatible with world-height-modifying mods
        if (y >= world.getHeight() - 1) {
            // 双层植物的上半部分会超出高度上限，判定为不可生长
            // The upper half of the double plant would exceed the height limit, so it is not growable
            return false;
        }

        // 否则保留原判定结果
        // Otherwise keep the original result
        return original;
    }
}
