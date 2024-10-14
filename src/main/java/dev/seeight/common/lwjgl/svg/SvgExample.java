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

package dev.seeight.common.lwjgl.svg;

import dev.seeight.common.lwjgl.Window;
import dev.seeight.renderer.renderer.Texture;
import dev.seeight.renderer.renderer.gl.OpenGLRenderer2;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.IOException;

public class SvgExample {
	public static void main(String[] args) {
		if (!GLFW.glfwInit()) {
			throw new RuntimeException();
		}

		Window window = new Window("SVG Example", 1280, 720);
		window.createWindow();

		GL.createCapabilities();

		OpenGLRenderer2 renderer = new OpenGLRenderer2(true);
		renderer.ortho(0, window.getWidth(), window.getHeight(), 0, 0, 10);

		window.setFramebufferSizeCallback((width, height) -> {
			GL11.glViewport(0, 0, width, height);
			renderer.ortho(0, width, height, 0, 0, 10);
		});

		Texture s;
		try (SVGRasterContext context = SVGRasterContext.create()) {
			s = context.glTexFromResource(SvgExample.class, "/test_like.svg", 24F);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		while (!window.shouldClose()) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

			renderer.frameStart();

			renderer.texRect2f(s, 0, 0, s.getWidth(), s.getHeight());

			renderer.frameEnd();

			window.swapBuffers();

			GLFW.glfwPollEvents();
		}
	}
}
