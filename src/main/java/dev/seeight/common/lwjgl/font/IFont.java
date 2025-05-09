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
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Holds the data of a font. These include, size, height, etc.
 * An implementation of this would also hold the information of each supported character and accessed
 * by the method {@link #getCharacterData(int)} using a codepoint.
 *
 * @author C8FF
 */
public interface IFont {
	/**
	 * The corresponding bitmap or SDF texture.
	 */
	@NotNull
	Texture getTexture();

	/**
	 * The name of the string.
	 */
	@NotNull
	String getName();

	/**
	 * The size of the font.
	 */
	int getSize();

	/**
	 * The height of the biggest character (vertically) from the font.
	 */
	float getHeight();

	/**
	 * The line gap of the font.
	 */
	float getLineGap();

	/**
	 * The ascent of the font. This is the tallest ascent of a character.<p>
	 * A descent is the part of a character above the baseline of the string.
	 */
	float getAscent();

	/**
	 * The descent of the font. This is the tallest descent of a character.<p>
	 * A descent is the part of a character bellow the baseline of the string.
	 */
	float getDescent();

	/**
	 * Gets the information (width, height, advance, origin, etc.) of a specific codepoint.
	 */
	@Nullable
	CharacterData getCharacterData(int codepoint);
}
