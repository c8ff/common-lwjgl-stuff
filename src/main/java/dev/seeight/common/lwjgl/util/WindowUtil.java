package dev.seeight.common.lwjgl.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

public class WindowUtil {
    public static void setMonitorMatch(long windowId, MonitorMatch monitorMatch) {
        GLFW.glfwSetWindowMonitor(windowId, monitorMatch.getId(), monitorMatch.getX(), monitorMatch.getY(), monitorMatch.getWidth(), monitorMatch.getHeight(), GLFW.GLFW_DONT_CARE);
    }

    // Ported to LWJGL, from https://stackoverflow.com/a/31526753
    public static MonitorMatch getHoverMonitor(long windowID) throws RuntimeException {
        int bestOverlap = 0;

        int[] f = new int[1];
        int[] f2 = new int[1];

        GLFW.glfwGetWindowPos(windowID, f, f2);

        int wx = f[0];
        int wy = f2[0];

        GLFW.glfwGetWindowSize(windowID, f, f2);

        int ww = f[0];
        int wh = f2[0];

        PointerBuffer monitors = GLFW.glfwGetMonitors();

        if (monitors == null) {
            throw new RuntimeException("Couldn't get monitors from GLFW.");
        }

        MonitorMatch monitorMatch = new MonitorMatch();

        while (monitors.hasRemaining()) {
            long monitor = monitors.get();

            GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);

            if (mode == null) {
                continue;
            }

            GLFW.glfwGetMonitorPos(monitor, f, f2);

            // monitor x, monitor y, monitor width, monitor height
            int mx = f[0];
            int my = f2[0];
            int mw = mode.width();
            int mh = mode.height();

            int overlap = Math.max(0, Math.min(wx + ww, mx + mw) - Math.max(wx, mx)) * Math.max(0, Math.min(wy + wh, my + mh) - Math.max(wy, my));

            if (bestOverlap < overlap) {
                bestOverlap = overlap;
                monitorMatch.overlap = overlap;
                monitorMatch.id = monitor;
                monitorMatch.mode = mode;
                monitorMatch.x = mx;
                monitorMatch.y = my;
                monitorMatch.width = mw;
                monitorMatch.height = mh;
                monitorMatch.changed = true;
            }
        }

        if (!monitorMatch.changed) {
            throw new RuntimeException("monitor match wasn't changed (this shouldn't happen)");
        }

        return monitorMatch;
    }

    public static class MonitorMatch {
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
