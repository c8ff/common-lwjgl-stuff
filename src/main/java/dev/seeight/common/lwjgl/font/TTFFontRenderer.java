package dev.seeight.common.lwjgl.font;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.util.IOUtil;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL33;
import org.lwjgl.stb.STBTTAlignedQuad;
import org.lwjgl.stb.STBTTBakedChar;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

public class TTFFontRenderer implements IFontRenderer {
	private final Renderer renderer;
	private final Texture texture;
	private final Map<Integer, CharacterData> dataMap;
	private final int fontHeight;

	private float scaleX;
	private float scaleY;

	private static STBTTFontinfo info;

	public static TTFFontRenderer fromTTF(Renderer renderer, InputStream inputStream, int fontHeight) throws IOException {
		return fromTTF(renderer, inputStream, fontHeight, 512, 512, 32, 96);
	}

	public static TTFFontRenderer fromTTF(Renderer renderer, InputStream inputStream, int fontHeight, int textureWidth, int textureHeight) throws IOException {
		return fromTTF(renderer, inputStream, fontHeight, textureWidth, textureHeight, 32, 96);
	}

	public static TTFFontRenderer fromTTF(Renderer renderer, InputStream inputStream, int fontHeight, int textureWidth, int textureHeight, int firstChar, int characterCount) throws IOException {
		ByteBuffer ttf = IOUtil.byteBufferFrom(inputStream);

		if (info == null) {
			info = STBTTFontinfo.create();
		}
		if (!STBTruetype.stbtt_InitFont(info, ttf)) {
			throw new RuntimeException("failed to init font");
		}

		// Create Texture
		int texID = GL11.glGenTextures();

		// Allocate the character data
		STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(characterCount);

		// TODO: Maybe bake my own texture with support for specific character points.
		// Create bitmap
		ByteBuffer bitmap = BufferUtils.createByteBuffer(textureWidth * textureHeight);
		STBTruetype.stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, textureWidth, textureHeight, firstChar, cdata);

		// Move bitmap into texture
		// Thank you stackoverflow for not giving me the answer right away but waiting for when I've tried anything...
		// Anyway, the 'GL_TEXTURE_SWIZZLE_RGBA' line fixes the font being black.
		// https://stackoverflow.com/a/16950771
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0, GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
		GL11.glTexParameteriv(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA, new int[] {
				GL11.GL_ONE,
				GL11.GL_ONE,
				GL11.GL_ONE,
				GL11.GL_ALPHA,
		});
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

		// TODO: should anything be cleaned here?

		return new TTFFontRenderer(renderer, new GLTexture(texID, textureWidth, textureHeight), cdata, fontHeight);
	}

	public TTFFontRenderer(Renderer renderer, Texture texture, STBTTBakedChar.Buffer cdata, int fontHeight) {
		this.renderer = renderer;
		this.texture = texture;
		this.fontHeight = fontHeight;
		this.dataMap = new HashMap<>();

		this.scaleX = 1;
		this.scaleY = 1;

		try (MemoryStack stack = MemoryStack.stackPush()) {
			STBTTAlignedQuad charInfo = STBTTAlignedQuad.malloc(stack);

			FloatBuffer x = stack.mallocFloat(1);
			FloatBuffer y = stack.mallocFloat(1);

			for (int charIndex = 0; charIndex < cdata.capacity(); charIndex++) {
				// Reset the x and y position, as we are not using STB to bake quads.
				x.clear();
				y.clear();
				x.put(0, 0F);
				y.put(0, 0F);

				// Retrieve the baked character information
				STBTruetype.stbtt_GetBakedQuad(cdata, texture.getWidth(), texture.getHeight(), charIndex, x, y, charInfo, false);
				float endX = x.get(0);

				// The +0.5F is an offset that fixes the coordinates that STB provides.
				float x0 = charInfo.x0() + 0.5F;
				float y0 = charInfo.y0() + 0.5F;
				float x1 = charInfo.x1() + 0.5F;
				float y1 = charInfo.y1() + 0.5F;

				// Calculate the glyph information
				float width = x1 - x0;
				float height = y1 - y0;
				float advance = endX - x1;
				float originY = fontHeight + y0;

				// UV coordinates
				float u = charInfo.s0();
				float v = charInfo.t0();
				float u2 = charInfo.s1();
				float v2 = charInfo.t1();

				dataMap.put(charIndex + 32, new CharacterData((int) width, (int) height, 0, originY, advance, u, v, u2, v2, 1F));
			}
		}
	}

	@Override
	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	@Override
	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}

	@Override
	public float getScaleX() {
		return this.scaleX;
	}

	@Override
	public float getScaleY() {
		return this.scaleY;
	}

	@Override
	public void drawChar(@NotNull CharacterData data, double x, double y) {
		double y1 = y + data.renderOriginY * this.getScaleY();
		renderer.texRect2d(texture, x, y1, x + data.renderWidth * this.getScaleX(), y1 + data.renderHeight * this.getScaleY(), data.u(), data.v(), data.u2(), data.v2());
	}

	@Override
	public int getSize() {
		return fontHeight;
	}

	@Override
	public @Nullable CharacterData getCharacterData(char character) {
		return dataMap.get((int) character);
	}

	@Override
	public float getNewLineHeight() {
		return fontHeight * this.getScaleY();
	}

	@Override
	public float getCharacterWidth(char c, @Nullable CharacterData data) {
		if (data != null) {
			return (data.renderWidth + data.renderAdvance) * this.getScaleX();
		}

		return 0;
	}
}
