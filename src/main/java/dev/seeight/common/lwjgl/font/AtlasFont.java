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
import dev.seeight.common.lwjgl.font.json.FontData;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class AtlasFont implements IFont {
	private final float size;
	private final Texture texture;
	private final FontData fontData;
	private final float ascent;
	private final float descent;
	private final float maxHeight;
	private final Map<Integer, CharacterData> characters;

	public AtlasFont(Texture texture, FontData fontData, float scale) {
		this.texture = texture;
		this.fontData = fontData;
		this.size = this.fontData.size * scale;
		
		this.characters = new HashMap<>();

		float[] maxOriginY = new float[1];
		float[] maxOriginX = new float[1];
		float[] maxDescent = new float[1];
		float[] maxHeight = new float[1];
		this.fontData.characters.forEach((s, c) -> {
			c.calcUV(texture.getWidth(), texture.getHeight());

			if (maxOriginY[0] < c.originY) {
				maxOriginY[0] = c.originY;
			}
			if (maxOriginX[0] < c.originX) {
				maxOriginX[0] = c.originX;
			}

			if (maxHeight[0] < c.height) {
				maxHeight[0] = c.height;
			}

			float descent = c.height - c.originY;
			if (maxDescent[0] < descent) {
				maxDescent[0] = descent;
			}

			characters.put(s.codePointAt(0), c);
		});
		this.fontData.characters.forEach((s, c) -> {
			c.renderWidth = c.width * scale;
			c.renderHeight = c.height * scale;
			c.renderOriginX = (maxOriginX[0] - c.originX) * scale;
			c.renderOriginY = (maxOriginY[0] - c.originY) * scale;
			c.renderAdvance = c.advance * scale;
		});

		// TODO: is this correct?
		this.ascent = maxOriginY[0] * scale;
		this.descent = -maxDescent[0] * scale;
		this.maxHeight = maxHeight[0] * scale + this.descent;
	}

	@Override
	public @NotNull Texture getTexture() {
		return this.texture;
	}

	@Override
	public @NotNull String getName() {
		return this.fontData.name;
	}

	@Override
	public int getSize() {
		return (int) this.size;
	}

	@Override
	public float getHeight() {
		return this.maxHeight;
	}

	@Override
	public float getLineGap() {
		return this.size;
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
		return this.characters.get(codepoint);
	}
}
