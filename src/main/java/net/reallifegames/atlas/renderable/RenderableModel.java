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
package net.reallifegames.atlas.renderable;

import net.reallifegames.atlas.asset.blockstates.Model;
import net.reallifegames.atlas.asset.blockstates.Variant;
import net.reallifegames.atlas.module.atlas.TextureAtlas;

import javax.annotation.Nonnull;

/**
 * Holds a renderable OpenGL model.
 *
 * @author Tyler Bucher
 */
public class RenderableModel extends Model {

    /**
     * Creates a new {@link Model} for the {@link Variant}.
     *
     * @param modelName  the string name of the model in json.
     * @param blockModel the {@link RenderableBlockModel} for this model variant.
     * @param xRotation  x-axis rotation of this model.
     * @param yRotation  y-axis rotation of this model.
     * @param uvLock     locks the rotation of the texture of a block.
     * @param weight     sets the probability of the model for being used in the game.
     * @param useOpenGL  states if we should use OpenGL.
     */
    public RenderableModel(@Nonnull final String modelName,
                           @Nonnull final RenderableBlockModel blockModel,
                           final int xRotation,
                           final int yRotation,
                           final boolean uvLock,
                           final int weight,
                           final boolean useOpenGL) {
        super(modelName, blockModel, xRotation, yRotation, uvLock, weight);
        // Rotate texture coordinates along x axis if needed.
        for (int i = 0; i < blockModel.getVertexData().length; i += 66) {
            if (uvLock)
                if (checkUv(blockModel.getVertexData(), i, 2) || checkUv(blockModel.getVertexData(), i, 0)) {
                    rotateUv(blockModel.getVertexData(), i, xRotation);
                }
            for (int j = i; j < i + 66; j += 11) {
                rotateX(blockModel.getVertexData(), j, xRotation);
            }
        }
        // Rotate texture coordinates along y axis if needed.
        for (int i = 0; i < blockModel.getVertexData().length; i += 66) {
            if (uvLock)
                if (checkUv(blockModel.getVertexData(), i, 1)) {
                    rotateUv(blockModel.getVertexData(), i, yRotation);
                }
            for (int j = i; j < i + 66; j += 11) {
                rotateY(blockModel.getVertexData(), j, yRotation);
            }
        }
        if (useOpenGL) {
            blockModel.bake();
        }
    }

    /**
     * Creates a new {@link Model} for the {@link Variant}.
     *
     * @param model        the model to copy.
     * @param textureAtlas the block texture atlas.
     * @param useOpenGL    states if we should use OpenGL.
     */
    public RenderableModel(@Nonnull final Model model, @Nonnull final TextureAtlas textureAtlas, final boolean useOpenGL) {
        this(model.modelName, new RenderableBlockModel(model.blockModel, textureAtlas, useOpenGL), model.xRotation, model.yRotation, model.uvLock, model.weight, useOpenGL);
    }

    /**
     * Rotates texture uv coordinates along the x axis.
     *
     * @param vertices interleaved OpenGL vertex data.
     * @param index    current index to change.
     * @param rotation the amount of rotation in degrees.
     */
    @SuppressWarnings ("Duplicates")
    private void rotateX(@Nonnull final float[] vertices, final int index, final int rotation) {
        if (rotation == 0) {
            return;
        }
        float ty, tz;
        final float angle = (float) Math.toRadians(rotation);
        // Translate y
        vertices[index + 1] -= 0.5;
        // Rotate
        ty = (float) (vertices[index + 1] * Math.cos(angle) - vertices[index + 2] * Math.sin(angle));
        tz = (float) (vertices[index + 1] * Math.sin(angle) + vertices[index + 2] * Math.cos(angle));
        vertices[index + 1] = ty;
        vertices[index + 2] = tz;
        // Translate y
        vertices[index + 1] += 0.5;
        // Rotate normal
        ty = (float) (vertices[index + 6] * Math.cos(angle) - vertices[index + 7] * Math.sin(angle));
        tz = (float) (vertices[index + 6] * Math.sin(angle) + vertices[index + 7] * Math.cos(angle));
        vertices[index + 6] = ty;
        vertices[index + 7] = tz;
    }

