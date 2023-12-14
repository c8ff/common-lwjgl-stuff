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

	default float drawString(IFont font, char[] characters, float x, float y) {
		return drawString(font, characters, x, y, 0, characters.length);
	}

	default float getWidthFloat(IFont font, String string) {
		return getWidthFloat(font, string.toCharArray(), 0, string.length());
	}

	default float getWidthFloat(IFont font, char[] characters) {
		return getWidthFloat(font, characters, 0, characters.length);
	}

	default float getHeightFloat(IFont font, String string) {
		return getHeightFloat(font, string.toCharArray(), 0, string.length());
	}

	default float getHeightFloat(IFont font, String string, int start, int end) {
		return getHeightFloat(font, string.toCharArray(), start, end);
	}

	default float getHeightFloat(IFont font, char[] characters) {
		return getHeightFloat(font, characters, 0, characters.length);
	}

	default float drawString(IFont font, char[] characters, float x, float y, int start, int end) {
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

	default float getWidthFloat(IFont font, char[] characters, int start, int end) {
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

	default float getHeightFloat(IFont font, char[] characters, int start, int end) {
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

	default char[] cropString(IFont font, String string, float maxWidth, boolean appendDots) {
		char[] arr = string.toCharArray();
		float w = 0;
		int start = 0;
		int end = arr.length - 1;
		boolean isCropped = false;
		for (int i = arr.length - 1; i >= 0; i--) {
			char c = arr[i];
			if (isNewLine(c)) {
				w = 0;
			} else {
				w += getCharacterWidth(font, font.getCharacterData(c), c);
			}

			if (w > maxWidth) {
				isCropped = true;
				break;
			}

			start = i;
		}

		if (isCropped) {
			w = 0;
			w += getCharacterWidth(font, font.getCharacterData('.'), '.') * 3;

			for (int i = arr.length - 1; i >= 0; i--) {
				char c = arr[i];
				if (isNewLine(c)) {
					w = 0;
				} else {
					w += getCharacterWidth(font, font.getCharacterData(c), c);
				}

				if (w > maxWidth) {
					break;
				}

				start = i;
			}
		}

		int len = end - start + 1;
		int arrLen = len;
		boolean dots = isCropped && appendDots;
		if (dots) {
			len += 3;
		}

		char[] dest = new char[len];
		System.arraycopy(arr, start, dest, dots ? 3 : 0, arrLen);

		if (dots) {
			dest[0] = '.';
			dest[1] = '.';
			dest[2] = '.';
		}

		return dest;
	}
}
