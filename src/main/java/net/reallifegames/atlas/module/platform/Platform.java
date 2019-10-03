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

import net.reallifegames.atlas.listenable.GameProperty;
import net.reallifegames.atlas.renderable.Renderable;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import javax.annotation.Nonnull;

/**
 * A platform for blocks to sit on and help give perspective.
 *
 * @author Tyler Bucher
 */
public class Platform implements Renderable {

    /**
     * Platform radius.
     */
    private float radius;

    /**
     * Platform height.
     */
    private float height;

    /**
     * Platform Color.
     */
    private final Vector3f color;

    /**
     * Platform texture id.
     */
    private final int textureHandler;

    /**
     * Platform quad face array.
     */
    private final Quad[] quadArray = new Quad[6];

    /**
     * Outer grid.
     */
    private final Grid grid;

    /**
     * Inner grid.
     */
    private final Grid microGrid;

    /**
     * Texture coordinates data array.
     */
    private final float[] textCoordinates = new float[8];

    /**
     * Vertex data array.
     */
    private final float[] vertices = new float[12];

    /**
     * Grid division factor
     */
    private float divFactor;

    /**
     * Platform half height for texture data.
     */
    private float halfHeight;

    /**
     * Platform texture coordinate data length.
     */
    private float length;

    /**
     * Should the platform be updated.
     */
    private boolean update;

    /**
     * States which faces of the platform should be rendered. ( BOTTOM | EAST | SOUTH | WEST | NORTH | TOP)
     */
    public volatile int renderPlatformFaces = 0b111111;

    /**
     * Internal listener to update platform quads to use their texture.
     */
    private volatile GameProperty<Boolean> renderTextureProperty;

    /**
     * @param radius        platform radius.
     * @param height        platform height.
     * @param color         platform color.
     * @param textureId     id of a texture the platform should use.
     * @param renderTexture should the platform render its texture.
     */
    public Platform(final float radius, final float height, @Nonnull final Vector3f color, final int textureId, final boolean renderTexture) {
        this.radius = radius;
        this.height = height;
        this.color = color;
        textureHandler = textureId;
        this.renderTextureProperty = new GameProperty<>(renderTexture);
        generatePlatform(radius, height, color);
        // Create platform grids
        microGrid = new Grid(1.5f, 1.5f - 0.9375f, 0.0625f, 0.01f, new Vector3f(0), 1);
        grid = new Grid(1.5f, 1.5f, 1, 0.01f, new Vector3f(0), 2);
        // change quad useTexture var
        this.renderTextureProperty.addListener((oldValue, newValue)->{
            for (final Quad quad : quadArray) {
                quad.renderTexture = newValue;
            }
        });
    }

    /**
     * Generates platform quads and vertex data.
     *
     * @param radius platform radius.
     * @param height platform height.
     * @param color  platform color.
     */
    private void generatePlatform(final float radius, final float height, @Nonnull final Vector3f color) {
        divFactor = 6.0f;
        halfHeight = Math.abs(height) / 2.0f / divFactor;
        length = radius * 2.0f / divFactor;
        // Generate faces [top,north,west,south,east,bottom]
        computeData(0, radius, height);
        quadArray[0] = new Quad(vertices, textCoordinates, 0, 1, 0, color, textureHandler, this.renderTextureProperty.getProperty());
        computeData(1, radius, height);
        quadArray[1] = new Quad(vertices, textCoordinates, 0, 0, -1, color, textureHandler, this.renderTextureProperty.getProperty());
        computeData(2, radius, height);
        quadArray[2] = new Quad(vertices, textCoordinates, -1, 0, 0, color, textureHandler, this.renderTextureProperty.getProperty());
        computeData(3, radius, height);
        quadArray[3] = new Quad(vertices, textCoordinates, 0, 0, 1, color, textureHandler, this.renderTextureProperty.getProperty());
        computeData(4, radius, height);
        quadArray[4] = new Quad(vertices, textCoordinates, 1, 0, 0, color, textureHandler, this.renderTextureProperty.getProperty());
        computeData(5, radius, height);
        quadArray[5] = new Quad(vertices, textCoordinates, 0, -1, 0, color, textureHandler, this.renderTextureProperty.getProperty());
    }

    /**
     * Sets vertex and texture coordinate data.
     *
     * @param index  the index id of the data to make.
     * @param radius radius of the platform.
     * @param height height of the platform.
     */
    @SuppressWarnings ("SuspiciousNameCombination")
    private void computeData(final int index, final float radius, final float height) {
        switch (index) {
            case 0:
                setVertices(-radius, 0, -radius, -radius, 0, radius, radius, 0, radius, radius, 0, -radius);
                setTextCoordinates(halfHeight, halfHeight, halfHeight, length - halfHeight, length - halfHeight, length - halfHeight, length - halfHeight, halfHeight);
                break;
            case 1:
                setVertices(radius, 0, -radius, radius, height, -radius, -radius, height, -radius, -radius, 0, -radius);
                setTextCoordinates(length - halfHeight, halfHeight, length + halfHeight, -halfHeight, -halfHeight, -halfHeight, halfHeight, halfHeight);
                break;
            case 2:
                setVertices(-radius, 0, -radius, -radius, height, -radius, -radius, height, radius, -radius, 0, radius);
                setTextCoordinates(halfHeight, halfHeight, -halfHeight, -halfHeight, -halfHeight, length + halfHeight, halfHeight, length - halfHeight);
                break;
            case 3:
                setVertices(-radius, 0, radius, -radius, height, radius, radius, height, radius, radius, 0, radius);
                setTextCoordinates(halfHeight, length - halfHeight, -halfHeight, length + halfHeight, length + halfHeight, length + halfHeight, length - halfHeight, length - halfHeight);
                break;
            case 4:
                setVertices(radius, 0, radius, radius, height, radius, radius, height, -radius, radius, 0, -radius);
                setTextCoordinates(length - halfHeight, length - halfHeight, length + halfHeight, length + halfHeight, length + halfHeight, -halfHeight, length - halfHeight, halfHeight);
                break;
            case 5:
                setVertices(radius, height, -radius, radius, height, radius, -radius, height, radius, -radius, height, -radius);
                setTextCoordinates(length + halfHeight, -halfHeight, length + halfHeight, length + halfHeight, -halfHeight, length + halfHeight, -halfHeight, -halfHeight);
                break;
        }
    }

