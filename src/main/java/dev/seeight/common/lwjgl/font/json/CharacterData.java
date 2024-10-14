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
