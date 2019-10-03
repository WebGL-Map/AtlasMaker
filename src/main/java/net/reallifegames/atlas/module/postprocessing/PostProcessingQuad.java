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
package net.reallifegames.atlas.module.postprocessing;

import net.reallifegames.atlas.renderable.Renderable;
import org.ajgl.graphics.VertexBufferedObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;

/**
 * A 2d quad for post processing effects.
 *
 * @author Tyler Bucher
 */
public class PostProcessingQuad implements Renderable {

    /**
     * Triangle data array.
     */
    private final float[] data;

    /**
     * OpenGL vbo data handler.
     */
    private int vboDataHandler;

    /**
     * Creates a new 2d quad for the screen.
     */
    public PostProcessingQuad() {
        data = new float[]{
                // positions   // texCoords
                // First triangle
                -1, 1, 0, 0, 1,
                -1, -1, 0, 0, 0,
                1, 1, 0, 1, 1,
                // Second triangle
                1, 1, 0, 1, 1,
                -1, -1, 0, 0, 0,
                1, -1, 0, 1, 0
        };
        // VBO vertex handler
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            vboDataHandler = VertexBufferedObject.createVboHandler(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW,
                    (FloatBuffer) stack.callocFloat(data.length).put(data).flip());
        }
    }

    @Override
    public void draw() {
        // Enable pointers
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        // Vertex pointer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 5 * Float.BYTES, 0);
        // Texture pointer
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 5 * Float.BYTES, 3 * Float.BYTES);
        // Render call
        VertexBufferedObject.drawVboArrays(GL11.GL_TRIANGLES, 0, 6);
        // Disable pointers
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
    }
}
