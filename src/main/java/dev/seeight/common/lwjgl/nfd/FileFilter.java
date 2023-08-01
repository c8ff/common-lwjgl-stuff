package dev.seeight.common.lwjgl.nfd;

/**
 * The instances of this class can be cached as they don't hold any NFD specific data (pointers, memory stacks, etc).
 */
public class FileFilter {
    public final String description;
    public final String extensions;

    public FileFilter(String description, String extensions) {
        this.description = description;
        this.extensions = extensions;
    }
}