    /**
     * Rotates texture uv coordinates along the y axis.
     *
     * @param vertices interleaved OpenGL vertex data.
     * @param index    current index to change.
     * @param rotation the amount of rotation in degrees.
     */
    @SuppressWarnings ("Duplicates")
    private void rotateY(@Nonnull final float[] vertices, final int index, int rotation) {
        if (rotation == 0) {
            return;
        }
        float tx, tz;
        final float angle = (float) Math.toRadians(rotation);
        // Rotate
        tx = (float) (vertices[index] * Math.cos(angle) - vertices[index + 2] * Math.sin(angle));
        tz = (float) (vertices[index] * Math.sin(angle) + vertices[index + 2] * Math.cos(angle));
        vertices[index] = tx;
        vertices[index + 2] = tz;
        // Rotate normal
        tx = (float) (vertices[index + 5] * Math.cos(angle) - vertices[index + 7] * Math.sin(angle));
        tz = (float) (vertices[index + 5] * Math.sin(angle) + vertices[index + 7] * Math.cos(angle));
        vertices[index + 5] = tx;
        vertices[index + 7] = tz;
    }

    /**
     * Checks to see if uvs match.
     *
     * @param vertices interleaved OpenGL vertex data.
     * @param index    current index to change.
     * @param offset   index offset to change by.
     * @return true if uvs match false otherwise.
     */
    private boolean checkUv(@Nonnull final float[] vertices, final int index, final int offset) {
        return vertices[index + offset] == vertices[index + 11 + offset] && vertices[index + offset] == vertices[index + 22 + offset]
                && vertices[index + offset] == vertices[index + 55 + offset];
    }

    /**
     * Rotate texture uv coordinates.
     *
     * @param vertices interleaved OpenGL vertex data.
     * @param index    current index to change.
     * @param rotation rotation in degrees to change (90 / 180 / 270)
     */
    private void rotateUv(@Nonnull final float[] vertices, final int index, final int rotation) {
        final float uv0x = vertices[index + 3];
        final float uv0y = vertices[index + 4];
        final float uv1x = vertices[index + 11 + 3];
        final float uv1y = vertices[index + 11 + 4];
        final float uv2x = vertices[index + 55 + 3];
        final float uv2y = vertices[index + 55 + 4];
        final float uv3x = vertices[index + 22 + 3];
        final float uv3y = vertices[index + 22 + 4];
        switch (rotation) {
            case 90:
                vertices[index + 3] = uv3x;
                vertices[index + 4] = uv3y;
                vertices[index + 11 + 3] = uv0x;
                vertices[index + 11 + 4] = uv0y;
                vertices[index + 22 + 3] = uv2x;
                vertices[index + 22 + 4] = uv2y;
                vertices[index + 33 + 3] = uv2x;
                vertices[index + 33 + 4] = uv2y;
                vertices[index + 44 + 3] = uv0x;
                vertices[index + 44 + 4] = uv0y;
                vertices[index + 55 + 3] = uv1x;
                vertices[index + 55 + 4] = uv1y;
                break;
            case 180:
                vertices[index + 3] = uv2x;
                vertices[index + 4] = uv2y;
                vertices[index + 11 + 3] = uv3x;
                vertices[index + 11 + 4] = uv3y;
                vertices[index + 22 + 3] = uv1x;
                vertices[index + 22 + 4] = uv1y;
                vertices[index + 33 + 3] = uv1x;
                vertices[index + 33 + 4] = uv1y;
                vertices[index + 44 + 3] = uv3x;
                vertices[index + 44 + 4] = uv3y;
                vertices[index + 55 + 3] = uv0x;
                vertices[index + 55 + 4] = uv0y;
                break;
            case 270:
                vertices[index + 3] = uv1x;
                vertices[index + 4] = uv1y;
                vertices[index + 11 + 3] = uv2x;
                vertices[index + 11 + 4] = uv2y;
                vertices[index + 22 + 3] = uv0x;
                vertices[index + 22 + 4] = uv0y;
                vertices[index + 33 + 3] = uv0x;
                vertices[index + 33 + 4] = uv0y;
                vertices[index + 44 + 3] = uv2x;
                vertices[index + 44 + 4] = uv2y;
                vertices[index + 55 + 3] = uv3x;
                vertices[index + 55 + 4] = uv3y;
                break;
        }
    }
}
