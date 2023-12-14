package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;

public interface IFontRenderer {
	static void assertIndices(int max, int start, int end) throws IndexOutOfBoundsException {
		if (start < 0) throw new IndexOutOfBoundsException("start < 0");
		if (start > max) throw new IllegalArgumentException("start > max");
		if (end > max) throw new IndexOutOfBoundsException(String.format("end %s > max %s", end, max));
	}

	/**
	 * Renders a string using the specified font.
	 *
	 * @param font   The font that the {@code characters} will be rendered with.
	 * @param string The string to be rendered.
	 * @param x      The X position where the string will be rendered.
	 * @param y      The Y position where the string will be rendered.
	 * @return The end of the string in the X axis.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float drawString(IFont font, String string, float x, float y) throws IndexOutOfBoundsException {
		return drawString(font, string.toCharArray(), x, y, 0, string.length());
	}

	/**
	 * Renders a string using the specified font.
	 *
	 * @param font   The font that the {@code characters} will be rendered with.
	 * @param string The string to be rendered.
	 * @param x      The X position where the string will be rendered.
	 * @param y      The Y position where the string will be rendered.
	 * @param start  The start index inside the {@code string}'s length.
	 * @param end    The end index inside or equal to the length of {@code string}.
	 * @return The end of the string in the X axis.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float drawString(IFont font, String string, float x, float y, int start, int end) throws IndexOutOfBoundsException {
		return drawString(font, string.toCharArray(), x, y, start, end);
	}

	/**
	 * Renders a string using the specified font.
	 *
	 * @param font       The font that the {@code characters} will be rendered with.
	 * @param characters The characters that will be rendered.
	 * @param x          The X position where the string will be rendered.
	 * @param y          The Y position where the string will be rendered.
	 * @return The end of the string in the X axis.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float drawString(IFont font, char[] characters, float x, float y) throws IndexOutOfBoundsException {
		return drawString(font, characters, x, y, 0, characters.length);
	}

	/**
	 * Calculates the width of the {@code string} using the properties of the specified font.
	 *
	 * @param font   The font that will be used.
	 * @param string The string.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getWidthFloat(IFont font, String string) throws IndexOutOfBoundsException {
		return getWidthFloat(font, string.toCharArray(), 0, string.length());
	}

	/**
	 * Calculates the width of the {@code string} using the properties of the specified font.
	 *
	 * @param font   The font that will be used.
	 * @param string The string.
	 * @param start  The start index to calculate the width from. Must be inside the {@code string} length.
	 * @param end    The end index to calculate the width from. Must be inside or equal to the length of {@code string}.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getWidthFloat(IFont font, String string, int start, int end) throws IndexOutOfBoundsException {
		return getWidthFloat(font, string.toCharArray(), start, end);
	}

	/**
	 * Calculates the width of the {@code string} using the properties of the specified font.
	 *
	 * @param font   The font that will be used.
	 * @param characters The string characters.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getWidthFloat(IFont font, char[] characters) throws IndexOutOfBoundsException {
		return getWidthFloat(font, characters, 0, characters.length);
	}

	/**
	 * Calculates the height of the {@code characters} using the properties of the specified font.
	 *
	 * @param font       The font that will be used.
	 * @param string The string.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getHeightFloat(IFont font, String string) throws IndexOutOfBoundsException {
		return getHeightFloat(font, string.toCharArray(), 0, string.length());
	}

	/**
	 * Calculates the height of the {@code characters} using the properties of the specified font.
	 *
	 * @param font       The font that will be used.
	 * @param string The string.
	 * @param start      The start index to calculate the height from. Must be inside the {@code characters} length.
	 * @param end        The end index to calculate the height from. Must be inside or equal to the length of {@code characters}.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getHeightFloat(IFont font, String string, int start, int end) throws IndexOutOfBoundsException {
		return getHeightFloat(font, string.toCharArray(), start, end);
	}

	/**
	 * Calculates the height of the {@code characters} using the properties of the specified font.
	 *
	 * @param font       The font that will be used.
	 * @param characters The string characters.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getHeightFloat(IFont font, char[] characters) throws IndexOutOfBoundsException {
		return getHeightFloat(font, characters, 0, characters.length);
	}

	/**
	 * Renders a string using the specified font.
	 *
	 * @param font       The font that the {@code characters} will be rendered with.
	 * @param characters The characters that will be rendered.
	 * @param x          The X position where the string will be rendered.
	 * @param y          The Y position where the string will be rendered.
	 * @param start      The start index inside {@code characters}.
	 * @param end        The end index inside or equal to the length of {@code characters}.
	 * @return The end of the string in the X axis.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float drawString(IFont font, char[] characters, float x, float y, int start, int end) throws IndexOutOfBoundsException {
		if (characters.length == 0)
			return x;

		IFontRenderer.assertIndices(characters.length, start, end);

		float startX = x;
		float maxX = x;
		for (int i = start; i < end; i++) {
			int codePoint = characters[i];
			if (isNewLine(codePoint)) {
				if (maxX < x) {
					maxX = x;
				}
				x = startX;
				y += getNewLineHeight(font);
				continue;
			}

			CharacterData data = font.getCharacterData(codePoint);
			if (data != null) {
				drawChar(font, data, x, y);
			} else {
				drawInvalidChar(font, codePoint, x, y);
			}

			x += getCharacterWidth(font, data, codePoint);
		}
		if (maxX < x) {
			maxX = x;
		}

		return maxX;
	}

	/**
	 * Calculates the width of the {@code characters} using the properties of the specified font.
	 *
	 * @param font       The font that will be used.
	 * @param characters The string characters.
	 * @param start      The start index to calculate the width from. Must be inside the {@code characters} length.
	 * @param end        The end index to calculate the width from. Must be inside or equal to the length of {@code characters}.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getWidthFloat(IFont font, char[] characters, int start, int end) throws IndexOutOfBoundsException {
		IFontRenderer.assertIndices(characters.length, start, end);

		float width = 0;
		float maxWidth = 0;
		for (int i = start; i < end; i++) {
			int codePoint = characters[i];

			if (isNewLine(codePoint)) {
				width = 0;
				continue;
			}

			width += this.getCharacterWidth(font, font.getCharacterData(codePoint), codePoint);

			if (maxWidth < width) {
				maxWidth = width;
			}
		}

		return width;
	}

	/**
	 * Calculates the height of the {@code characters} using the properties of the specified font.
	 *
	 * @param font       The font that will be used.
	 * @param characters The string characters.
	 * @param start      The start index to calculate the height from. Must be inside the {@code characters} length.
	 * @param end        The end index to calculate the height from. Must be inside or equal to the length of {@code characters}.
	 * @return The calculated width.
	 * @throws IndexOutOfBoundsException If {@code start} or {@code end} are out of bounds.
	 */
	default float getHeightFloat(IFont font, char[] characters, int start, int end) throws IndexOutOfBoundsException {
		IFontRenderer.assertIndices(characters.length, start, end);

		float height = this.getNewLineHeight(font);
		for (int i = start; i < end; i++) {
			int codePoint = characters[i];
			if (isNewLine(codePoint)) {
				height += this.getNewLineHeight(font);
			}
		}

		return height;
	}

