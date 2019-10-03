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
package net.reallifegames.atlas.module.skybox;

import net.reallifegames.atlas.renderable.Renderable;
import org.ajgl.graphics.VertexBufferedObject;
import org.lwjgl.opengl.GL15;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 * Creates a cube from the given data.
 *
 * @author Tyler Bucher
 */
public abstract class Cube implements Renderable {

    /**
     * The vertex data for a cube.
     */
    protected final float[] data;

    /**
     * The index rendering order.
     */
    protected final int[] indices;

    /**
     * The OpenGL vertex data handler.
     */
    protected final int vboDataHandler;

    /**
     * The OpenGL index data handler.
     */
    protected final int vboIndexHandler;

    /**
     * Creates a renderable cube from the provided data.
     *
     * @param data    vertex data for a cube.
     * @param indices index rendering order.
     */
    public Cube(@Nonnull final float[] data, @Nonnull final int[] indices) {
        this.data = data;
        this.indices = indices;
        // VBO vertex handler
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            vboDataHandler = VertexBufferedObject.createVboHandler(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW,
                    (FloatBuffer) stack.callocFloat(data.length).put(data).flip());
        }
        // VBO index handler
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            vboIndexHandler = VertexBufferedObject.createVboHandler(GL15.GL_ELEMENT_ARRAY_BUFFER, GL15.GL_STATIC_DRAW,
                    (IntBuffer) stack.callocInt(indices.length).put(indices).flip());
        }
    }

    @Override
    public abstract void draw();
}
