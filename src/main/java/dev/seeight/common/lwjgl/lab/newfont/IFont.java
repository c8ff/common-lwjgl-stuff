package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.renderer.renderer.Texture;

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
	Texture getTexture();

	/**
	 * The name of the string.
	 */
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
	CharacterData getCharacterData(int codepoint);
}