    /**
     * Sets texture coordinates position data.
     *
     * @param texCoordinates0X texture coordinate 0 position x.
     * @param texCoordinates0Y texture coordinate 0 position y.
     * @param texCoordinates1X texture coordinate 1 position x.
     * @param texCoordinates1Y texture coordinate 1 position y.
     * @param texCoordinates2X texture coordinate 2 position x.
     * @param texCoordinates2Y texture coordinate 2 position y.
     * @param texCoordinates3X texture coordinate 3 position x.
     * @param texCoordinates3Y texture coordinate 3 position y.
     */
    @SuppressWarnings ("Duplicates")
    private void setTextCoordinates(final float texCoordinates0X,
                                    final float texCoordinates0Y,
                                    final float texCoordinates1X,
                                    final float texCoordinates1Y,
                                    final float texCoordinates2X,
                                    final float texCoordinates2Y,
                                    final float texCoordinates3X,
                                    final float texCoordinates3Y) {
        textCoordinates[0] = texCoordinates0X;
        textCoordinates[1] = texCoordinates0Y;
        textCoordinates[2] = texCoordinates1X;
        textCoordinates[3] = texCoordinates1Y;
        textCoordinates[4] = texCoordinates2X;
        textCoordinates[5] = texCoordinates2Y;
        textCoordinates[6] = texCoordinates3X;
        textCoordinates[7] = texCoordinates3Y;
    }

    /**
     * Sets vertex position data.
     *
     * @param vertex0X vertex 0 position x.
     * @param vertex0Y vertex 0 position y.
     * @param vertex0Z vertex 0 position z.
     * @param vertex1X vertex 1 position x.
     * @param vertex1Y vertex 1 position y.
     * @param vertex1Z vertex 1 position z.
     * @param vertex2X vertex 2 position x.
     * @param vertex2Y vertex 2 position y.
     * @param vertex2Z vertex 2 position z.
     * @param vertex3X vertex 3 position x.
     * @param vertex3Y vertex 3 position y.
     * @param vertex3Z vertex 3 position z.
     */
    @SuppressWarnings ("Duplicates")
    private void setVertices(final float vertex0X,
                             final float vertex0Y,
                             final float vertex0Z,
                             final float vertex1X,
                             final float vertex1Y,
                             final float vertex1Z,
                             final float vertex2X,
                             final float vertex2Y,
                             final float vertex2Z,
                             final float vertex3X,
                             final float vertex3Y,
                             final float vertex3Z) {
        vertices[0] = vertex0X;
        vertices[1] = vertex0Y;
        vertices[2] = vertex0Z;
        vertices[3] = vertex1X;
        vertices[4] = vertex1Y;
        vertices[5] = vertex1Z;
        vertices[6] = vertex2X;
        vertices[7] = vertex2Y;
        vertices[8] = vertex2Z;
        vertices[9] = vertex3X;
        vertices[10] = vertex3Y;
        vertices[11] = vertex3Z;
    }

    /**
     * @return radius of the platform.
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Set the radius of the platform.
     *
     * @param radius radius of the platform.
     */
    public void setRadius(final float radius) {
        this.radius = radius;
        this.update = true;
    }

    /**
     * @return height of the platform.
     */
    public float getHeight() {
        return height;
    }

    /**
     * Set the height of the platform.
     *
     * @param height height of the platform.
     */
    public void setHeight(final float height) {
        this.height = height;
        this.update = true;
    }

    /**
     * Sets the color of the platform.
     *
     * @param red   red component.
     * @param green green component.
     * @param blue  blue component.
     */
    public void setColor(float red, float green, float blue) {
        this.color.set(red, green, blue);
        this.update = true;
    }

    /**
     * @return the platform overall color.
     */
    public Vector3fc getColor() {
        return color;
    }

    /**
     * @return the outer platform grid.
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * @return the inner platform grid.
     */
    public Grid getMicroGrid() {
        return microGrid;
    }

    /**
     * @return Internal listener to update platform quads to use their texture.
     */
    public GameProperty<Boolean> getRenderTextureProperty() {
        return renderTextureProperty;
    }

    /**
     * Updates the platform if it needs to be updated.
     */
    public void update() {
        if (update) {
            update = false;
            update(radius, height, color);
        }
        renderTextureProperty.updateProperty();
    }

    /**
     * Updates the platform data.
     *
     * @param radius the radius of the platform
     * @param height the height of the platform.
     * @param color  the color of the platform.
     */
    public void update(final float radius, final float height, @Nonnull final Vector3f color) {
        divFactor = 6.0f;
        halfHeight = Math.abs(height) / 2.0f / divFactor;
        length = radius * 2.0f / divFactor;
        for (int i = 0; i < quadArray.length; i++) {
            computeData(i, radius, height);
            quadArray[i].update(vertices, textCoordinates, color);
        }
    }

    @Override
    public void draw() {
        for (int i = 0; i < quadArray.length; i++) {
            if ((renderPlatformFaces & 0b000001 << i) == 0b000001 << i) {
                quadArray[i].draw();
            }
        }
    }
}
