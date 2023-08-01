package dev.seeight.common.lwjgl;

import dev.seeight.common.lwjgl.util.IOUtil;
import dev.seeight.common.lwjgl.util.WindowUtil;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.*;
import org.lwjgl.stb.STBImage;

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

	protected WindowUtil.MonitorMatch previousMonitor;
	protected int previousWidth;
	protected int previousHeight;

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

	public Window setTransparentFramebuffer(boolean transparentFramebuffer) {
		if (this.transparentFramebuffer != transparentFramebuffer) {
			if (this.initialized) {
				GLFW.glfwSetWindowAttrib(this.windowID, GLFW.GLFW_TRANSPARENT_FRAMEBUFFER, transparentFramebuffer ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
			}
		}

		this.transparentFramebuffer = transparentFramebuffer;

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

	public void setSizeVariables(int width, int height) {
		this.width = width;
		this.height = height;
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
				this.title = title;
			}
		}

		return this;
	}

	public void setCharCallback(GLFWCharCallback callback) {
		GLFW.glfwSetCharCallback(this.windowID, callback);
	}

	public void setKeyCallback(GLFWKeyCallback callback) {
		GLFW.glfwSetKeyCallback(this.windowID, callback);
	}

	public void setCursorPosCallback(GLFWCursorPosCallback callback) {
		GLFW.glfwSetCursorPosCallback(this.windowID, callback);
	}

	public void setScrollCallback(GLFWScrollCallback callback) {
		GLFW.glfwSetScrollCallback(this.windowID, callback);
	}

	public void setWindowCloseCallback(GLFWWindowCloseCallback callback) {
		GLFW.glfwSetWindowCloseCallback(this.windowID, callback);
	}

	public void setMouseButtonCallback(GLFWMouseButtonCallback callback) {
		GLFW.glfwSetMouseButtonCallback(this.windowID, callback);
	}

	public void setWindowFocusCallback(GLFWWindowFocusCallback callback) {
		GLFW.glfwSetWindowFocusCallback(this.windowID, callback);
	}

	public void setFramebufferSizeCallback(GLFWFramebufferSizeCallback callback) {
		GLFW.glfwSetFramebufferSizeCallback(this.windowID, callback);
	}

	public void setWindowSizeCallback(GLFWWindowSizeCallback glfwWindowSizeCallback) {
		GLFW.glfwSetWindowSizeCallback(this.windowID, glfwWindowSizeCallback);
	}

	public void setWindowIconifyCallback(GLFWWindowIconifyCallback glfwWindowIconifyCallback) {
		GLFW.glfwSetWindowIconifyCallback(this.windowID, glfwWindowIconifyCallback);
	}

	public void setDropCallback(GLFWDropCallback glfwDropCallback) {
		GLFW.glfwSetDropCallback(this.windowID, glfwDropCallback);
	}

	public void setWindowContentScaleCallback(GLFWWindowContentScaleCallback glfwWindowContentScaleCallback) {
		GLFW.glfwSetWindowContentScaleCallback(this.windowID, glfwWindowContentScaleCallback);
	}

	public void setCursorEnterCallback(GLFWCursorEnterCallback glfwCursorEnterCallback) {
		GLFW.glfwSetCursorEnterCallback(this.windowID, glfwCursorEnterCallback);
	}

	public void setWindowMaximizeCallback(GLFWWindowMaximizeCallback glfwWindowMaximizeCallback) {
		GLFW.glfwSetWindowMaximizeCallback(this.windowID, glfwWindowMaximizeCallback);
	}

	public void setWindowRefreshCallback(GLFWWindowRefreshCallback glfwWindowRefreshCallback) {
		GLFW.glfwSetWindowRefreshCallback(this.windowID, glfwWindowRefreshCallback);
	}

	public void setWindowPosCallback(GLFWWindowPosCallback glfwWindowPosCallback) {
		GLFW.glfwSetWindowPosCallback(this.windowID, glfwWindowPosCallback);
	}

	public void setCharModsCallback(GLFWCharModsCallback glfwCharModsCallback) {
		GLFW.glfwSetCharModsCallback(this.windowID, glfwCharModsCallback);
	}

	protected void glfwWindowHint(int name, boolean value) {
		GLFW.glfwWindowHint(name, value ? GLFW.GLFW_TRUE : GLFW.GLFW_FALSE);
	}

	public void setFullscreen(boolean fullscreen) {
		if (this.initialized && this.fullscreen != fullscreen) {
			if (fullscreen) {
				try {
					WindowUtil.MonitorMatch monitorMatch = WindowUtil.getHoverMonitor(this.windowID);
					this.previousWidth = this.getWidth();
					this.previousHeight = this.getHeight();
					WindowUtil.setMonitorMatch(this.windowID, monitorMatch);
					this.previousMonitor = monitorMatch;
				} catch (Exception e) {
					System.err.print("Couldn't set fullscreen. ");
					e.printStackTrace();
				}
			} else {
				WindowUtil.MonitorMatch m = previousMonitor;
				GLFW.glfwSetWindowMonitor(this.windowID,
						0,
						m.getX() + (m.getWidth() - previousWidth) / 2,
						m.getY() + (m.getHeight() - previousHeight) / 2,
						previousWidth,
						previousHeight,
						GLFW.GLFW_DONT_CARE
				);
			}
		}

		this.fullscreen = fullscreen;
	}

	public void setIcon(String name) throws IOException {
		InputStream stream = Window.class.getResourceAsStream(name);
		if (stream == null) throw new NullPointerException("Couldn't find file " + name + ".");
		this.setIcon(stream);
	}

	public void setIcon(InputStream stream) throws IOException {
		if (stream == null) {
			throw new NullPointerException("stream");
		}

		GLFWImage image = GLFWImage.malloc();
		GLFWImage.Buffer imagebf = GLFWImage.malloc(1);

		IntBuffer width = BufferUtils.createIntBuffer(1);
		IntBuffer height = BufferUtils.createIntBuffer(1);
		IntBuffer comp = BufferUtils.createIntBuffer(1);

		ByteBuffer data = STBImage.stbi_load_from_memory(IOUtil.byteBufferFrom(stream), width, height, comp, 4);
		if (data == null) throw new NullPointerException("data = null");

		image.set(width.get(), height.get(), data);
		imagebf.put(0, image);
		GLFW.glfwSetWindowIcon(this.windowID, imagebf);
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

	public CharSequence getTitle() {
		return title;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public boolean isFullscreen() {
		return fullscreen;
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
