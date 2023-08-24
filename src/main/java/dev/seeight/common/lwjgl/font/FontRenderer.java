package dev.seeight.common.lwjgl.font;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.font.json.FontData;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.Texture;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders fonts.
 * @author C8FF
 */
@SuppressWarnings("UnusedReturnValue")
public class FontRenderer {
	/**
	 * Used to render the characters.
	 */
	@NotNull
	protected final Renderer renderer;
	/**
	 * Holds the data for the supported characters.
	 */
	protected final Map<Character, CharacterData> characterData;
	/**
	 * The size of the font.
	 */
	protected int size;
	/**
	 * The texture containing all the glyphs of this font.
	 */
	@NotNull
	protected final Texture texture;

	/**
	 * A string holding all the supported characters for this font.
	 */
	public final String supportedCharacters;

	/**
	 * Same as {@link #FONT_HEIGHT_FLOAT}, but as an integer.
	 */
	public int FONT_HEIGHT;
	/**
	 * The height of the font. It's the tallest height of a character in the font.
	 */
	public float FONT_HEIGHT_FLOAT;

	/**
	 * The horizontal scale of the font. This will affect the rendering and width of any strings.
	 */
	public float scaleX = 1;
	/**
	 * The vertical scale of the font. This will affect the rendering and width of any strings.
	 */
	public float scaleY = 1;

	/**
	 * The font's data. Includes the character data for the characters.
	 */
	@NotNull
	private final FontData fontData;

	/**
	 * The maximum horizontal origin.
	 */
	private float maxOriginX;
	/**
	 * The maximum vertical origin.
	 */
	private float maxOriginY;
	/**
	 * The maximum character height.
	 */
	private float maxHeight;

	public FontRenderer(@NotNull Renderer renderer, @NotNull FontData fontData, @NotNull Texture texture) {
		this(renderer, fontData, texture, 1);
	}

	public FontRenderer(@NotNull Renderer renderer, @NotNull FontData fontData, @NotNull Texture texture, float sizeMultiplier) {
		this.fontData = fontData;

		StringBuilder builder = new StringBuilder();
		Map<Character, CharacterData> map = new HashMap<>();

		fontData.characters.forEach((s, characterData) -> {
			builder.append(s);
			map.put(s.charAt(0), characterData);

			characterData.renderWidth = characterData.width * sizeMultiplier;
			characterData.renderHeight = characterData.height * sizeMultiplier;
			characterData.renderOriginX = characterData.originX * sizeMultiplier;
			characterData.renderOriginY = characterData.originY * sizeMultiplier;
			characterData.renderAdvance = characterData.advance * sizeMultiplier;

			maxHeight = Math.max(maxHeight, characterData.renderHeight);
			maxOriginX = Math.max(maxOriginX, characterData.renderOriginX);
			maxOriginY = Math.max(maxOriginY, characterData.renderOriginY);

			characterData.calcUV(fontData.width, fontData.height);
		});

		this.FONT_HEIGHT_FLOAT = maxHeight;
		this.FONT_HEIGHT = (int) this.FONT_HEIGHT_FLOAT;
		this.supportedCharacters = builder.toString();
		this.characterData = map;
		this.renderer = renderer;
		this.texture = texture;
	}

	/**
	 * Draws a string at the given coordinates.
	 * @return The X position that the string finishes rendering at.
	 */
	public double drawString(@NotNull String str, double x, double y) {
		return this.drawString(str.toCharArray(), x, y);
	}

	/**
	 * Draws a string at the given coordinates.
	 * @return The X position that the characters finish rendering at.
	 */
	public double drawString(char @NotNull [] characters, double x, double y) {
		return drawString(characters, x, y, 0, characters.length);
	}

	/**
	 * Draws a string at the given coordinates.
	 * @return The X position that the characters finish rendering at.
	 */
	public double drawString(char @NotNull [] characters, double x, double y, int start, int end) throws IndexOutOfBoundsException {
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
		}