	/**
	 * Renders a character using the specified font.
	 * Not recommended to use separately from {@link #drawString(IFont, char[], float, float, int, int)}.
	 *
	 * @param font The font to be used.
	 * @param data The data of the character.
	 * @param x    The X axis position to render the character to.
	 * @param y    The Y axis position to render the character to.
	 */
	void drawChar(IFont font, CharacterData data, float x, float y);

	/**
	 * Renders an unknown character using the specified font.
	 * Not recommended to use separately from {@link #drawString(IFont, char[], float, float, int, int)}
	 *
	 * @param font      The font to be used.
	 * @param codepoint The codepoint of the character.
	 * @param x         The X axis position to render the character to.
	 * @param y         The Y axis position to render the character to.
	 */
	void drawInvalidChar(IFont font, int codepoint, float x, float y);

	/**
	 * Gets the height to use in a new line.
	 *
	 * @return The new line height.
	 */
	default float getNewLineHeight(IFont font) {
		return font.getSize() * this.getScaleY();
	}

	/**
	 * @return True if the codepoint represents a new line character.
	 */
	default boolean isNewLine(int codepoint) {
		return codepoint == '\n';
	}

	/**
	 * Calculates the width of a character using {@code data} or {@code codepoint}, depending on the implementation.
	 * It also applies the scale X.
	 *
	 * @param font      The font to be used.
	 * @param data      The character's data. Can be null.
	 * @param codepoint The codepoint of the character to be rendered.
	 * @return The width of the character.
	 */
	default float getCharacterWidth(IFont font, CharacterData data, int codepoint) {
		if (data != null) {
			return (data.renderWidth + data.renderAdvance) * this.getScaleX();
		}

		return 0;
	}

	/**
	 * @return The horizontal scale of the rendered strings.
	 */
	float getScaleX();

	/**
	 * Sets the horizontal scale of the rendered strings.
	 *
	 * @param scaleX The horizontal scale.
	 */
	void setScaleX(float scaleX);

	/**
	 * @return The vertical scale of the rendered strings.
	 */
	float getScaleY();

	/**
	 * Sets the vertical scale of the rendered strings.
	 *
	 * @param scaleY The vertical scale.
	 */
	void setScaleY(float scaleY);

	/**
	 * Sets both the horizontal and vertical scale of the rendered strings.
	 *
	 * @param scaleX The horizontal scale.
	 * @param scaleY The vertical scale.
	 */
	default void setScale(float scaleX, float scaleY) {
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
	}

	/**
	 * Sets both the horizontal and vertical scale of the rendered strings.
	 *
	 * @param scale The horizontal and vertical scale.
	 */
	default void setScale(float scale) {
		this.setScale(scale, scale);
	}
}
