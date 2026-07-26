package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.block;

import net.minecraft.block.BlockTallGrass;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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
     *     因此在此处让 IGrowable 的判定方法 {@code func_149851_a} 直接返回 false。
     * </p>
     * @author Seniye
     */
    @Inject(
        method = "func_149851_a",
        at = @At("HEAD"),
        cancellable = true
    )
    private void denyGrowAtHeightLimit(World world, int x, int y, int z, boolean isClient, CallbackInfoReturnable<Boolean> cir) {
        // 动态获取世界高度限制，兼容修改世界高度的模组
        // Dynamically read the world height limit, compatible with world-height-modifying mods
        if (y >= world.getHeight() - 1) {
            // 双层植物的上半部分会超出高度上限，判定为不可生长
            // The upper half of the double plant would exceed the height limit, so it is not growable
            cir.setReturnValue(false);
        }
    }
}
