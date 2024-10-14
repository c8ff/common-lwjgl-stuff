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

package dev.seeight.common.lwjgl;

import dev.seeight.common.lwjgl.util.IOUtil;
import dev.seeight.common.lwjgl.util.WindowUtil;
import dev.seeight.common.lwjgl.window.*;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

/**
 * A wrapper for a window in GLFW
 *
 * @author C8FF
 */
public class Window {
	protected CharSequence title;
	protected int width;
	protected int height;
	protected int x;
	protected int y;
	protected boolean iconified = false;
	protected boolean cursorInside = false;
	protected boolean maximized = false;

	protected boolean resizable = true;
	protected boolean focused = false;
	protected boolean mousePassThrough = false;
	protected boolean decorated = true;
	protected boolean visible = true;
	protected boolean transparentFramebuffer = false;
	protected boolean alwaysOnTop = false;
	protected boolean fullscreen = false;

	protected int vSync = -1;

	protected boolean initialized = false;
	protected long windowID;

	protected WindowUtil.Monitor previousMonitor;
	protected int previousWidth;
	protected int previousHeight;

	/**
	 * The {@link #createWindow()} method must be called, or else the window won't be created.
	 */
	public Window(CharSequence title, int width, int height) {
		this.title = title;
		this.width = width;
		this.height = height;
	}

	public void createWindow() {
		this.createWindow(null);
	}

	public void createWindow(Window window) {
		this.glfwWindowHint(GLFW.GLFW_RESIZABLE, this.resizable);
		this.glfwWindowHint(GLFW.GLFW_FOCUSED, this.focused);
		this.glfwWindowHint(GLFW.GLFW_MOUSE_PASSTHROUGH, this.mousePassThrough);
		this.glfwWindowHint(GLFW.GLFW_DECORATED, this.decorated);
		this.glfwWindowHint(GLFW.GLFW_VISIBLE, false);
		this.glfwWindowHint(GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, this.transparentFramebuffer);
		this.glfwWindowHint(GLFW.GLFW_FLOATING, this.alwaysOnTop);
		this.glfwWindowHint(GLFW.GLFW_MAXIMIZED, this.maximized);

		long s = window != null && window.isInitialized() ? window.windowID : 0;
		this.windowID = GLFW.glfwCreateWindow(this.width, this.height, this.title, 0, s);

		if (this.windowID <= 0) {
			throw new RuntimeException("Window couldn't be created.");
		}

		this.initialized = true;

		// Make the OpenGL context current
		this.makeContextCurrent();
		// Enable v-sync
		GLFW.glfwSwapInterval(vSync);

		this.setCharCallback(null);
		this.setKeyCallback(null);
		this.setCursorPosCallback(null);
		this.setScrollCallback(null);
		this.setWindowCloseCallback(null);
		this.setMouseButtonCallback(null);
		this.setWindowFocusCallback(null);
		this.setFramebufferSizeCallback(null);
		this.setWindowSizeCallback(null);
		this.setWindowIconifyCallback(null);
		this.setDropCallback(null);
		this.setWindowContentScaleCallback(null);
		this.setCursorEnterCallback(null);
		this.setWindowMaximizeCallback(null);
		this.setWindowRefreshCallback(null);
		this.setWindowPosCallback(null);
		this.setCharModsCallback(null);

		// Make the window visible
		if (this.visible) {
			GLFW.glfwShowWindow(this.windowID);
		}
	}

	public void destroy() {
		// Free the window callbacks and destroy the window
		this.glfwFreeCallbacks();
		GLFW.glfwDestroyWindow(this.windowID);

		this.initialized = false;
		this.windowID = 0;
	}

	public void makeContextCurrent() {
		if (!this.initialized) {
			throw new RuntimeException("Window not initialized.");
		}

		GLFW.glfwMakeContextCurrent(this.windowID);
	}

	public void glfwFreeCallbacks() {
		Callbacks.glfwFreeCallbacks(this.windowID);
	}

	public void swapBuffers() {
		if (!this.initialized) {
			throw new RuntimeException("Window not initialized.");
		}

		GLFW.glfwSwapBuffers(this.windowID);
	}

	/**
	 * Only has an effect before the window is created.
	 */
	public Window setFocusedParam(boolean focused) {
		this.focused = focused;
		return this;
	}

	/**
	 * Only applies at window creation.
	 */
	public Window setTransparentFramebuffer(boolean transparentFramebuffer) {
		this.transparentFramebuffer = transparentFramebuffer;
		return this;
	}

