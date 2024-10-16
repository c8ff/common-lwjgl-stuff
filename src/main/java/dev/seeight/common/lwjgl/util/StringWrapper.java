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

package dev.seeight.common.lwjgl.util;

import dev.seeight.common.lwjgl.font.FontRenderer;
import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.font.IFont;
import dev.seeight.common.lwjgl.fontrenderer.IFontRenderer;
import org.jetbrains.annotations.Nullable;

public class StringWrapper {
    @Deprecated
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

    @Deprecated
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
                width += (int) characterData.advance;
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
                        extraWidth += (int) cd.advance;
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
            } else if (width > maxWidth) {
                width = 0;
                builder.append('-').append('\n').append(c);
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }

    public static String wrapString(IFont font, final String string, final float maxWidth) {
        return wrapString(font, null, string, maxWidth);
    }

    public static String wrapString(IFont font, @Nullable IFontRenderer fontRenderer, final String string, final float maxWidth) {
        StringBuilder builder = new StringBuilder();
        float width = 0;
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
            if (fontRenderer != null) {
                width += fontRenderer.getCharacterWidth(font, characterData, c);
            } else if (characterData != null) {
	            width += characterData.width;
            }

            // Check if it should wrap the string
            if (c == ' ') {
                float extraWidth = 0;
                int i1 = i + 1;

                // Calculate the word's width (or the width until the next ' ' character)
                for (; i1 < chars.length; i1++) {
                    char c1 = chars[i1];

                    CharacterData cd = font.getCharacterData(c1);
                    if (fontRenderer != null) {
                        extraWidth += fontRenderer.getCharacterWidth(font, cd, c1);
                    } else if (cd != null) {
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
            } else if (width > maxWidth && c != ',' && c != '.' && c != ';' && c != ':' && c != '!' && c != '?') {
                width = 0;
                builder.append('-').append('\n').append(c);
            } else {
                builder.append(c);
            }
        }

        return builder.toString();
    }
}
