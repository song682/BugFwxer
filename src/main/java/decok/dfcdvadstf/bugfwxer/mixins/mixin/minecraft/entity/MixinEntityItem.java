package decok.dfcdvadstf.bugfwxer.mixins.mixin.minecraft.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * <p>
 *     Fix MC-4: item entity position desync between client and server.<br>
 *     修复 MC-4：物品实体在客户端与服务端之间的位置不同步问题。
 * </p>
 * <p>
 *     Root cause / 根本原因：<br>
 *     The server sends item positions to the client as 1/32 fixed-point values
 *     ({@code floor(pos * 32)} in S0EPacketSpawnObject, and EnumEntitySize rounding
 *     in EntityTrackerEntry), while keeping the exact double position for itself.
 *     Both sides then run the same physics in {@code onUpdate()}, so near a block
 *     edge the client's rounded copy may slide off while the server's exact one
 *     stays on. Relative move packets are only sent when the delta reaches 4/32
 *     of a block, so this sub-precision drift is never corrected.<br>
 *     服务端把物品坐标编码成 1/32 定点数发给客户端（S0EPacketSpawnObject 使用
 *     {@code floor(pos * 32)}，EntityTrackerEntry 使用 EnumEntitySize 舍入），
 *     而自己保留精确的 double 坐标。之后两端各自运行同一套 {@code onUpdate()} 物理逻辑，
 *     在方块边缘处，客户端那份被舍入过的坐标可能滑落，而服务端的精确坐标仍停留在边上；
 *     又因为相对移动包只有位移达到 4/32 格才会发送，这种亚精度偏差永远无法被纠正。
 * </p>
 * <p>
 *     Fix (by Panda4994, same approach as Paper's {@code fix-entity-position-desync}) /
 *     修复方案（来自 Panda4994，与 Paper 的 {@code fix-entity-position-desync} 补丁同思路）：<br>
 *     Truncate the item's actual position to the network precision grid (1/32 in
 *     1.7.10, 1/4096 in modern versions). On-grid values survive every packet
 *     encoding bit-exactly, so the client decodes exactly the server's position
 *     and both physics simulations stay in lockstep.<br>
 *     把物品实体的真实坐标截断到网络精度格点上（1.7.10 为 1/32，现代版本为 1/4096）。
 *     格点上的坐标经过任何发包编码都不会损失精度，客户端解码得到的坐标与服务端完全一致，
 *     两端的物理模拟从而保持同步。
 * </p>
 *
 * @author Seniye
 * @see net.minecraft.network.play.server.S0EPacketSpawnObject
 * @see net.minecraft.entity.EntityTrackerEntry
 */
@Mixin(EntityItem.class)
public abstract class MixinEntityItem extends Entity {

    public MixinEntityItem(World world) {
        super(world);
    }

    /**
     * <p>
     *     Quantize every explicit position write (constructor, teleport via
     *     {@code setLocationAndAngles}, etc.) to the network grid, so commands like
     *     {@code /tp @e[type=item] ~ ~1 ~-0.6249} can no longer leave the server
     *     position off-grid.<br>
     *     对所有显式坐标写入（构造器、经 {@code setLocationAndAngles} 的传送等）做格点对齐，
     *     使 {@code /tp @e[type=item] ~ ~1 ~-0.6249} 这类命令无法再把服务端坐标留在格点之外。
     * </p>
     */
    @Override
    public void setPosition(double x, double y, double z) {
        super.setPosition(
            quantizeToNetworkGrid(x),
            quantizeToNetworkGrid(y),
            quantizeToNetworkGrid(z)
        );
    }

    /**
     * <p>
     *     Re-align the position after each physics tick, since {@code moveEntity()}
     *     writes posX/posY/posZ directly without going through {@code setPosition()}.
     *     Runs on both sides so client and server simulate on identical inputs.<br>
     *     每个物理 tick 结束后重新对齐坐标，因为 {@code moveEntity()} 直接写
     *     posX/posY/posZ 字段而不经过 {@code setPosition()}。两端都执行，
     *     保证客户端与服务端在完全相同的输入上做模拟。
     * </p>
     */
    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void fixMC4PositionDesync(CallbackInfo ci) {
        // Goes through the overridden setPosition above, which also refreshes the bounding box
        // 走上面重写过的 setPosition，同时会刷新碰撞箱
        this.setPosition(this.posX, this.posY, this.posZ);
    }

    /**
     * <p>
     *     Truncate a coordinate to the 1/32 fixed-point grid used by the 1.7.10
     *     entity network protocol. Uses {@code floor} to match the encoding in
     *     S0EPacketSpawnObject; on-grid values make EnumEntitySize's floor/ceil
     *     variants agree as well ({@code pos * 32} is then an integer).<br>
     *     将坐标截断到 1.7.10 实体网络协议使用的 1/32 定点格。采用 {@code floor}
     *     与 S0EPacketSpawnObject 的编码保持一致；坐标落在格点上后
     *     {@code pos * 32} 为整数，EnumEntitySize 的 floor/ceil 两种舍入结果也随之相同。
     * </p>
     */
    private static double quantizeToNetworkGrid(double value) {
        return MathHelper.floor_double(value * 32.0D) / 32.0D;
    }
}
