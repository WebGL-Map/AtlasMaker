/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2019 Tyler Bucher
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reallifegames.atlas;

import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBImage;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;

/**
 * Helps with loading a texture for use with OpenGL.
 *
 * @author Tyler Bucher
 */
public class TextureLoader {

    /**
     * Converts a resource path into a byte buffer.
     *
     * @param resource   the path of the resource.
     * @param bufferSize size of the buffer.
     * @return the resource as a ByteBuffer.
     *
     * @throws IOException if file is not able to be read.
     */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;

        final File file = new File(resource);
        if (file.isFile()) {
            final FileInputStream stream = new FileInputStream(file);
            final FileChannel fc = stream.getChannel();
            buffer = BufferUtils.createByteBuffer((int) fc.size() + 1);

            while (fc.read(buffer) != -1)
                ;

            fc.close();
            stream.close();
        } else {
            buffer = BufferUtils.createByteBuffer(bufferSize);
            final InputStream source = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (source == null) {
                throw new java.io.FileNotFoundException(resource);
            }
            try {
                try (ReadableByteChannel rbc = Channels.newChannel(source)) {
                    while (true) {
                        int bytes = rbc.read(buffer);
                        if (bytes == -1)
                            break;
                        if (buffer.remaining() == 0)
                            buffer = resizeBuffer(buffer, buffer.capacity() * 2);
                    }
                }
            } finally {
                source.close();
            }
        }
        buffer.flip();
        return buffer;
    }

    /**
     * Resizes a buffer.
     *
     * @param buffer      buffer to resize.
     * @param newCapacity new size for the buffer.
     * @return the newly sized buffer.
     */
    private static ByteBuffer resizeBuffer(@Nonnull final ByteBuffer buffer, final int newCapacity) {
        final ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        return newBuffer;
    }

    /**
     * Loads an image form a file.
     *
     * @param path     the path to the image.
     * @param channels how many channels is the image.
     * @return the image as a ByteBuffer.
     *
     * @throws IOException if file is not able to be read.
     */
    public static ByteBuffer loadImage(@Nonnull final String path, final int channels) throws IOException {
        final IntBuffer width = BufferUtils.createIntBuffer(1);
        final IntBuffer height = BufferUtils.createIntBuffer(1);
        final IntBuffer components = BufferUtils.createIntBuffer(1);
        return STBImage.stbi_load_from_memory(
                TextureLoader.ioResourceToByteBuffer(path, 2048),
                width, height, components, channels
        );
    }
}