	public Window setResizable(boolean resizable) {
		if (this.resizable != resizable) {
			if (this.initialized) {
				GLFW.glfwSetWindowAttrib(this.windowID, GLFW.GLFW_RESIZABLE, resizable ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
			}
		}
		this.resizable = resizable;

		return this;
	}

	public Window setFocused() {
		if (this.initialized) {
			if (GLFW.glfwGetWindowAttrib(this.windowID, GLFW.GLFW_FOCUSED) == GLFW.GLFW_FALSE) {
				GLFW.glfwFocusWindow(this.windowID);
			}
		}

		return this;
	}

	public Window setIconified(boolean iconified) {
		if (this.initialized) {
			if (GLFW.glfwGetWindowAttrib(this.windowID, GLFW.GLFW_ICONIFIED) == GLFW.GLFW_TRUE) {
				if (!iconified) {
					GLFW.glfwRestoreWindow(this.windowID);
				}
			} else {
				if (iconified) {
					GLFW.glfwIconifyWindow(this.windowID);
				}
			}
		}

		return this;
	}

	public Window maximizeWindow() {
		if (this.initialized) {
			GLFW.glfwMaximizeWindow(this.windowID);
		}

		return this;
	}

	public Window restoreWindow() {
		if (this.initialized) {
			GLFW.glfwRestoreWindow(this.windowID);
		}

		return this;
	}

	public Window requestWindowAttention() {
		if (this.initialized) {
			GLFW.glfwRequestWindowAttention(this.windowID);
		}

		return this;
	}

	public Window setCursor(int cursor) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetCursor(this.windowID, cursor);
		return this;
	}

