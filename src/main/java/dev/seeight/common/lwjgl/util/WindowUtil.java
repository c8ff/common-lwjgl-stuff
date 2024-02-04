package dev.seeight.common.lwjgl.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

public class WindowUtil {
	public static void setMonitor(long windowId, Monitor monitor) {
		GLFW.glfwSetWindowMonitor(windowId, monitor.getId(), monitor.getX(), monitor.getY(), monitor.getWidth(), monitor.getHeight(), GLFW.GLFW_DONT_CARE);
	}

	// Ported to LWJGL, from https://stackoverflow.com/a/31526753
	public static Monitor getHoverMonitor(long windowID) throws RuntimeException {
		try (MemoryStack stack = MemoryStack.stackPush()) {
			IntBuffer b0 = stack.mallocInt(1);
			IntBuffer b1 = stack.mallocInt(1);

			GLFW.glfwGetWindowPos(windowID, b0, b1);
			int x = b0.get();
			int y = b1.get();

			GLFW.glfwGetWindowSize(windowID, b0.clear(), b1.clear());
			int width = b0.get();
			int height = b1.get();

			PointerBuffer monitors = GLFW.glfwGetMonitors();

			if (monitors == null) {
				throw new RuntimeException("No monitors from GLFW.");
			}

			int bestOverlap = 0;
			Monitor result = new Monitor();
			while (monitors.hasRemaining()) {
				long monitor = monitors.get();
				GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);

				if (mode == null) {
					continue;
				}

				GLFW.glfwGetMonitorPos(monitor, b0.clear(), b1.clear());

				int monitorX = b0.get();
				int monitorY = b1.get();
				int monitorWidth = mode.width();
				int monitorHeight = mode.height();

				int overlap = Math.max(0, Math.min(x + width, monitorX + monitorWidth) - Math.max(x, monitorX)) * Math.max(0, Math.min(y + height, monitorY + monitorHeight) - Math.max(y, monitorY));

				if (bestOverlap < overlap) {
					bestOverlap = overlap;
					result.overlap = overlap;
					result.id = monitor;
					result.mode = mode;
					result.x = monitorX;
					result.y = monitorY;
					result.width = monitorWidth;
					result.height = monitorHeight;
					result.changed = true;
				}
			}

			if (!result.changed) {
				throw new RuntimeException("Couldn't find a matching monitor for window: " + windowID);
			}

			return result;
		}
	}

	public static class Monitor {
		private GLFWVidMode mode;
		private int x;
		private int y;
		private int width;
		private int height;
		private long id;
		private int overlap;

		private boolean changed;

		public GLFWVidMode getMode() {
			return mode;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}

		public long getId() {
			return id;
		}

		public int getOverlap() {
			return overlap;
		}

		public boolean isChanged() {
			return changed;
		}
	}
}
