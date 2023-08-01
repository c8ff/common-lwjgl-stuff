package dev.seeight.common.lwjgl.util;

import org.apache.commons.io.IOUtils;
import org.lwjgl.BufferUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class IOUtil {
    public static ByteBuffer byteBufferFrom(Class<?> clazz, String resourceName) throws IOException {
        InputStream resourceAsStream = clazz.getResourceAsStream(resourceName);

        if (resourceAsStream == null) {
            throw new FileNotFoundException(resourceName);
        }

        return byteBufferFrom(resourceAsStream);
    }

    /**
     * From <a href="https://stackoverflow.com/questions/45171816/lwjgl-3-stbi-load-from-memory-not-working-when-in-jar">here</a>.<p>
     * Note: This method closes the {@code stream}.
     */
    public static ByteBuffer byteBufferFrom(InputStream stream) throws IOException {
        return byteBufferFrom(IOUtils.toByteArray(stream));
    }

    /**
     * Creates a byte buffer, sets the {@code bytes} to the buffer and flips them.
     */
    public static ByteBuffer byteBufferFrom(byte[] bytes) {
        ByteBuffer buff = BufferUtils.createByteBuffer(bytes.length);
        buff.put(bytes);
        buff.flip();
        return buff;
    }
}
