package decok.dfcdvadstf.bugfwxer.mixins.early.minecraft.world.gen;

import net.minecraft.world.gen.ChunkProviderFlat;
import net.minecraft.world.gen.MapGenBase;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.InitMapGenEvent;
import net.minecraftforge.event.terraingen.InitMapGenEvent.EventType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * <p>
 * Make superflat worlds fire Forge's {@code InitMapGenEvent} for village
 * generation, exactly like regular worlds do.<br>
 * 让超平坦世界像普通世界一样，为村庄生成触发 Forge 的 {@code InitMapGenEvent} 事件。
 * </p>
 * <p>
 * Vanilla {@link ChunkProviderFlat} adds a plain {@link MapGenVillage} to its
 * structure list without posting the event, so mods that replace the village
 * generator through {@code InitMapGenEvent} (e.g. VillageNames) never get a
 * chance to take over. This mixin redirects the first {@code List.add} call in
 * the constructor — the one that appends the village generator — and posts the
 * event there, keeping whatever generator the bus hands back. When the flat
 * preset declares no {@code village} feature, the vanilla behaviour is left
 * untouched.<br>
 * 原版 {@link ChunkProviderFlat} 将普通 {@link MapGenVillage} 加入结构列表时
 * 不会发布该事件，因此通过 {@code InitMapGenEvent} 替换村庄生成器的模组
 * （如 VillageNames）永远没有机会接管。本 Mixin 重定向构造函数中的第一个
 * {@code List.add} 调用——即追加村庄生成器的那一次——并在那里发布事件，
 * 将事件总线返回的生成器加入列表。当平坦预设没有声明 {@code village} 特性时，
 * 保持原版行为不变。
 * </p>
 * <p>
 * Note: Forge's {@code InitMapGenEvent} constructor is package-private, so the
 * event is created via reflection; this happens once per world creation.<br>
 * 注意：Forge 的 {@code InitMapGenEvent} 构造器是包私有的，因此事件通过反射
 * 创建；该操作每个世界创建时仅发生一次。
 * </p>
 *
 * @author Seniye
 */
@Mixin(ChunkProviderFlat.class)
public abstract class MixinChunkProviderFlat {

    /**
     * Fire the village {@code InitMapGenEvent} right where the vanilla village
     * generator is appended, and add the replacement (if any) instead.<br>
     * 在原版村庄生成器被加入列表处发布村庄 {@code InitMapGenEvent} 事件，
     * 并将替换后的生成器（如果有）加入列表。
     *
     * @param structureGenerators the {@code structureGenerators} list; 结构生成器列表
     * @param generator           the generator being added; 正在加入的生成器
     * @return the result of {@code List.add}; {@code List.add} 的结果
     */
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 0))
    @SuppressWarnings({"rawtypes", "unchecked"}) // The target field is a raw List; handler must mirror its signature. 目标字段是原始 List，handler 签名必须与之对应。
    private boolean bugfwxer$fireVillageInitMapGenEvent(List structureGenerators, Object generator) {
        if (!(generator instanceof MapGenVillage)) {
            return structureGenerators.add(generator);
        }

        // Simulate ChunkProviderGenerate.initMapGen: post the event with the
        // vanilla generator as the original, then honour the bus's answer
        // 模拟 ChunkProviderGenerate.initMapGen：以原版生成器作为原始对象发布事件，
        // 然后采用事件总线的返回值
        InitMapGenEvent event = this.bugfwxer$newInitMapGenEvent(EventType.VILLAGE, (MapGenVillage) generator);
        if (event == null) {
            return structureGenerators.add(generator);
        }
        MinecraftForge.TERRAIN_GEN_BUS.post(event);
        MapGenBase replacement = event.newGen != null ? event.newGen : (MapGenVillage) generator;
        return structureGenerators.add(replacement);
    }

    /**
     * Create an {@link InitMapGenEvent} via reflection because its constructor
     * is package-private; returns {@code null} if creation fails.<br>
     * 由于 {@link InitMapGenEvent} 的构造器是包私有的，通过反射创建事件；
     * 创建失败时返回 {@code null}。
     */
    private InitMapGenEvent bugfwxer$newInitMapGenEvent(EventType type, MapGenBase original) {
        try {
            Constructor<InitMapGenEvent> constructor = InitMapGenEvent.class
                    .getDeclaredConstructor(EventType.class, MapGenBase.class);
            constructor.setAccessible(true);
            return constructor.newInstance(type, original);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }
}