	public Window setCursorPos(double xpos, double ypos) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetCursorPos(this.windowID, xpos, ypos);
		return this;
	}

	public Window setGamma(float gamma) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetGamma(this.windowID, gamma);
		return this;
	}

	public Window setInputMode(int mode, int value) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetInputMode(this.windowID, mode, value);
		return this;
	}

	public Window setWindowOpacity(float opacity) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetWindowOpacity(this.windowID, opacity);
		return this;
	}

	public Window setWindowSizeLimits(int minWidth, int minHeight, int maxWidth, int maxHeight) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetWindowSizeLimits(this.windowID, minWidth, minHeight, maxWidth, maxHeight);
		return this;
	}

	public Window setWindowAspectRatio(int numerator, int denominator) {
		if (!this.initialized) {
			return this;
		}

		GLFW.glfwSetWindowAspectRatio(this.windowID, numerator, denominator);
		return this;
	}

	public Window setMousePassThough(boolean mousePassThough) {
		this.mousePassThrough = mousePassThough;

		return this;
	}

	public Window setDecorated(boolean decorated) {
		if (this.decorated != decorated) {
			if (this.initialized) {
				GLFW.glfwSetWindowAttrib(this.windowID, GLFW.GLFW_DECORATED, decorated ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
			}
		}

		this.decorated = decorated;

		return this;
	}

	public Window setMaximized(boolean maximized) {
		if (this.initialized) {
			if (this.maximized != maximized) {
				if (maximized) {
					GLFW.glfwMaximizeWindow(this.windowID);
				} else {
					GLFW.glfwRestoreWindow(this.windowID);
				}
			}

		}

		this.maximized = maximized;
		return this;
	}

	public Window setAlwaysOnTop(boolean alwaysOnTop) {
		if (this.alwaysOnTop != alwaysOnTop) {
			if (this.initialized) {
				GLFW.glfwSetWindowAttrib(this.windowID, GLFW.GLFW_FLOATING, alwaysOnTop ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
			}
		}

		this.alwaysOnTop = alwaysOnTop;

		return this;
	}

	public Window setVisible(boolean visible) {
		if (this.visible != visible) {
			if (this.initialized) {
				if (visible) {
					GLFW.glfwShowWindow(this.windowID);
				} else {
					GLFW.glfwHideWindow(this.windowID);
				}
			}
		}
		this.visible = visible;

		return this;
	}

	public Window setVSync(int vSync) {
		this.vSync = vSync;

		if (this.initialized) {
			GLFW.glfwSwapInterval(vSync);
		}

		return this;
	}

	public Window setSize(int width, int height) {
		this.width = width;
		this.height = height;

		if (this.initialized) {
			GLFW.glfwSetWindowSize(this.windowID, width, height);
		}

		return this;
	}

	public Window setPosition(int x, int y) {
		if (this.initialized) {
			this.x = x;
			this.y = y;
			GLFW.glfwSetWindowPos(this.windowID, x, y);
		}

		return this;
	}

	public void setShouldClose(boolean shouldClose) {
		if (this.initialized) {
			GLFW.glfwSetWindowShouldClose(this.windowID, shouldClose);
		}
	}

	public Window setTitle(CharSequence title) {
		if (this.initialized) {
			if (!this.title.equals(title)) {
				GLFW.glfwSetWindowTitle(this.windowID, title);
			}
		}
		this.title = title;

		return this;
	}

	public void setCharCallback(@Nullable CharCallback callback) {
		//noinspection resource
		GLFW.glfwSetCharCallback(this.windowID, callback == null ? null : new GLFWCharCallback() {
			@Override
			public void invoke(long window, int codepoint) {
				callback.invoke(codepoint);
			}
		});
	}

	public void setKeyCallback(@Nullable KeyCallback callback) {
		//noinspection resource
		GLFW.glfwSetKeyCallback(this.windowID, callback == null ? null : new GLFWKeyCallback() {
			@Override
			public void invoke(long window, int key, int scancode, int action, int mods) {
				callback.invoke(key, scancode, action, mods);
			}
		});
	}

	public void setCursorPosCallback(@Nullable CursorPosCallback callback) {
		//noinspection resource
		GLFW.glfwSetCursorPosCallback(this.windowID, callback == null ? null : new GLFWCursorPosCallback() {
			@Override
			public void invoke(long window, double xpos, double ypos) {
				callback.invoke(xpos, ypos);
			}
		});
	}

	public void setScrollCallback(@Nullable ScrollCallback callback) {
		//noinspection resource
		GLFW.glfwSetScrollCallback(this.windowID, callback == null ? null : new GLFWScrollCallback() {
			@Override
			public void invoke(long window, double xoffset, double yoffset) {
				callback.invoke(xoffset, yoffset);
			}
		});
	}

	public void setWindowCloseCallback(@Nullable WindowCloseCallback callback) {
		//noinspection resource
		GLFW.glfwSetWindowCloseCallback(this.windowID, callback == null ? null : new GLFWWindowCloseCallback() {
			@Override
			public void invoke(long window) {
				callback.invoke();
			}
		});
	}

	public void setMouseButtonCallback(@Nullable MouseButtonCallback callback) {
		//noinspection resource
		GLFW.glfwSetMouseButtonCallback(this.windowID, callback == null ? null : new GLFWMouseButtonCallback() {
			@Override
			public void invoke(long window, int button, int action, int mods) {
				callback.invoke(button, action, mods);
			}
		});
	}

	public void setWindowFocusCallback(@Nullable WindowFocusCallback callback) {
		WindowFocusCallback bgDefault = focused -> this.focused = focused;

		//noinspection resource
		GLFW.glfwSetWindowFocusCallback(this.windowID, callback == null ? new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				bgDefault.invoke(focused);
			}
		} : new GLFWWindowFocusCallback() {
			@Override
			public void invoke(long window, boolean focused) {
				callback.invoke(focused);
			}
		});
	}

	public void setFramebufferSizeCallback(@Nullable FramebufferSizeCallback callback) {
		FramebufferSizeCallback bgCallback = (width, height) -> {
			this.width = width;
			this.height = height;
		};

		//noinspection resource
		GLFW.glfwSetFramebufferSizeCallback(this.windowID, callback != null ? new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				bgCallback.invoke(width, height);
				callback.invoke(width, height);
			}
		} : new GLFWFramebufferSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				bgCallback.invoke(width, height);
			}
		});
	}

	public void setWindowSizeCallback(@Nullable WindowSizeCallback callback) {
		WindowSizeCallback bgCallback = (width, height) -> {
			this.width = width;
			this.height = height;
		};

		//noinspection resource
		GLFW.glfwSetWindowSizeCallback(this.windowID, callback != null ? new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				bgCallback.invoke(width, height);
				callback.invoke(width, height);
			}
		} : new GLFWWindowSizeCallback() {
			@Override
			public void invoke(long window, int width, int height) {
				bgCallback.invoke(width, height);
			}
		});
	}

	public void setWindowIconifyCallback(@Nullable WindowIconifyCallback callback) {
		WindowIconifyCallback bgCallback = iconify -> this.iconified = iconify;

		//noinspection resource
		GLFW.glfwSetWindowIconifyCallback(this.windowID, callback != null ? new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, boolean iconified) {
				bgCallback.invoke(iconified);
				callback.invoke(iconified);
			}
		} : new GLFWWindowIconifyCallback() {
			@Override
			public void invoke(long window, boolean iconified) {
				bgCallback.invoke(iconified);
			}
		});
	}

	public void setDropCallback(@Nullable DropCallback callback) {
		//noinspection resource
		GLFW.glfwSetDropCallback(this.windowID, callback == null ? null : new GLFWDropCallback() {
			@Override
			public void invoke(long window, int count, long names) {
				PointerBuffer charPointers = MemoryUtil.memPointerBuffer(names, count);
				String[] out = new String[count];
				for (int i = 0; i < count; i++) {
					out[i] = MemoryUtil.memUTF8(charPointers.get(i));
				}
				callback.invoke(out);
			}
		});
	}

	public void setWindowContentScaleCallback(WindowContentScaleCallback callback) {
		//noinspection resource
		GLFW.glfwSetWindowContentScaleCallback(this.windowID, callback == null ? null : new GLFWWindowContentScaleCallback() {
			@Override
			public void invoke(long window, float xscale, float yscale) {
				callback.invoke(xscale, yscale);
			}
		});
	}

	public void setCursorEnterCallback(CursorEnterCallback callback) {
		CursorEnterCallback bgCallback = c -> this.cursorInside = c;

		//noinspection resource
		GLFW.glfwSetCursorEnterCallback(this.windowID, callback != null ? new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, boolean entered) {
				bgCallback.invoke(entered);
				callback.invoke(entered);
			}
		} : new GLFWCursorEnterCallback() {
			@Override
			public void invoke(long window, boolean entered) {
				bgCallback.invoke(entered);
			}
		});
	}

	public void setWindowMaximizeCallback(WindowMaximizeCallback callback) {
		WindowMaximizeCallback bgDefault = maximized -> this.maximized = maximized;

		//noinspection resource
		GLFW.glfwSetWindowMaximizeCallback(this.windowID, callback == null ? new GLFWWindowMaximizeCallback() {
			@Override
			public void invoke(long window, boolean maximized) {
				bgDefault.invoke(maximized);
			}
		} : new GLFWWindowMaximizeCallback() {
			@Override
			public void invoke(long window, boolean maximized) {
				bgDefault.invoke(maximized);
				callback.invoke(maximized);
			}
		});
	}

	public void setWindowRefreshCallback(@Nullable WindowRefreshCallback callback) {
		//noinspection resource
		GLFW.glfwSetWindowRefreshCallback(this.windowID, callback == null ? null : new GLFWWindowRefreshCallback() {
			@Override
			public void invoke(long window) {
				callback.invoke();
			}
		});
	}

	public void setWindowPosCallback(@Nullable WindowPosCallback callback) {
		WindowPosCallback bgCallback = (x, y) -> {
			this.x = x;
			this.y = y;
		};

		//noinspection resource
		GLFW.glfwSetWindowPosCallback(this.windowID, callback != null ? new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xpos, int ypos) {
				bgCallback.invoke(xpos, ypos);
				callback.invoke(xpos, ypos);
			}
		} : new GLFWWindowPosCallback() {
			@Override
			public void invoke(long window, int xpos, int ypos) {
				bgCallback.invoke(xpos, ypos);
			}
		});
	}

	public void setCharModsCallback(CharModsCallback callback) {
		//noinspection resource
		GLFW.glfwSetCharModsCallback(this.windowID, callback == null ? null : new GLFWCharModsCallback() {
			@Override
			public void invoke(long window, int codepoint, int mods) {
				callback.invoke(codepoint, mods);
			}
		});
	}

	protected void glfwWindowHint(int name, boolean value) {
		GLFW.glfwWindowHint(name, value ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
	}

	public WindowUtil.Monitor getHoverMonitor() {
		return WindowUtil.getHoverMonitor(this.windowID);
	}

	public void getCursorPos(double[] x, double[] y) {
		GLFW.glfwGetCursorPos(this.windowID, x, y);
	}

	public void setFullscreen(boolean fullscreen) {
		if (this.initialized && this.fullscreen != fullscreen) {
			if (fullscreen) {
				try {
					WindowUtil.Monitor monitor = WindowUtil.getHoverMonitor(this.windowID);
					this.previousWidth = this.getWidth();
					this.previousHeight = this.getHeight();
					WindowUtil.setMonitor(this.windowID, monitor);
					this.previousMonitor = monitor;
				} catch (Exception e) {
					System.err.print("Couldn't set fullscreen. ");
					e.printStackTrace();
					return; // prevent setting full screen if failed.
				}
			} else {
				WindowUtil.Monitor m = previousMonitor;

				if (m == null) {
					GLFW.glfwSetWindowMonitor(this.windowID,
							0L,
							10,
							10,
							previousWidth,
							previousHeight,
							GLFW.GLFW_DONT_CARE
					);
				} else {
					GLFW.glfwSetWindowMonitor(this.windowID,
							0L,
							m.getX() + (m.getWidth() - previousWidth) / 2,
							m.getY() + (m.getHeight() - previousHeight) / 2,
							previousWidth,
							previousHeight,
							GLFW.GLFW_DONT_CARE
					);
				}
			}
		}

		this.fullscreen = fullscreen;
	}

	/**
	 * Sets the icon of the window to the contents of the resource {@code name}.
	 */
	public void setIcon(String name) throws IOException {
		InputStream stream = Window.class.getResourceAsStream(name);
		if (stream == null) throw new NullPointerException("Couldn't find file " + name + ".");
		this.setIcon(stream);
	}

	/**
	 * Sets the icon of the window to the contents of the {@code stream}.
	 */
	public void setIcon(InputStream stream) throws IOException {
		if (stream == null) {
			throw new NullPointerException("stream");
		}

		try (MemoryStack stack = MemoryStack.stackPush()) {
			GLFWImage image = GLFWImage.malloc();
			GLFWImage.Buffer imagebf = GLFWImage.malloc(1);

			IntBuffer width = stack.mallocInt(1);
			IntBuffer height = stack.mallocInt(1);
			IntBuffer comp = stack.mallocInt(1);

			ByteBuffer data = STBImage.stbi_load_from_memory(IOUtil.byteBufferFrom(stream), width, height, comp, 4);
			if (data == null) throw new NullPointerException("data = null");

			image.set(width.get(), height.get(), data);
			imagebf.put(0, image);
			GLFW.glfwSetWindowIcon(this.windowID, imagebf);
		}
	}

	public boolean shouldClose() {
		return GLFW.glfwWindowShouldClose(this.windowID);
	}

	public int getInputMode(int mode) {
		return GLFW.glfwGetInputMode(this.windowID, mode);
	}

	public float getWindowOpacity() {
		return GLFW.glfwGetWindowOpacity(this.windowID);
	}

	public long getWindowMonitor() {
		return GLFW.glfwGetWindowMonitor(this.windowID);
	}

	public int getWindowAttrib(int attrib) {
		return GLFW.glfwGetWindowAttrib(this.windowID, attrib);
	}

	/**
	 * Gets the title of the window.
	 * @return The window title
	 */
	public CharSequence getTitle() {
		return title;
	}

	/**
	 * Gets the cached width of the window.
	 * @return The window width.
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * Gets the cached height of the window.
	 * @return The window height.
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * Gets the cached X position ({@link #x}) of the window.
	 * @return The window X position.
	 */
	public int getX() {
		return this.x;
	}

	/**
	 * Gets the cached Y position ({@link #y}) of the window.
	 * @return The window Y position.
	 */
	public int getY() {
		return this.y;
	}

	/**
	 * Gets the X position of the window directly from GLFW instead of {@link #x}.
	 * @return The window X position.
	 */
	public int grabX() {
		int x;
		try (MemoryStack s = MemoryStack.stackPush()) {
			IntBuffer xBuffer = s.mallocInt(1);
			GLFW.glfwGetWindowPos(this.windowID, xBuffer, null);
			x = xBuffer.get();
		}
		this.x = x;
		return x;
	}

	/**
	 * Gets the Y position of the window directly from GLFW instead of {@link #y}.
	 * @return The window Y position.
	 */
	public int grabY() {
		int y;
		try (MemoryStack s = MemoryStack.stackPush()) {
			IntBuffer yBuffer = s.mallocInt(1);
			GLFW.glfwGetWindowPos(this.windowID, null, yBuffer);
			y = yBuffer.get();
		}
		this.y = y;
		return y;
	}

	public boolean isIconified() {
		return iconified;
	}

	public boolean isFullscreen() {
		return fullscreen;
	}

	public boolean isFocused() {
		return focused;
	}

	public boolean isDecorated() {
		return decorated;
	}

	public boolean isResizable() {
		return resizable;
	}

	public boolean isMousePassThrough() {
		return mousePassThrough;
	}

	public boolean isVisible() {
		return visible;
	}

	public boolean isTransparentFramebuffer() {
		return transparentFramebuffer;
	}

	public boolean isAlwaysOnTop() {
		return alwaysOnTop;
	}

	public boolean isInitialized() {
		return initialized;
	}
}
