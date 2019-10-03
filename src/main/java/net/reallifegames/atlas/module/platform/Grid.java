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

import net.reallifegames.atlas.renderable.Renderable;
import org.ajgl.graphics.VertexBufferedObject;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

/**
 * A grid for used for block measurement on a {@link Platform}.
 *
 * @author Tyler Bucher
 */
public class Grid implements Renderable {

    /**
     * The OpenGL vbo handle id.
     */
    private int vboDataHandler;

    /**
     * The OpenGL data size.
     */
    private int dataSize;

    /**
     * States if the grid should updates its data.
     */
    private volatile boolean update;

    /**
     * Radius of the grid.
     */
    private volatile float radius;

    /**
     * Radius ending point.
     */
    private volatile float radiusEnd;

    /**
     * Grid incremental value.
     */
    private float inc;

    /**
     * Height from 0 which the grid will sit at.
     */
    private float height;

    /**
     * OpenGL line width.
     */
    private float lineWidth;

    /**
     * The color of the gird.
     */
    private volatile Vector3f color;

    /**
     * The calculated grid diameter.
     */
    private int diameter;

    /**
     * Creates a new Grid for a {@link Platform}.
     *
     * @param radius    radius of the grid.
     * @param radiusEnd radius ending point.
     * @param inc       the grid incremental value.
     * @param height    the height from 0 which the grid will sit at.
     * @param color     color of the grid.
     * @param lineWidth OpenGL line width.
     */
    public Grid(final float radius,
                final float radiusEnd,
                final float inc,
                final float height,
                @Nonnull final Vector3f color,
                final float lineWidth) {
        this.radius = radius;
        this.radiusEnd = radiusEnd;
        this.inc = inc;
        this.height = height;
        this.lineWidth = lineWidth;
        this.color = color;
        // Setup OpenGL data
        diameter = (int) (radius * 2.0f / inc);
        dataSize = diameter * diameter * 24 / 6;
        final FloatBuffer buffer = MemoryUtil.memAllocFloat(diameter * diameter * 24);
        vboDataHandler = VertexBufferedObject.createVboHandler(GL15.GL_ARRAY_BUFFER, GL15.GL_DYNAMIC_DRAW,
                (FloatBuffer) updateGrid(radius, radiusEnd, inc, height, color, buffer).flip());
        MemoryUtil.memFree(buffer);
    }

    /**
     * Sets the grid radius and ending point.
     *
     * @param radius    radius of the grid.
     * @param radiusEnd radius ending point.
     */
    public void setRadius(final float radius, final float radiusEnd) {
        this.radius = radius;
        this.radiusEnd = radiusEnd;
        this.update = true;
    }

    /**
     * @return radius of the grid.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * @return radius ending point.
     */
    public float getRadiusEnd() {
        return radiusEnd;
    }

    /**
     * Set the grid incremental value.
     *
     * @param inc the grid incremental value.
     */
    public void setInc(final float inc) {
        this.inc = inc;
        this.update = true;
    }

    /**
     * @return the grid incremental value.
     */
    public float getInc() {
        return inc;
    }

    /**
     * Sets the height from 0 which the grid will sit at.
     *
     * @param height the height from 0 which the grid will sit at.
     */
    public void setHeight(final float height) {
        this.height = height;
        this.update = true;
    }

    /**
     * @return the height from 0 which the grid sits at.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Sets the width of the grid lines.
     *
     * @param lineWidth the OpenGL line width.
     */
    public void setLineWidth(final float lineWidth) {
        this.lineWidth = lineWidth;
        this.update = true;
    }

    /**
     * @return the current OpenGL line width.
     */
    public float getLineWidth() {
        return lineWidth;
    }

    /**
     * Sets the color of the grid.
     *
     * @param red   red component.
     * @param green green component.
     * @param blue  blue component.
     */
    public void setColor(final float red, final float green, final float blue) {
        this.color.set(red, green, blue);
        this.update = true;
    }

    /**
     * @return the current color of the grid.
     */
    public Vector3fc getColor() {
        return color;
    }

    /**
     * Updates the grid if it needs to be updated.
     */
    public void update() {
        if (this.update) {
            this.update = false;
            diameter = (int) (radius * 2.0f / inc);
            dataSize = diameter * diameter * 24 / 6;
            final FloatBuffer buffer = MemoryUtil.memAllocFloat(diameter * diameter * 24);
            GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
            GL15.glBufferData(GL15.GL_ARRAY_BUFFER, (FloatBuffer) updateGrid(radius, radiusEnd, inc, height, color, buffer).flip(), GL15.GL_DYNAMIC_DRAW);
            MemoryUtil.memFree(buffer);
        }
    }

    /**
     * Updates the multiple aspects of the grid.
     *
     * @param radius    radius of the grid.
     * @param radiusEnd radius ending point.
     * @param inc       incremental value for the grid.
     * @param height    the height of the grid from 0.
     * @param color     the color of the grid lines.
     * @param buffer    the interleaved buffer for data storage.
     * @return the passed interleaved buffer for data storage.
     */
    private FloatBuffer updateGrid(final float radius,
                                   final float radiusEnd,
                                   final float inc,
                                   final float height,
                                   @Nonnull final Vector3f color,
                                   @Nonnull final FloatBuffer buffer) {
        for (float i = -radius; i < radiusEnd; i += inc) {
            for (float j = -radius; j < radiusEnd; j += inc) {
                subUpdate(buffer, i, height, j, color);
                subUpdate(buffer, i, height, j + 1, color);
                subUpdate(buffer, i + 1, height, j + 1, color);
                subUpdate(buffer, i + 1, height, j, color);
            }
        }
        return buffer;
    }

    /**
     * Updates a portion of a buffer at its current position.
     *
     * @param buffer the buffer to update.
     * @param x      x position to add.
     * @param y      y position to add.
     * @param z      z position to add.
     * @param color  the color of the newly added vertices.
     */
    private void subUpdate(@Nonnull final FloatBuffer buffer,
                           final float x,
                           final float y,
                           final float z,
                           @Nonnull final Vector3f color) {
        buffer.put(x);
        buffer.put(y);
        buffer.put(z);
        buffer.put(color.x);
        buffer.put(color.y);
        buffer.put(color.z);
    }

    @Override
    public void draw() {
        // Enable pointers
        GL20.glEnableVertexAttribArray(0);
        GL20.glEnableVertexAttribArray(1);
        // Vertex pointer
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboDataHandler);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 0);
        // Color pointer
        GL20.glVertexAttribPointer(1, 3, GL11.GL_FLOAT, false, 6 * Float.BYTES, 3 * Float.BYTES);
        // Render call
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_LINE);
        GL11.glLineWidth(lineWidth);
        VertexBufferedObject.drawVboArrays(GL11.GL_QUADS, 0, dataSize);
        GL11.glPolygonMode(GL11.GL_FRONT_AND_BACK, GL11.GL_FILL);
        // Disable pointers
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
    }
}