		return x;
	}

	/**
	 * Draws a character at the given coordinates.
	 */
	public void drawChar(@NotNull CharacterData data, double x, double y) {
		double tX = x + (maxOriginX - data.renderOriginX) * this.scaleX;
		double tY = y + (maxOriginY - data.renderOriginY) * this.scaleY;
		renderer.texRect2d(this.texture, tX, tY, tX + data.renderWidth * this.scaleX, tY + data.renderHeight * this.scaleY, data.u(), data.v(), data.u2(), data.v2());
	}

	/**
	 * @return The size of the font. Not to be confused with width or height.
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @param string The string to calculate the width with.
	 * @return The width of the string.
	 */
	public float getWidthFloat(@NotNull String string) {
		return this.getWidthFloat(string.toCharArray());
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @return The width of the characters.
	 */
	public float getWidthFloat(char @NotNull [] chars) {
		return getWidthFloat(chars, 0, chars.length);
	}

	/**
	 * @param string The string to calculate the width with.
	 * @param end The end of the string.
	 * @return The width of the string.
	 */
	public float getWidthFloat(@NotNull String string, int end) throws IndexOutOfBoundsException {
		return this.getWidthFloat(string, 0, end);
	}

	/**
	 * @param string The string to calculate the width with.
	 * @param start The beginning of the string.
	 * @param end The end of the string.
	 * @return The width of the string.
	 */
	public float getWidthFloat(@NotNull String string, int start, int end) throws IndexOutOfBoundsException {
		return this.getWidthFloat(string.toCharArray(), start, end);
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @param end The end of the characters.
	 * @return The width of the characters.
	 */
	public float getWidthFloat(char @NotNull [] chars, int end) throws IndexOutOfBoundsException {
		return getWidthFloat(chars, 0, end);
	}

	/**
	 * @param chars The characters to calculate the width with.
	 * @param start The beginning of the characters.
	 * @param end The end of the characters.
	 * @return The width of the characters.
	 */
	public float getWidthFloat(char @NotNull [] chars, int start, int end) throws IndexOutOfBoundsException {
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
	public float getHeightFloat(@NotNull String string) throws IndexOutOfBoundsException {
		return this.getHeightFloat(string.toCharArray());
	}

	/**
	 * @param chars The characters to calculate the height with.
	 * @return The height of the characters.
	 */
	public float getHeightFloat(char[] chars) throws IndexOutOfBoundsException {
		return getHeightFloat(chars, 0, chars.length);
	}

	/**
	 * @param chars The characters to calculate the height with.
	 * @param start The start of the characters.
	 * @param end The end of the characters.
	 * @return The height of the characters.
	 */
	public float getHeightFloat(char[] chars, int start, int end) throws IndexOutOfBoundsException {
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
	 * @return The font data backing the font.
	 */
	public @NotNull FontData getFontData() {
		return fontData;
	}

	/**
	 * @return The data of the {@code character} parameter. {@code null} if there isn't any data for it.
	 */
	public @Nullable CharacterData getCharacterData(char character) {
		return this.characterData.get(character);
	}

	/**
	 * @return True if the character should be treated as a new line.
	 */
	protected boolean isNewLine(char c) {
		return c == '\n';
	}

	/**
	 * @return The new line height.
	 */
	protected float getNewLineHeight() {
		return FONT_HEIGHT_FLOAT * scaleY;
	}

	/**
	 * @param c The character
	 * @param data The data of the character.
	 * @return The width of the character.
	 */
	@SuppressWarnings("unused")
	protected float getCharacterWidth(char c, @Nullable CharacterData data) {
		if (data != null) {
			return data.renderAdvance * scaleX;
		}

		return 0;
	}

	/**
	 * Draws an invalid character. Called when {@link #getCharacterData(char)} returns null when rendering a string.
	 * @param startX The X position where the string started from.
	 * @param x The X position that the current character will be drawn at.
	 * @param y The Y position that the current character will be drawn at.
	 */
	@SuppressWarnings("unused")
	protected void drawInvalidCharacter(double startX, double x, double y, char c) {

	}
}
