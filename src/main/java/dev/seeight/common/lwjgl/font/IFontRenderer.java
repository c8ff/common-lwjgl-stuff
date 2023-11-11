package dev.seeight.common.lwjgl.font;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IFontRenderer {
	void setScaleX(float scaleX);
	void setScaleY(float scaleY);

	float getScaleX();

	float getScaleY();

	/**
	 * Draws a string at the given coordinates.
	 *
	 * @return The X position that the string finishes rendering at.
	 */
	default double drawString(@NotNull String str, double x, double y) {
		return drawString(str.toCharArray(), x, y, 0, str.length());
	}

	/**
	 * Draws a string at the given coordinates.
	 *
	 * @return The X position that the characters finish rendering at.
	 */
	default double drawString(char @NotNull [] characters, double x, double y) {
		return drawString(characters, x, y, 0, characters.length);
	}

	/**
	 * Draws a string at the given coordinates.
	 *
	 * @return The X position that the characters finish rendering at.
	 */
	default double drawString(char @NotNull [] characters, double x, double y, int start, int end) throws IndexOutOfBoundsException {
		if (start < 0) {
			throw new IndexOutOfBoundsException("start cannot be lower than 0.");
		}
		if (start > end) {
			throw new IndexOutOfBoundsException("start is bigger than the end (" + start + ">= " + end + ").");
		}
		if (end > characters.length) {
			throw new IndexOutOfBoundsException("end is bigger than the characters length (" + characters.length + ").");
		}

		double startX = x;
		double maxX = x;
		for (int i = start; i < end; i++) {
			char c = characters[i];
			CharacterData data = this.getCharacterData(c);
			if (this.isNewLine(c)) {
				x = startX;
				y += this.getNewLineHeight();
			} else {
				if (data != null) {
					this.drawChar(data, x, y);
				} else {
					this.drawInvalidCharacter(startX, x, y, c);
				}

				x += this.getCharacterWidth(c, data);
			}

			if (x > maxX) {
				maxX = x;
			}
		}

		return maxX;
	}

	/**
	 * Draws a character at the given coordinates.
	 */
	void drawChar(@NotNull CharacterData data, double x, double y);

	/**
	 * @return The size of the font. Not to be confused with width or height.
	 */
	int getSize();

	/**
	 * @param string The string to calculate the width with.
	 * @return The width of the string.
	 */
	default float getWidthFloat(@NotNull String string) {
		return getWidthFloat(string, 0, string.length());
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @return The width of the characters.
	 */
	default float getWidthFloat(char @NotNull [] chars) {
		return getWidthFloat(chars, 0, chars.length);
	}

	/**
	 * @param string The string to calculate the width with.
	 * @param end    The end of the string.
	 * @return The width of the string.
	 */
	default float getWidthFloat(@NotNull String string, int end) throws IndexOutOfBoundsException {
		return getWidthFloat(string, 0, end);
	}

	/**
	 * @param string The string to calculate the width with.
	 * @param start  The beginning of the string.
	 * @param end    The end of the string.
	 * @return The width of the string.
	 */
	default float getWidthFloat(@NotNull String string, int start, int end) throws IndexOutOfBoundsException {
		return getWidthFloat(string.toCharArray(), start, end);
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @param end   The end of the characters.
	 * @return The width of the characters.
	 */
	default float getWidthFloat(char @NotNull [] chars, int end) throws IndexOutOfBoundsException {
		return getWidthFloat(chars, 0, end);
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @param start The beginning of the characters.
	 * @param end   The end of the characters.
	 * @return The width of the characters.
	 */
	default float getWidthFloat(char @NotNull [] chars, int start, int end) throws IndexOutOfBoundsException {
		if (start < 0) {
			throw new IndexOutOfBoundsException("start cannot be lower than 0.");
		}
		if (start > end) {
			throw new IndexOutOfBoundsException("start is bigger than the end (" + start + ">= " + end + ").");
		}
		if (end > chars.length) {
			throw new IndexOutOfBoundsException("end is bigger than the characters length (" + chars.length + ").");
		}

		float width = 0;
		for (int i = start; i < end; i++) {
			char c = chars[i];
			CharacterData data = this.getCharacterData(c);
			if (this.isNewLine(c)) {
				width = 0;
			} else {
				width += this.getCharacterWidth(c, data);
			}
		}

		return width;
	}

	/**
	 * @param string The string to calculate the height with.
	 * @return The height of the string.
	 */
	default float getHeightFloat(@NotNull String string) throws IndexOutOfBoundsException {
		return getHeightFloat(string.toCharArray(), 0, string.length());
	}

	/**
	 * @param chars The characters to calculate the height with.
	 * @return The height of the characters.
	 */
	default float getHeightFloat(char[] chars) throws IndexOutOfBoundsException {
		return getHeightFloat(chars, 0, chars.length);
	}

	/**
	 * @param chars The characters to calculate the height with.
	 * @param start The start of the characters.
	 * @param end   The end of the characters.
	 * @return The height of the characters.
	 */
	default float getHeightFloat(char[] chars, int start, int end) throws IndexOutOfBoundsException {
		if (start < 0) {
			throw new IndexOutOfBoundsException("start cannot be lower than 0.");
		}
		if (start > end) {
			throw new IndexOutOfBoundsException("start is bigger than the end (" + start + ">= " + end + ").");
		}
		if (end > chars.length) {
			throw new IndexOutOfBoundsException("end is bigger than the characters length (" + chars.length + ").");
		}

		float height = this.getNewLineHeight();
		for (int i = start; i < end; i++) {
			char c = chars[i];
			if (this.isNewLine(c)) {
				height += this.getNewLineHeight();
			}
		}
		return height;
	}

	/**
	 * @return The data of the {@code character} parameter. {@code null} if there isn't any data for it.
	 */
	@Nullable CharacterData getCharacterData(char character);

	/**
	 * @return True if the character should be treated as a new line.
	 */
	default boolean isNewLine(char c) {
		return c == '\n';
	}

	/**
	 * @return The new line height.
	 */
	float getNewLineHeight();

	/**
	 * @param c    The character
	 * @param data The data of the character.
	 * @return The width of the character.
	 */
	@SuppressWarnings("unused")
	float getCharacterWidth(char c, @Nullable CharacterData data);

	/**
	 * Draws an invalid character. Called when {@link #getCharacterData(char)} returns null when rendering a string.
	 *
	 * @param startX The X position where the string started from.
	 * @param x      The X position that the current character will be drawn at.
	 * @param y      The Y position that the current character will be drawn at.
	 */
	@SuppressWarnings("unused")
	default void drawInvalidCharacter(double startX, double x, double y, char c) {

	}
}
