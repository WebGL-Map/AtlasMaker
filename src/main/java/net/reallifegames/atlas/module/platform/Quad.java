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
package net.reallifegames.atlas.module.platform;

import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.renderable.Renderable;
import org.ajgl.graphics.VertexBufferedObject;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

/**
 * A two dimensional quad used for rendering.
 *
 * @author Tyler Bucher
 */
class Quad implements Renderable {

    /**
     * The data to pass to the OpenGL vbo.
     */
    private final float[] data;

    /**
     * The OpenGL vbo handle id.
     */
    private int vboDataHandler;

    /**
     * The id of the texture to render.
     */
    private final int textureHandler;

    /**
     * States if the texture should be rendered.
     */
    public boolean renderTexture;

    /**
     * Creates a new {@link Quad}.
     *
     * @param vertices       the quad vertex positions.
     * @param texCoordinates the texture coordinates.
     * @param normalX        the x quad planar normal.
     * @param normalY        the y quad planar normal.
     * @param normalZ        the z quad planar normal.
     * @param color          the color of this quad.
     * @param textureHandler the id of the texture to render.
     * @param renderTexture  states if the texture should be rendered.
     */
    public Quad(@Nonnull final float[] vertices,
                @Nonnull final float[] texCoordinates,
                final float normalX,
                final float normalY,
                final float normalZ,
                @Nonnull final Vector3f color,
                final int textureHandler,
                final boolean renderTexture) {
        // position 3, texture 2, normal 3, color 3
        data = new float[]{
                vertices[0], vertices[1], vertices[2], texCoordinates[0], texCoordinates[1], normalX, normalY, normalZ, color.x, color.y, color.z,
                vertices[3], vertices[4], vertices[5], texCoordinates[2], texCoordinates[3], normalX, normalY, normalZ, color.x, color.y, color.z,
                vertices[9], vertices[10], vertices[11], texCoordinates[6], texCoordinates[7], normalX, normalY, normalZ, color.x, color.y, color.z,
                vertices[9], vertices[10], vertices[11], texCoordinates[6], texCoordinates[7], normalX, normalY, normalZ, color.x, color.y, color.z,
                vertices[3], vertices[4], vertices[5], texCoordinates[2], texCoordinates[3], normalX, normalY, normalZ, color.x, color.y, color.z,
                vertices[6], vertices[7], vertices[8], texCoordinates[4], texCoordinates[5], normalX, normalY, normalZ, color.x, color.y, color.z
        };
        // VBO vertex handler
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            vboDataHandler = VertexBufferedObject.createVboHandler(GL15.GL_ARRAY_BUFFER, GL15.GL_DYNAMIC_DRAW,
                    (FloatBuffer) stack.callocFloat(data.length).put(data).flip());
        }
        this.textureHandler = textureHandler;
        this.renderTexture = renderTexture;
    }

    /**
     * Updates the two triangles interleaved data and pushes the data to OpenGL.
     *
     * @param vertices       the new vertex positions.
     * @param texCoordinates the new texture coordinates.
     * @param color          the new vertex colors.
     */
    public void update(@Nonnull float[] vertices, @Nonnull float[] texCoordinates, @Nonnull Vector3f color) {
        updateData(vertices, texCoordinates, color);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
            GL15.glBufferSubData(GL15.GL_ARRAY_BUFFER, 0, (FloatBuffer) stack.callocFloat(data.length).put(data).flip());
        }
    }

    /**
     * Updates the two triangles interleaved data.
     *
     * @param vertices       the new vertex positions.
     * @param texCoordinates the new texture coordinates.
     * @param color          the new vertex colors.
     */
    private void updateData(@Nonnull float[] vertices, @Nonnull float[] texCoordinates, @Nonnull Vector3f color) {
        // First Triangle
        subUpdateData(0, vertices[0], vertices[1], vertices[2], texCoordinates[0], texCoordinates[1], color);
        subUpdateData(11, vertices[3], vertices[4], vertices[5], texCoordinates[2], texCoordinates[3], color);
        subUpdateData(22, vertices[9], vertices[10], vertices[11], texCoordinates[6], texCoordinates[7], color);
        // Second triangle
        subUpdateData(33, vertices[9], vertices[10], vertices[11], texCoordinates[6], texCoordinates[7], color);
        subUpdateData(44, vertices[3], vertices[4], vertices[5], texCoordinates[2], texCoordinates[3], color);
        subUpdateData(55, vertices[6], vertices[7], vertices[8], texCoordinates[4], texCoordinates[5], color);
    }

    /**
     * Updates the interleaved data given an index.
     *
     * @param index            the starting position of the data to update.
     * @param vertexX          x position.
     * @param vertexY          y position.
     * @param vertexZ          z position.
     * @param textCoordinatesX texture coordinates x position.
     * @param textCoordinatesY texture coordinates y position.
     * @param color            the color of the vertex.
     */
    private void subUpdateData(final int index,
                               final float vertexX,
                               final float vertexY,
                               final float vertexZ,
                               final float textCoordinatesX,
                               final float textCoordinatesY,
                               @Nonnull final Vector3f color) {
        data[index] = vertexX;
        data[index + 1] = vertexY;
        data[index + 2] = vertexZ;
        data[index + 3] = textCoordinatesX;
        data[index + 4] = textCoordinatesY;
        data[index + 8] = color.x;
        data[index + 9] = color.y;
        data[index + 10] = color.z;
    }

    @Override
    @SuppressWarnings ("Duplicates")
    public void draw() {
        // Enable pointers
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        GL20.glEnableVertexAttribArray(2);
        GL20.glEnableVertexAttribArray(3);
        // Vertex pointer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 11 * Float.BYTES, 0);
        // Texture pointer
        GL20.glVertexAttribPointer(1, 2, GL11.GL_FLOAT, false, 11 * Float.BYTES, 3 * Float.BYTES);
        // Normal pointer
        GL20.glVertexAttribPointer(2, 3, GL11.GL_FLOAT, false, 11 * Float.BYTES, 5 * Float.BYTES);
        // Color pointer
        GL20.glVertexAttribPointer(3, 3, GL11.GL_FLOAT, false, 11 * Float.BYTES, 8 * Float.BYTES);
        // Bind Texture
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.renderTexture ? textureHandler : Atlas.nullTextureId);
        // Render call
        VertexBufferedObject.drawVboArrays(GL11.GL_TRIANGLES, 0, 12);
        // Disable pointers
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
    }
}
