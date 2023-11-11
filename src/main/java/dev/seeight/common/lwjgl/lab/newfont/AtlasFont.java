package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.common.lwjgl.font.json.FontData;
import dev.seeight.renderer.renderer.Texture;

import java.util.HashMap;
import java.util.Map;

public class AtlasFont implements IFont {
	private final float size;
	private final Texture texture;
	private final FontData fontData;
	private final float ascent;
	private final float descent;
	private final Map<Integer, CharacterData> characters;

	public AtlasFont(Texture texture, FontData fontData, float scale) {
		this.texture = texture;
		this.fontData = fontData;
		this.size = this.fontData.size * scale;
		
		this.characters = new HashMap<>();

		float[] maxOriginY = new float[1];
		float[] maxDescent = new float[1];
		this.fontData.characters.forEach((s, c) -> {
			c.calcUV(texture.getWidth(), texture.getHeight());

			if (maxOriginY[0] < c.originY) {
				maxOriginY[0] = c.originY;
			}

			float descent = c.height - c.originY;
			if (maxDescent[0] < descent) {
				maxDescent[0] = descent;
			}

			c.renderWidth = c.width * scale;
			c.renderHeight = c.height * scale;
			c.renderOriginX = 0;
			c.renderOriginY = (fontData.size - c.originY) * scale;
			c.renderAdvance = (c.advance - c.width + c.originX) * scale;

			characters.put(s.codePointAt(0), c);
		});

		// TODO: is this correct?
		this.ascent = maxOriginY[0] * scale;
		this.descent = -maxDescent[0] * scale;
	}

	@Override
	public Texture getTexture() {
		return this.texture;
	}

	@Override
	public String getName() {
		return this.fontData.name;
	}

	@Override
	public int getSize() {
		return (int) this.size;
	}

	@Override
	public float getHeight() {
		return getSize();
	}

	@Override
	public float getLineGap() {
		return this.size;
	}

	@Override
	public float getAscent() {
		return this.ascent;
	}

	@Override
	public float getDescent() {
		return this.descent;
	}

	@Override
	public CharacterData getCharacterData(int codepoint) {
		return this.characters.get(codepoint);
	}
}
