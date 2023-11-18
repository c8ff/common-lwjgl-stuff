package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;

public interface IFontRenderer {
	static void assertIndices(int max, int start, int end) {
		if (start < 0) throw new IndexOutOfBoundsException("start < 0");
		if (start > max) throw new IllegalArgumentException("start > max");
		if (end > max) throw new IndexOutOfBoundsException(String.format("end %s > max %s", end, max));
	}

	default float drawString(IFont font, String string, float x, float y) {
		return drawString(font, string.toCharArray(), x, y, 0, string.length());
	}

	default float drawString(IFont font, String string, float x, float y, int start, int end) {
		return drawString(font, string.toCharArray(), x, y, start, end);
	}

	default float drawString(IFont font, char[] chars, float x, float y) {
		return drawString(font, chars, x, y, 0, chars.length);
	}

	default float getWidthFloat(IFont font, String string) {
		return getWidthFloat(font, string.toCharArray(), 0, string.length());
	}

	default float getWidthFloat(IFont font, char[] chars) {
		return getWidthFloat(font, chars, 0, chars.length);
	}

	default float getHeightFloat(IFont font, String string) {
		return getHeightFloat(font, string.toCharArray(), 0, string.length());
	}

	default float getHeightFloat(IFont font, String string, int start, int end) {
		return getHeightFloat(font, string.toCharArray(), start, end);
	}

	default float getHeightFloat(IFont font, char[] chars) {
		return getHeightFloat(font, chars, 0, chars.length);
	}

	default float drawString(IFont font, char[] chars, float x, float y, int start, int end) {
		IFontRenderer.assertIndices(chars.length, start, end);

		float startX = x;
		float maxX = x;
		for (int i = start; i < end; i++) {
			int codePoint = chars[i];
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

	default float getWidthFloat(IFont font, char[] chars, int start, int end) {
		IFontRenderer.assertIndices(chars.length, start, end);

		float width = 0;
		float maxWidth = 0;
		for (int i = start; i < end; i++) {
			int codePoint = chars[i];

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

	default float getHeightFloat(IFont font, char[] chars, int start, int end) {
		IFontRenderer.assertIndices(chars.length, start, end);

		float height = this.getNewLineHeight(font);
		for (int i = start; i < end; i++) {
			int codePoint = chars[i];
			if (isNewLine(codePoint)) {
				height += this.getNewLineHeight(font);
			}
		}

		return height;
	}

	void drawChar(IFont font, CharacterData data, float x, float y);

	void drawInvalidChar(IFont font, int codepoint, float x, float y);

	default float getNewLineHeight(IFont font) {
		return font.getSize() * this.getScaleY();
	}

	default boolean isNewLine(int codepoint) {
		return codepoint == '\n';
	}

	default float getCharacterWidth(IFont font, CharacterData data, int codepoint) {
		if (data != null) {
			return (data.renderWidth + data.renderAdvance) * this.getScaleX();
		}

		return 0;
	}

	float getScaleX();

	void setScaleX(float scaleX);

	float getScaleY();

	void setScaleY(float scaleY);

	default void setScale(float scaleX, float scaleY) {
		this.setScaleX(scaleX);
		this.setScaleY(scaleY);
	}

	default void setScale(float scale) {
		this.setScale(scale, scale);
	}
}
