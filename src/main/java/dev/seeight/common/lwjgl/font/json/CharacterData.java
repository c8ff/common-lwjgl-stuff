package dev.seeight.common.lwjgl.font.json;

public class CharacterData {
	public int x;
	public int y;
	public int width;
	public int height;
	public float originX;
	public float originY;
	public float advance;

	private double u;
	private double v;
	private double u2;
	private double v2;

	public CharacterData() {

	}

	public CharacterData(int width, int height, float originX, float originY, float advance, double u, double v, double u2, double v2, float scale) {
		this.width = width;
		this.height = height;
		this.originX = originX;
		this.originY = originY;
		this.advance = advance;
		this.u = u;
		this.v = v;
		this.u2 = u2;
		this.v2 = v2;

		this.renderWidth = this.width * scale;
		this.renderHeight = this.height * scale;
		this.renderOriginX = this.originX * scale;
		this.renderOriginY = this.originY * scale;
		this.renderAdvance = this.advance * scale;
	}

	public transient float renderWidth;
	public transient float renderHeight;
	public transient float renderOriginX;
	public transient float renderOriginY;
	public transient float renderAdvance;

	public void calcUV(double texWidth, double texHeight) {
		this.u = (texWidth - this.x) / texWidth;
		this.v = (texHeight - this.y) / texHeight;
		this.u2 = (texWidth - (this.x + this.width)) / texWidth;
		this.v2 = (texHeight - (this.y + this.height)) / texHeight;

		this.u = 1 - this.u;
		this.v = 1 - this.v;
		this.u2 = 1 - this.u2;
		this.v2 = 1 - this.v2;
	}

	public double u() {
		return u;
	}

	public double v() {
		return v;
	}

	public double u2() {
		return u2;
	}

	public double v2() {
		return v2;
	}
}
