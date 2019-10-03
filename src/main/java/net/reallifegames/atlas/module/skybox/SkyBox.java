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

import org.ajgl.graphics.VertexBufferedObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;

/**
 * A Renderable cube used to draw a SkyBox.
 *
 * @author Tyler Bucher
 */
public final class SkyBox extends Cube {

    /**
     * The texture id handler.
     */
    private final int cubeTextureHandler;

    /**
     * Creates a new SkyBox with a given scale and texture.
     *
     * @param scale     the value to scale the cube by.
     * @param textureId texture id handler.
     */
    public SkyBox(final float scale, final int textureId) {
        super(new float[]{
                // North
                scale, scale, scale,
                scale, -scale, scale,
                -scale, -scale, scale,
                -scale, scale, scale,
                // West
                scale, scale, -scale,
                scale, -scale, -scale,
                // South
                -scale, scale, -scale,
                -scale, -scale, -scale,
        }, new int[]{
                // North
                0, 1, 3,
                3, 1, 2,
                // West
                4, 5, 0,
                0, 5, 1,
                // South
                6, 7, 4,
                4, 7, 5,
                // East
                3, 2, 6,
                6, 2, 7,
                // Top
                4, 0, 6,
                6, 0, 3,
                // Bottom
                1, 5, 2,
                2, 5, 7
        });
        // Make cube map
        cubeTextureHandler = textureId;
    }

    @Override
    public void draw() {
        // Enable pointers
        GL20.glEnableVertexAttribArray(0);
        // Index pointer
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, vboIndexHandler);
        GL11.glIndexPointer(GL11.GL_INT, 0, 0);
        // Vertex pointer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);
        // Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, cubeTextureHandler);
        // Render call
        VertexBufferedObject.drawVboElements(GL11.GL_TRIANGLES, this.indices.length, GL11.GL_UNSIGNED_INT, 0);
        // Disable pointers
        GL20.glDisableVertexAttribArray(0);
    }
}
