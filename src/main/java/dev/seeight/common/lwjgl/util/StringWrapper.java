package dev.seeight.common.lwjgl.util;

import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.font.IFont;

public class StringWrapper {
    public static void drawCenteredString(FontRenderer font, final char[] chars, double x, double y) {
        double startX = x;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            CharacterData data = font.getCharacterData(c);

            // Calculate x centered position, based on a new line (or if i is 0)
            if (c == '\n' || i == 0) {
                if (i != 0) {
                    y += font.FONT_HEIGHT_FLOAT * font.scaleY;
                }

                // Get the current line index range
                int j = i + 1;
                for (; j < chars.length; j++) {
                    if (chars[j] == '\n') {
                        break;
                    }
                }

                // Get the current line width
                float currentLineWidth = font.getWidthFloat(chars, i, j);

                // Center the x position
                x = startX - currentLineWidth / 2f;

                if (i != 0) {
                    continue;
                }
            }

            // Render the character
            if (data != null) {
                font.drawChar(data, x, y);
                x += data.advance * font.scaleX;
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    public static String wrapString(FontRenderer font, final String string, final int maxWidth) {
        StringBuilder builder = new StringBuilder();
        int width = 0;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Reset for every new line, as it is already 'wrapped'.
            if (c == '\n') {
                builder.append(c);
                width = 0;
                continue;
            }

            // Add the current character's advance
            CharacterData characterData = font.getCharacterData(c);
            if (characterData != null) {
                width += characterData.advance;
            }

            // Check if it should wrap the string
            if (c == ' ') {
                int extraWidth = 0;
                int i1 = i;

                // Calculate the word's width (or the width to the next ' ' character)
                for (; i1 < chars.length; i1++) {
                    char c1 = chars[i1];

                    CharacterData cd = font.getCharacterData(c);
                    if (cd != null) {
                        extraWidth += cd.advance;
                    }

                    if (c1 == ' ' || width + extraWidth > maxWidth) {
                        break;
                    }
                }

                // Append '\n' in case the word's width is larger than the specified amount.
                if (width + extraWidth > maxWidth) {
                    width = 0;
                    builder.append('\n');
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    public static String wrapString(IFont font, final String string, final int maxWidth) {
        StringBuilder builder = new StringBuilder();
        int width = 0;
        char[] chars = string.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];

            // Reset for every new line, as it is already 'wrapped'.
            if (c == '\n') {
                builder.append(c);
                width = 0;
                continue;
            }

            // Add the current character's advance
            CharacterData characterData = font.getCharacterData(c);
            if (characterData != null) {
                width += characterData.advance;
            }

            // Check if it should wrap the string
            if (c == ' ') {
                int extraWidth = 0;
                int i1 = i;

                // Calculate the word's width (or the width to the next ' ' character)
                for (; i1 < chars.length; i1++) {
                    char c1 = chars[i1];

                    CharacterData cd = font.getCharacterData(c);
                    if (cd != null) {
                        extraWidth += cd.advance;
                    }

                    if (c1 == ' ' || width + extraWidth > maxWidth) {
                        break;
                    }
                }

                // Append '\n' in case the word's width is larger than the specified amount.
                if (width + extraWidth > maxWidth) {
                    width = 0;
                    builder.append('\n');
                } else {
                    builder.append(c);
                }
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
