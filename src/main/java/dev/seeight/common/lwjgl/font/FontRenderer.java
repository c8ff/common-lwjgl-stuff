package dev.seeight.common.lwjgl.font;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.font.json.FontData;
import dev.seeight.renderer.renderer.Renderer;
import dev.seeight.renderer.renderer.Texture;

import java.util.HashMap;
import java.util.Map;

/**
 * Renders fonts.
 * @author C8FF
 */
@SuppressWarnings("UnusedReturnValue")
public class FontRenderer {
	protected final Map<Character, CharacterData> characterData;
	protected int size;
	protected final Texture texture;

	public final String supportedCharacters;

	protected final Renderer renderer;

	public int FONT_HEIGHT;
	public float FONT_HEIGHT_FLOAT;

	public float scaleX = 1;
	public float scaleY = 1;

	private final FontData fontData;

	private float maxOriginX;
	private float maxOriginY;
	private float maxHeight;

	public FontRenderer(Renderer renderer, FontData fontData, Texture texture) {
		this.fontData = fontData;

		StringBuilder builder = new StringBuilder();
		Map<Character, CharacterData> map = new HashMap<>();

		fontData.characters.forEach((s, characterData) -> {
			builder.append(s);
			map.put(s.charAt(0), characterData);

			maxHeight = Math.max(maxHeight, characterData.height);
			maxOriginX = Math.max(maxOriginX, characterData.originX);
			maxOriginY = Math.max(maxOriginY, characterData.originY);

			characterData.calcUV(fontData.width, fontData.height);
		});

		this.FONT_HEIGHT_FLOAT = maxHeight;
		this.FONT_HEIGHT = (int) maxHeight;
		this.supportedCharacters = builder.toString();
		this.characterData = map;
		this.renderer = renderer;
		this.texture = texture;
	}

	/**
	 * Draws a string at the given coordinates.
	 */
	public double drawString(String str, double x, double y) {
		return this.drawString(str.toCharArray(), x, y);
	}

	/**
	 * Draws a string at the given coordinates.
	 */
	public double drawString(char[] characters, double x, double y) {
		double startX = x;
		for (char c : characters) {
			CharacterData data = this.getCharacterData(c);
			if (data != null) {
				this.drawChar(data, x, y);
				x += this.getCharacterWidth(data);
			} else if (this.isNewLine(c)) {
				x = startX;
				y += this.getNewLineHeight();
			}
		}

		return x;
	}

	/**
	 * Draws a character at the given coordinates.
	 */
	public void drawChar(CharacterData data, double x, double y) {
		double tX = x + (maxOriginX - data.originX) * this.scaleX;
		double tY = y + (maxOriginY - data.originY) * this.scaleY;
		renderer.texRect2d(this.texture, tX, tY, tX + data.width * this.scaleX, tY + data.height * this.scaleY, data.u(), data.v(), data.u2(), data.v2());
	}

	/**
	 * @return The size of the font (not to be confused with width or height).
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return The width of the string.
	 */
	public float getWidthFloat(String s) {
		return this.getWidthFloat(s.toCharArray());
	}

	/**
	 * @return The width of the characters.
	 */
	public float getWidthFloat(char[] cs) {
		float width = 0;
		for (char c : cs) {
			CharacterData data = characterData.get(c);
			if (data != null) {
				width += this.getCharacterWidth(data);
			} else if (this.isNewLine(c)) {
				width = 0;
			}
		}
		return width;
	}

	public float getWidthFloat(String string, int maxIndex) {
		return this.getWidthFloat(string.toCharArray(), maxIndex);
	}

	public float getWidthFloat(char[] chars, int maxIndex) {
		float width = 0;
		for (int i = 0, len = Math.min(chars.length, maxIndex); i < len; i++) {
			char c = chars[i];
			CharacterData data = this.getCharacterData(c);
			if (data != null) {
				width += this.getCharacterWidth(data);
			} else if (this.isNewLine(c)) {
				width = 0;
			}
		}

		return width;
	}

	public float getWidthFloat(char[] chars, int start, int end) {
		float width = 0;
		for (int i = start, len = Math.min(chars.length, end); i < len; i++) {
			char c = chars[i];
			CharacterData data = this.getCharacterData(c);
			if (data != null) {
				width += this.getCharacterWidth(data);
			} else if (this.isNewLine(c)) {
				width = 0;
			}
		}

		return width;
	}

	/**
	 * @return The height of the string.
	 */
	public float getHeightFloat(String str) {
		return this.getHeightFloat(str.toCharArray());
	}

	/**
	 * @return The height of the characters.
	 */
	public float getHeightFloat(char[] chars) {
		float height = this.getNewLineHeight();
		for (char c : chars) {
			if (this.isNewLine(c)) {
				height += this.getNewLineHeight();
			}
		}
		return height;
	}

	public FontData getFontData() {
		return fontData;
	}

	public CharacterData getCharacterData(char character) {
		return this.characterData.get(character);
	}

	protected boolean isNewLine(char c) {
		return c == '\n';
	}

	protected float getNewLineHeight() {
		return FONT_HEIGHT_FLOAT * scaleY;
	}

	protected float getCharacterWidth(CharacterData characterData) {
		if (characterData != null) {
			return characterData.advance * scaleX;
		}

		return 0;
	}
}
