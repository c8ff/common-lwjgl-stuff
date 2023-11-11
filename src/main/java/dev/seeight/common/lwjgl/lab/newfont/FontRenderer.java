package dev.seeight.common.lwjgl.lab.newfont;

import dev.seeight.common.lwjgl.font.json.CharacterData;
import dev.seeight.renderer.renderer.Renderer;

/**
 * Immediate mode font renderer. It uses a {@link Renderer} instance
 * to render each character separately.
 *
 * @see BufferedFontRenderer A faster implementation.
 */
public class FontRenderer implements IFontRenderer {
	private final Renderer renderer;

	private float scaleX;
	private float scaleY;

	public FontRenderer(Renderer renderer) {
		this.renderer = renderer;

		this.scaleX = 1;
		this.scaleY = 1;
	}

	@Override
	public void drawChar(IFont font, CharacterData data, float x, float y) {
		float x1 = x + data.renderOriginX * this.scaleX;
		float y1 = y + data.renderOriginY * this.scaleY;
		float x2 = x1 + data.renderWidth * this.scaleX;
		float y2 = y1 + data.renderHeight * this.scaleY;
		renderer.texRect2f(font.getTexture(), x1, y1, x2, y2, (float) data.u(), (float) data.v(), (float) data.u2(), (float) data.v2());
	}

	@Override
	public void drawInvalidChar(IFont font, int codepoint, float x, float y) {

	}

	@Override
	public float getScaleX() {
		return scaleX;
	}

	@Override
	public void setScaleX(float scaleX) {
		this.scaleX = scaleX;
	}

	@Override
	public float getScaleY() {
		return scaleY;
	}

	@Override
	public void setScaleY(float scaleY) {
		this.scaleY = scaleY;
	}
}
