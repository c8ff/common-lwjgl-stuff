/*
 * common-lwjgl-stuff
 * Copyright (C) 2024 c8ff
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.seeight.common.lwjgl.font;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.util.IOUtil;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.components.GLTexture;
import org.jetbrains.annotations.NotNull;
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
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class TTFFont implements IFont {
	private static STBTTFontinfo _tempInfo;

	private final Texture texture;
	private final Map<Integer, CharacterData> glyphs;
	private final String name;
	private final int fontHeight;
	private final float height;
	private final float ascent;
	private final float descent;
	private final float lineGap;

	public TTFFont(InputStream inputStream, int textureWidth, int textureHeight, int fontHeight) throws IOException {
		this(inputStream, textureWidth, textureHeight, fontHeight, 95, 32, 1F);
	}

	public TTFFont(InputStream inputStream, int textureWidth, int textureHeight, int fontHeight, float scale) throws IOException {
		this(inputStream, textureWidth, textureHeight, fontHeight, 95, 32, scale);
	}

	public TTFFont(InputStream ttfFile, int textureWidth, int textureHeight, int fontHeight, int characterCount, int firstChar, float scale) throws IOException {
		ByteBuffer ttf = IOUtil.byteBufferFrom(ttfFile);

		if (_tempInfo == null) {
			_tempInfo = STBTTFontinfo.create();
		}
		if (!STBTruetype.stbtt_InitFont(_tempInfo, ttf)) {
			throw new RuntimeException("failed to init font");
		}

		// Create Texture
		int texID = GL11.glGenTextures();

		// Allocate the character data
		STBTTBakedChar.Buffer cdata = STBTTBakedChar.malloc(characterCount);

		// Create bitmap
		ByteBuffer bitmap = BufferUtils.createByteBuffer(textureWidth * textureHeight);
		STBTruetype.stbtt_BakeFontBitmap(ttf, fontHeight, bitmap, textureWidth, textureHeight, firstChar, cdata);

		// Move bitmap into texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, texID);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, textureWidth, textureHeight, 0, GL11.GL_ALPHA, GL11.GL_UNSIGNED_BYTE, bitmap);
		this.applyTextureParameters();

		this.texture = new GLTexture(texID, textureWidth, textureHeight);
		this.glyphs = new HashMap<>();

		float ascent;
		float descent;
		float lineGap;

		float maxHeight = 0;

		// Collect glyphs into a friendlier environment
		try (MemoryStack stack = MemoryStack.stackPush()) {
			STBTTAlignedQuad charInfo = STBTTAlignedQuad.malloc(stack);

			// Get name of the font, along with the size.
			// http://forum.lwjgl.org/index.php?topic=6565.0
			ByteBuffer byteBuffer = STBTruetype.stbtt_GetFontNameString(_tempInfo, STBTruetype.STBTT_PLATFORM_ID_MICROSOFT, STBTruetype.STBTT_MS_EID_UNICODE_BMP, STBTruetype.STBTT_MS_LANG_ENGLISH, 4);
			if (byteBuffer != null) {
				this.name = MemoryUtil.memUTF8(byteBuffer.order(ByteOrder.BIG_ENDIAN)).replace("\u0000", "") + " - " + fontHeight;
			} else {
				this.name = "unknown - " + fontHeight;
			}

			// Get the scale for the next operations.
			float sc = STBTruetype.stbtt_ScaleForPixelHeight(_tempInfo, fontHeight);

			// Get vertical information for moving the characters into the correct position.
			IntBuffer _ascent = stack.mallocInt(1);
			IntBuffer _descent = stack.mallocInt(1);
			IntBuffer _lineGap = stack.mallocInt(1);
			STBTruetype.stbtt_GetFontVMetrics(_tempInfo, _ascent, _descent, _lineGap);
			ascent = _ascent.get(0) * sc;
			descent = _descent.get(0) * sc;
			lineGap = _lineGap.get(0) * sc;

			// Center the font
			float fontOffset = (float) Math.ceil(ascent + descent);

			// Convert characters into a friendlier format
			FloatBuffer x = stack.mallocFloat(1);
			FloatBuffer y = stack.mallocFloat(1);

			IntBuffer _advanceWidth = stack.mallocInt(1);
			IntBuffer _leftSideBearing = stack.mallocInt(1);

			for (int charIndex = 0; charIndex < cdata.capacity(); charIndex++) {
				// Reset the x and y position, as we are not using STB to bake quads.
				x.clear();
				y.clear();
				x.put(0, 0F);
				y.put(0, 0F);

				// Retrieve the baked character information
				STBTruetype.stbtt_GetBakedQuad(cdata, textureWidth, textureHeight, charIndex, x, y, charInfo, false);

				// Retrieve horizontal codepoint information
				// This information can be extracted the baked quad, but I do it this way just to be sure.
				// It's not like performance is important at initialization... I think.
				STBTruetype.stbtt_GetCodepointHMetrics(_tempInfo, charIndex + firstChar, _advanceWidth.clear(), _leftSideBearing.clear());
				float advanceWidth = _advanceWidth.get(0) * sc;
				float originX = _leftSideBearing.get(0) * sc;

				// The +0.5F is an offset that fixes the coordinates that STB provides.
				float x0 = charInfo.x0() + 0.5F;
				float y0 = charInfo.y0() + 0.5F;
				float x1 = charInfo.x1() + 0.5F;
				float y1 = charInfo.y1() + 0.5F;

				// Calculate the glyph information
				float width = x1 - x0;
				float height = y1 - y0;
				float originY = y0 + fontOffset;

				// UV coordinates
				float u = charInfo.s0();
				float v = charInfo.t0();
				float u2 = charInfo.s1();
				float v2 = charInfo.t1();

				if (maxHeight < height) {
					maxHeight = height;
				}

				glyphs.put(charIndex + firstChar, new CharacterData((int) width, (int) height, originX, originY, advanceWidth, u, v, u2, v2, scale));
			}
		}

		this.fontHeight = (int) (fontHeight * scale);
		this.height = (maxHeight) * scale;
		this.ascent = ascent * scale;
		this.descent = descent * scale;
		this.lineGap = lineGap * scale;
	}

	protected void applyTextureParameters() {
		// This parameter fixes the font being black.
		// https://stackoverflow.com/a/16950771
		GL11.glTexParameteriv(GL11.GL_TEXTURE_2D, GL33.GL_TEXTURE_SWIZZLE_RGBA, new int[] {
				GL11.GL_ONE,
				GL11.GL_ONE,
				GL11.GL_ONE,
				GL11.GL_ALPHA,
		});
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
	}

	@Override
	public @NotNull Texture getTexture() {
		return this.texture;
	}

	@Override
	public @NotNull String getName() {
		return this.name;
	}

	@Override
	public int getSize() {
		return this.fontHeight;
	}

	@Override
	public float getHeight() {
		return this.height;
	}

	@Override
	public float getLineGap() {
		return this.lineGap;
	}

	@Override
	public float getAscent() {
		return this.ascent;
	}

	@Override
	public float getDescent() {
		return this.descent;
	}

	@Override
	public CharacterData getCharacterData(int codepoint) {
		return this.glyphs.get(codepoint);
	}
}
