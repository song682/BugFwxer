package decok.dfcdvadstf.bugfwxer.mixins.middle.minecraft.client.gui;

import java.util.Random;
import net.minecraft.client.gui.FontRenderer;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

/**
 * MixinFontRenderer
 * Fixes logic errors in the 1.7.10 {@link FontRenderer} by aligning its
 * behavior
 * with the modern Font / StringSplitter implementation:
 * <ul>
 * <li>sizeStringToWidth: an overflowing first character no longer returns 0,
 * which used to make wrapFormattedStringToWidth recurse infinitely and crash
 * with a StackOverflowError.</li>
 * <li>getCharWidth: the unicode width now exactly matches the advance actually
 * used by rendering, so measuring, wrapping and trimming all agree with the
 * drawn result.</li>
 * <li>getStringWidth: a lone trailing formatting code (&sect;) no longer
 * subtracts one pixel from the width.</li>
 * <li>renderStringAtPos: the obfuscated (&sect;k) style picks a random
 * same-width glyph in bounded time instead of hanging forever when the
 * character's width is unique, and spaces are never randomized.</li>
 * </ul>
 * 修复 1.7.10 FontRenderer 的逻辑错误，使其与现代版 Font / StringSplitter 行为一致：
 * <ul>
 * <li>sizeStringToWidth：超宽的首字符不再返回 0，避免 wrapFormattedStringToWidth
 * 无限递归并 StackOverflowError 崩溃。</li>
 * <li>getCharWidth：unicode 宽度与渲染实际推进宽度完全一致，测量、换行、截断
 * 全部与实际绘制结果吻合。</li>
 * <li>getStringWidth：末尾孤立的格式码（&sect;）不再使宽度减少 1px。</li>
 * <li>renderStringAtPos：乱码（&sect;k）样式在有限时间内随机挑选同宽字形，
 * 字符宽度唯一时不再永久卡死，且空格不参与随机化。</li>
 * </ul>
 *
 * @author Seniye
 */
@Mixin(FontRenderer.class)
public abstract class MixinFontRenderer {

    /**
     * Width of every glyph in default.png, indexed by the glyph charset.
     * 默认字体每个字形的宽度。
     */
    @Shadow
    protected int[] charWidth;

    /**
     * Per-glyph (start/end column nibbles) widths for the unicode font. unicode
     * 字体每个字形的起止列。
     */
    @Shadow
    protected byte[] glyphWidth;

    @Shadow
    private boolean unicodeFlag;

    @Shadow
    private boolean randomStyle;

    @Shadow
    private boolean boldStyle;

    @Shadow
    private boolean italicStyle;

    @Shadow
    private boolean underlineStyle;

    @Shadow
    private boolean strikethroughStyle;

    @Shadow
    private int textColor;

    @Shadow
    private int[] colorCode;

    @Shadow
    private float red;

    @Shadow
    private float blue;

    @Shadow
    private float green;

    @Shadow
    private float alpha;

    @Shadow
    protected float posX;

    @Shadow
    protected float posY;

    @Shadow
    public Random fontRandom;

    @Shadow
    public int FONT_HEIGHT;

    @Shadow
    private float renderCharAtPos(int p_78278_1_, char p_78278_2_, boolean p_78278_3_) {
        throw new UnsupportedOperationException();
    }

    @Shadow(remap = false)
    protected abstract void doDraw(float f);

    @Shadow(remap = false)
    protected abstract void setColor(float r, float g, float b, float a);

    @Shadow
    private static boolean isFormatColor(char p_78272_0_) {
        throw new UnsupportedOperationException();
    }

    @Shadow
    private void loadGlyphTexture(int p_78257_1_) {
        throw new UnsupportedOperationException();
    }

