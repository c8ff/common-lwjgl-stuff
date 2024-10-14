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

package dev.seeight.common.lwjgl.util;

import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.BufferUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;

public class IOUtil {
    @Contract("_, _ -> new")
    public static @NotNull InputStreamReader readerFrom(@NotNull Class<?> clazz, @NotNull String resourceName) throws FileNotFoundException {
        InputStream resourceAsStream = clazz.getResourceAsStream(resourceName);

        if (resourceAsStream == null) {
            throw new FileNotFoundException(resourceName);
        }

        return new InputStreamReader(resourceAsStream);
    }

    @Contract("_, _ -> new")
    public static @NotNull ByteBuffer byteBufferFrom(@NotNull Class<?> clazz, @NotNull String resourceName) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(resourceName);

        if (resourceAsStream == null) {
            throw new FileNotFoundException(resourceName);
        }

        return byteBufferFrom(resourceAsStream);
    }

    /**
     * Reads all bytes from the stream into a byte array, closes the stream,
     * allocates a byte buffer with the length of the bytes,
     * puts all bytes into the buffer, and flips them.<p>
     * From <a href="https://stackoverflow.com/questions/45171816/lwjgl-3-stbi-load-from-memory-not-working-when-in-jar">here</a>.
     */
    public static @NotNull ByteBuffer byteBufferFrom(@NotNull InputStream stream) throws IOException {
        return byteBufferFrom(IOUtils.toByteArray(stream));
    }

    /**
     * Creates a byte buffer, puts the {@code bytes} into the buffer and flips them.
     */
    public static ByteBuffer byteBufferFrom(byte[] bytes) {
        ByteBuffer buff = BufferUtils.createByteBuffer(bytes.length);
        buff.put(bytes);
        buff.flip();
        return buff;
    }
}
