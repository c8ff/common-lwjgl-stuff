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

package dev.seeight.common.lwjgl.nfd;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NFDFilterItem;
import org.lwjgl.util.nfd.NFDPathSetEnum;
import org.lwjgl.util.nfd.NativeFileDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that contains simplified methods for the lwjgl-nfd library.<p>
 * Based on examples from <a href="https://github.com/LWJGL/lwjgl3/blob/master/modules/samples/src/test/java/org/lwjgl/demo/util/nfd/HelloNFD.java">here.</a>
 *
 * @author C8FF
 */
@SuppressWarnings("resource")
public class FileChooserUtil {
    /**
     * The initialization check for the current thread.
     */
    private static final ThreadLocal<Boolean> initialized = ThreadLocal.withInitial(() -> false);

    /**
     * Initializes NFD. This is required for
     */
    public static void initialize() {
        // The docs don't really specify what to do with the return value, so... /shrug
        if (!initialized.get()) {
            NativeFileDialog.NFD_Init();
            initialized.set(true);
        }
    }

    public static void quit() {
        // The docs don't really specify what to do with the return value, so... /shrug
        if (initialized.get()) {
            NativeFileDialog.NFD_Quit();
            initialized.set(false);
        }
    }

    /**
     * Opens the folder chooser. Locks the current thread until this dialog closes.
     *
     * @return The specified path, null if the user clicked cancel.
     */
    @Nullable
    public static String openFolderChooser(@Nullable CharSequence defaultPath) throws NFDException {
        assertInitialized();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer pp = stack.mallocPointer(1);

            switch (NativeFileDialog.NFD_PickFolder(pp, defaultPath)) {
                case NativeFileDialog.NFD_OKAY -> {
                    String str = pp.getStringUTF8(0);
                    NativeFileDialog.NFD_FreePath(pp.get(0));
                    return str;
                }
                case NativeFileDialog.NFD_CANCEL -> {
                    return null;
                }
                default -> throw new NFDException(NativeFileDialog.NFD_GetError());
            }
        }
    }

    /**
     * Opens the OS file chooser.
     *
     * @param defaultPath The default path that the dialog will 'open' at.
     * @param filtersIn   The desired file filters. The entry 'Any File (*.*)' will always be added even if {@code filtersIn} is null.
     * @return The selected file, or null if the user clicked cancel.
     */
    @Nullable
    public static String openFileChooser(@Nullable CharSequence defaultPath, FileFilter... filtersIn) throws NFDException {
        assertInitialized();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            NFDFilterItem.Buffer filters = fromFileFilters(stack, filtersIn);
            PointerBuffer pp = stack.mallocPointer(1);

            switch (NativeFileDialog.NFD_OpenDialog(pp, filters, defaultPath)) {
                case NativeFileDialog.NFD_OKAY -> {
                    String str = pp.getStringUTF8(0);
                    NativeFileDialog.NFD_FreePath(pp.get(0));
                    return str;
                }
                case NativeFileDialog.NFD_CANCEL -> {
                    return null;
                }
                default -> throw new NFDException(NativeFileDialog.NFD_GetError());
            }
        }
    }

    /**
     * Opens the OS multiple file chooser.
     *
     * @param defaultPath The default path that the dialog will 'open' at.
     * @param filtersIn   The desired file filters. The entry 'Any File (*.*)' will always be added even if {@code filtersIn} is null.
     * @return A list of the selected files, or null if the user clicked cancel.
     */
    @Nullable
    public static List<String> openMultipleFileChooser(@Nullable CharSequence defaultPath, FileFilter... filtersIn) throws NFDException {
        assertInitialized();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            NFDFilterItem.Buffer filters = fromFileFilters(stack, filtersIn);
            PointerBuffer pp = stack.mallocPointer(1);

            switch (NativeFileDialog.NFD_OpenDialogMultiple(pp, filters, defaultPath)) {
                case NativeFileDialog.NFD_OKAY -> {
                    // the api is very human
                    // very easy to use
                    long pathSet = pp.get(0);
                    NFDPathSetEnum psEnum = NFDPathSetEnum.calloc(stack);

                    NativeFileDialog.NFD_PathSet_GetEnum(pathSet, psEnum);

                    List<String> paths = new ArrayList<>();
                    while (NativeFileDialog.NFD_PathSet_EnumNext(psEnum, pp) == NativeFileDialog.NFD_OKAY && pp.get(0) != MemoryUtil.NULL) {
                        paths.add(pp.getStringUTF8(0));
                        NativeFileDialog.NFD_PathSet_FreePath(pp.get(0));
                    }

                    NativeFileDialog.NFD_PathSet_FreeEnum(psEnum);
                    NativeFileDialog.NFD_PathSet_Free(pathSet);
                    return paths;
                }
                case NativeFileDialog.NFD_CANCEL -> {
                    return null;
                }
                default -> // NFD_ERROR
                        throw new NFDException(NativeFileDialog.NFD_GetError());
            }
        }
    }

    /**
     * Opens the OS multiple file saver.
     *
     * @param defaultPath The default path that the dialog will 'open' at.
     * @param defaultName The default name that the file will have.
     * @param filtersIn   The desired file filters. The entry 'Any File (*.*)' will always be added even if {@code filtersIn} is null.
     * @return A list of the saved files, or null if the user clicked cancel.
     */
    @Nullable
    public static String openSaveChooser(@Nullable CharSequence defaultPath, @Nullable CharSequence defaultName, FileFilter... filtersIn) throws NFDException {
        assertInitialized();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            NFDFilterItem.Buffer filters = fromFileFilters(stack, filtersIn);
            PointerBuffer pp = stack.mallocPointer(1);

            switch (NativeFileDialog.NFD_SaveDialog(pp, filters, defaultPath, defaultName)) {
                case NativeFileDialog.NFD_OKAY -> {
                    String path = pp.getStringUTF8(0);
                    NativeFileDialog.NFD_FreePath(pp.get(0));
                    return path;
                }
                case NativeFileDialog.NFD_CANCEL -> {
                    return null;
                }
                default -> throw new NFDException(NativeFileDialog.NFD_GetError());
            }
        }
    }

    /**
     * Converts {@link FileFilter} to a {@link NFDFilterItem.Buffer}.
     */
    @Contract("_, null -> null")
    @Nullable
    public static NFDFilterItem.Buffer fromFileFilters(MemoryStack stack, FileFilter @Nullable [] filters) {
        if (filters == null) {
            return null;
        }

        if (filters.length == 0) {
            return null;
        }

        // Allocate the filters.
        NFDFilterItem.Buffer outFilters = NFDFilterItem.malloc(filters.length);

        // Fill the previous allocation with the descriptions and extensions.
        for (int i = 0; i < filters.length; i++) {
            FileFilter filter = filters[i];

            outFilters.get(i)
                    .name(stack.UTF8(filter.description))
                    .spec(stack.UTF8(filter.extensions));
        }

        return outFilters;
    }

    /**
     * @throws NotInitializedException In the case of the thread not having a NFD context.
     */
    private static void assertInitialized() throws NotInitializedException {
        if (!initialized.get()) {
            throw new NotInitializedException("NFD is not initialized.");
        }
    }
}