    /**
     * @author Seniye
     * @reason Trailing lone formatting code must consume zero width, matching
     *         modern Font#width(String)
     *         FontMetrics.stringWidth(String s).
     *         返回字符串渲染宽度，等价于 FontMetrics.stringWidth(String s)。
     *         <p>
     *         Fix: a lone trailing formatting code (&sect;) used to contribute -1
     *         px to the
     *         width. Formatting codes are invisible and must always consume 0
     *         width,
     *         exactly
     *         as in the modern Font#width(String) (StringSplitter)
     *         implementation.<br>
     *         修复：末尾孤立的格式码（&sect;）此前会使宽度减少 1px。格式码不可见，始终应占
     *         0 宽度，与现代版 Font#width(String)（StringSplitter）一致。
     *         </p>
     */
    @Overwrite
    public int getStringWidth(String p_78256_1_) {
        if (p_78256_1_ == null) {
            return 0;
        } else {
            int i = 0;
            boolean flag = false;

            for (int j = 0; j < p_78256_1_.length(); ++j) {
                char c0 = p_78256_1_.charAt(j);
                int k = this.getCharWidth(c0);

                if (k < 0) {
                    // A formatting code: consume the following code char (if any) and track bold
                    // state.
                    // 格式码：消费其后的格式字符（若有），并跟踪粗体状态。
                    if (j < p_78256_1_.length() - 1) {
                        ++j;
                        c0 = p_78256_1_.charAt(j);

                        if (c0 != 108 && c0 != 76) {
                            if (c0 == 114 || c0 == 82) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }

                    // Formatting codes never consume width, including a trailing lone §.
                    // 格式码不占宽度，包括末尾孤立的 §。
                    k = 0;
                }

                i += k;

                if (flag && k > 0) {
                    ++i;
                }
            }

            return i;
        }
    }

    /**
     * @author Seniye
     * @reason Unicode width must equal the actual rendered advance
     *         (k - j + 1) / 2 + 1
     *         Returns the width of this character as rendered.
     *         返回该字符实际渲染时的推进宽度。
     *         <p>
     *         Fix: the unicode branch special-cased glyphs wider than half a cell
     *         ({@code k > 7}) to a fixed full-cell width of 8, which never matched
     *         the real
     *         advance of renderUnicodeChar (e.g. measured 8 while only 5 was
     *         actually
     *         advanced), breaking centering and wrapping for such glyphs. The
     *         measurement
     *         now uses the exact advance used by rendering,
     *         {@code (k - j + 1) / 2 + 1},
     *         mirroring the modern Font where width() is derived from the very
     *         advances
     *         used for drawing.<br>
     *         修复：unicode 分支对超过半格的字形（k &gt; 7）特判为固定整格宽度 8，与
     *         renderUnicodeChar 的实际推进宽度不符（例如测量 8 而实际只推进 5），导致这类
     *         字形居中对齐与换行错位。现改为直接使用渲染推进宽度 (k - j + 1) / 2 + 1，
     *         对应现代版 Font 由绘制所用 advance 推导 width() 的做法。
     *         <p>
     *         Fix: the glyphWidth start nibble was read through a sign-extended
     *         {@code >>> 4}; glyphs whose byte value is &ge; 0x80 (narrow full-width
     *         punctuation such as U+FF08 （) yielded a huge start column and a
     *         negative width, so they were measured like a formatting code. Masking
     *         with {@code & 0xFF} restores the intended 0..15 nibble.<br>
     *         修复：glyphWidth 起始列的 {@code >>> 4} 读取存在符号扩展问题，字节值
     *         &ge; 0x80 的窄全角标点（如 U+FF08 （）会被解析为巨大的起始列与负宽度，
     *         在测量时被当作格式码处理。以 {@code & 0xFF} 屏蔽符号位后恢复 0..15 的
     *         正确 nibble 范围。
     *         </p>
     */
    @Overwrite
    public int getCharWidth(char p_78263_1_) {
        if (p_78263_1_ == 167) {
            return -1;
        } else if (p_78263_1_ == 32) {
            return 4;
        } else {
            int i = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"
                    .indexOf(p_78263_1_);

            if (p_78263_1_ > 0 && i != -1 && !this.unicodeFlag) {
                return this.charWidth[i];
            } else if (this.glyphWidth[p_78263_1_] != 0) {
                // The byte[] nibble must be read unsigned: glyphs >= 0x80 (e.g.
                // U+FF08 = 0x9C) would otherwise sign-extend into a ~268M start
                // column and a negative width (see @reason).
                // byte[] nibble 必须按无符号读取：>= 0x80 的字形（如 U+FF08 = 0x9C）
                // 会被符号扩展成约 2.68 亿的起始列与负宽度（见 @reason）。
                int j = (this.glyphWidth[p_78263_1_] & 255) >>> 4;
                int k = this.glyphWidth[p_78263_1_] & 15;
                ++k;
                // Matches the truncated advance of renderUnicodeChar: (int)((k + 1 - j) / 2.0F
                // + 1.0F)
                // 与 renderUnicodeChar 截断后的推进宽度一致：(int)((k + 1 - j) / 2.0F + 1.0F)
                return (k - j) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * @author Seniye
     * @reason Unsigned glyphWidth nibble read makes narrow full-width
     *         punctuation (（）｛｝｜) render instead of vanishing and pushing
     *         the rest of the line off-screen
     *         Render a single Unicode character at current (posX,posY) location
     *         using one of the /font/unicode_page_XX.png files.
     *         在当前 (posX,posY) 位置使用 unicode_page_XX.png 渲染单个 Unicode 字符。
     *         <p>
     *         Fix: glyphWidth is a {@code byte[]}, so any glyph whose byte value
     *         is &ge; 0x80 (a narrow glyph starting past column 8, e.g.
     *         U+FF08 = 0x9C) was sign-extended before {@code >>> 4}, producing a
     *         start column of 268435449. The glyph was then sampled from a
     *         garbage texture coordinate (invisible), the quad spanned
     *         -134217721 px, and the returned advance {@code (k - j) / 2 + 1}
     *         was -134217717 px, rendering every following character far
     *         off-screen. Reading the nibble unsigned ({@code & 0xFF}) restores
     *         the intended 0..15 range.<br>
     *         修复：glyphWidth 是 {@code byte[]}，字节值 &ge; 0x80 的字形（第 8 列
     *         之后才起笔的窄字形，如 U+FF08 = 0x9C）在 {@code >>> 4} 前被符号扩展，
     *         起始列变成 268435449：字形采样到错误的纹理坐标（不可见），四边形宽度为
     *         -134217721px，且返回的推进宽度 {@code (k - j) / 2 + 1} 为
     *         -134217717px，其后所有字符都被渲染到屏幕之外。按无符号
     *         （{@code & 0xFF}）读取 nibble 即可恢复 0..15 的正确范围。
     *         </p>
     */
    @Overwrite
    protected float renderUnicodeChar(char p_78277_1_, boolean p_78277_2_) {
        if (this.glyphWidth[p_78277_1_] == 0) {
            return 0.0F;
        } else {
            int i = p_78277_1_ / 256;
            this.loadGlyphTexture(i);
            // The byte[] nibble must be read unsigned: glyphs >= 0x80 would
            // otherwise sign-extend into a ~268M start column (see @reason).
            // byte[] nibble 必须按无符号读取：>= 0x80 的字形会被符号扩展成约 2.68 亿的起始列。
            int j = (this.glyphWidth[p_78277_1_] & 255) >>> 4;
            int k = this.glyphWidth[p_78277_1_] & 15;
            float f = (float) j;
            float f1 = (float) (k + 1);
            float f2 = (float) (p_78277_1_ % 16 * 16) + f;
            float f3 = (float) ((p_78277_1_ & 255) / 16 * 16);
            float f4 = f1 - f - 0.02F;
            float f5 = p_78277_2_ ? 1.0F : 0.0F;
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);
            GL11.glTexCoord2f(f2 / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f5, this.posY, 0.0F);
            GL11.glTexCoord2f(f2 / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX - f5, this.posY + 7.99F, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, f3 / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F + f5, this.posY, 0.0F);
            GL11.glTexCoord2f((f2 + f4) / 256.0F, (f3 + 15.98F) / 256.0F);
            GL11.glVertex3f(this.posX + f4 / 2.0F - f5, this.posY + 7.99F, 0.0F);
            GL11.glEnd();
            return (f1 - f) / 2.0F + 1.0F;
        }
    }

    /**
     * @author Seniye
     * @reason An overflowing first character must still occupy one line to
     *         prevent infinite recursion
     *         Determines how many characters from the string will fit into the
     *         specified
     *         width.
     *         计算字符串中多少个字符能放入指定宽度。
     *         <p>
     *         Fix: when even the first character alone exceeds the width, this
     *         method used
     *         to
     *         return 0, which made wrapFormattedStringToWidth recurse on the very
     *         same
     *         string
     *         forever and crash with a StackOverflowError. A non-empty line now
     *         always
     *         contains at least one character, matching the modern StringSplitter
     *         LineBreakFinder which unconditionally accepts the first character
     *         (its
     *         maxWidth is also clamped to at least 1).<br>
     *         修复：当仅第一个字符就超出宽度时，该方法此前返回 0，导致
     *         wrapFormattedStringToWidth 对同一字符串无限递归并 StackOverflowError 崩溃。
     *         现在非空字符串的每一行至少包含一个字符，与现代版 StringSplitter 的
     *         LineBreakFinder（无条件接受首个字符，maxWidth 至少为 1）行为一致。
     *         </p>
     */
    @Overwrite
    private int sizeStringToWidth(String p_78259_1_, int p_78259_2_) {
        int j = p_78259_1_.length();
        int k = 0;
        int l = 0;
        int i1 = -1;

        for (boolean flag = false; l < j; ++l) {
            char c0 = p_78259_1_.charAt(l);

            switch (c0) {
                case 10:
                    --l;
                    break;
                case 167:
                    if (l < j - 1) {
                        ++l;
                        char c1 = p_78259_1_.charAt(l);

                        if (c1 != 108 && c1 != 76) {
                            if (c1 == 114 || c1 == 82 || isFormatColor(c1)) {
                                flag = false;
                            }
                        } else {
                            flag = true;
                        }
                    }

                    break;
                case 32:
                    i1 = l; /* fall through */
                default:
                    k += this.getCharWidth(c0);

                    if (flag) {
                        ++k;
                    }
            }

            if (c0 == 10) {
                ++l;
                i1 = l;
                break;
            }

            if (k > p_78259_2_) {
                break;
            }
        }

        // Guarantee progress: an overflowing first character must still occupy a line,
        // otherwise wrapFormattedStringToWidth would recurse infinitely.
        // 保证推进：超宽的首字符也必须占一行，否则 wrapFormattedStringToWidth 将无限递归。
        if (l == 0 && i1 == -1 && j > 0) {
            return 1;
        }

        return l != j && i1 != -1 && i1 < l ? i1 : l;
    }

    /**
     * @author Seniye
     * @reason Obfuscated style random pick must terminate and must not randomize
     *         spaces
     *         Render a single line string at the current (posX,posY) and update
     *         posX.
     *         在当前 (posX,posY) 渲染单行字符串并更新 posX。
     *         <p>
     *         Fix: the obfuscated (&sect;k) style picked a random same-width
     *         character in
     *         an
     *         unbounded do-while loop that hangs the game forever when the current
     *         character's width is unique among all glyphs. The pick now uses
     *         bounded
     *         reservoir sampling over all same-width glyphs (the original glyph is
     *         always
     *         a member, so it always terminates), and spaces are never randomized,
     *         matching the modern Font#getGlyph / FontSet#getRandomGlyph
     *         behavior.<br>
     *         修复：乱码（&sect;k）样式此前在无界 do-while 循环中挑选同宽随机字符，当当前
     *         字符宽度在所有字形中唯一时游戏会永久卡死。现改为在同宽字形集合上做有界的
     *         蓄水池采样（原字形必然在候选集中，因此必定终止），且空格不再参与随机化，
     *         对应现代版 Font#getGlyph / FontSet#getRandomGlyph 行为。
     *         </p>
     */
    @Overwrite
    private void renderStringAtPos(String p_78255_1_, boolean p_78255_2_) {
        for (int i = 0; i < p_78255_1_.length(); ++i) {
            char c0 = p_78255_1_.charAt(i);
            int j;
            int k;

            if (c0 == 167 && i + 1 < p_78255_1_.length()) {
                j = "0123456789abcdefklmnor".indexOf(p_78255_1_.toLowerCase().charAt(i + 1));

                if (j < 16) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;

                    if (j < 0 || j > 15) {
                        j = 15;
                    }

                    if (p_78255_2_) {
                        j += 16;
                    }

                    k = this.colorCode[j];
                    this.textColor = k;
                    setColor((float) (k >> 16) / 255.0F, (float) (k >> 8 & 255) / 255.0F, (float) (k & 255) / 255.0F,
                            this.alpha);
                } else if (j == 16) {
                    this.randomStyle = true;
                } else if (j == 17) {
                    this.boldStyle = true;
                } else if (j == 18) {
                    this.strikethroughStyle = true;
                } else if (j == 19) {
                    this.underlineStyle = true;
                } else if (j == 20) {
                    this.italicStyle = true;
                } else if (j == 21) {
                    this.randomStyle = false;
                    this.boldStyle = false;
                    this.strikethroughStyle = false;
                    this.underlineStyle = false;
                    this.italicStyle = false;
                    setColor(this.red, this.blue, this.green, this.alpha);
                }

                ++i;
            } else {
                j = "\u00c0\u00c1\u00c2\u00c8\u00ca\u00cb\u00cd\u00d3\u00d4\u00d5\u00da\u00df\u00e3\u00f5\u011f\u0130\u0131\u0152\u0153\u015e\u015f\u0174\u0175\u017e\u0207\u0000\u0000\u0000\u0000\u0000\u0000\u0000 !\"#$%&\'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~\u0000\u00c7\u00fc\u00e9\u00e2\u00e4\u00e0\u00e5\u00e7\u00ea\u00eb\u00e8\u00ef\u00ee\u00ec\u00c4\u00c5\u00c9\u00e6\u00c6\u00f4\u00f6\u00f2\u00fb\u00f9\u00ff\u00d6\u00dc\u00f8\u00a3\u00d8\u00d7\u0192\u00e1\u00ed\u00f3\u00fa\u00f1\u00d1\u00aa\u00ba\u00bf\u00ae\u00ac\u00bd\u00bc\u00a1\u00ab\u00bb\u2591\u2592\u2593\u2502\u2524\u2561\u2562\u2556\u2555\u2563\u2551\u2557\u255d\u255c\u255b\u2510\u2514\u2534\u252c\u251c\u2500\u253c\u255e\u255f\u255a\u2554\u2569\u2566\u2560\u2550\u256c\u2567\u2568\u2564\u2565\u2559\u2558\u2552\u2553\u256b\u256a\u2518\u250c\u2588\u2584\u258c\u2590\u2580\u03b1\u03b2\u0393\u03c0\u03a3\u03c3\u03bc\u03c4\u03a6\u0398\u03a9\u03b4\u221e\u2205\u2208\u2229\u2261\u00b1\u2265\u2264\u2320\u2321\u00f7\u2248\u00b0\u2219\u00b7\u221a\u207f\u00b2\u25a0\u0000"
                        .indexOf(c0);

                if (this.randomStyle && c0 != 32 && j != -1) {
                    // Uniform random pick among all same-width glyphs (reservoir sampling).
                    // The original glyph is always a member, so this can never hang —
                    // unlike the vanilla unbounded do-while loop when the width is unique.
                    // 在同宽字形中均匀随机挑选（蓄水池采样）。原字形必然在候选集中，
                    // 因此绝不会像原版无界 do-while 那样在宽度唯一时卡死。
                    int i2 = 0;
                    int k2 = j;

                    for (int j2 = 0; j2 < this.charWidth.length; ++j2) {
                        if (this.charWidth[j] == this.charWidth[j2] && this.fontRandom.nextInt(++i2) == 0) {
                            k2 = j2;
                        }
                    }

                    j = k2;
                }

                float f1 = this.unicodeFlag ? 0.5F : 1.0F;
                boolean flag1 = (c0 == 0 || j == -1 || this.unicodeFlag) && p_78255_2_;

                if (flag1) {
                    this.posX -= f1;
                    this.posY -= f1;
                }

                float f = this.renderCharAtPos(j, c0, this.italicStyle);

                if (flag1) {
                    this.posX += f1;
                    this.posY += f1;
                }

                if (this.boldStyle) {
                    this.posX += f1;

                    if (flag1) {
                        this.posX -= f1;
                        this.posY -= f1;
                    }

                    this.renderCharAtPos(j, c0, this.italicStyle);
                    this.posX -= f1;

                    if (flag1) {
                        this.posX += f1;
                        this.posY += f1;
                    }

                    ++f;
                }

                doDraw(f);
            }
        }
    }
}
