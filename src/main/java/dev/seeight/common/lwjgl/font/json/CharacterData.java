package dev.seeight.common.lwjgl.font.json;

public class CharacterData {
	public int x;
	public int y;
	public int width;
	public int height;
	public int originX;
	public int originY;
	public int advance;

	private double u;
	private double v;
	private double u2;
	private double v2;

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
