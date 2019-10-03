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
package net.reallifegames.atlas.asset.blockmodels;

import org.joml.Vector4d;

import javax.annotation.Nonnull;

/**
 * Named down, up, north, south, west or east. Contains the properties of the specified face.
 *
 * @author Tyler Bucher
 */
public class Face {

    /**
     * Defines the area of the texture to use according to the scheme [x1, y1, x2, y2]. If unset, it defaults to values
     * equal to xyz position of the element. The texture behavior will be inconsistent if UV extends below 0 or above
     * 16. If the numbers of x1 and x2 are swapped (e.g. from 0, 0, 16, 16 to 16, 0, 0, 16), the texture will be
     * flipped. UV is optional, and if not supplied it will automatically generate based on the element's position.
     */
    public Vector4d uv;

    /**
     * Specifies the texture in form of the texture variable prepended with a #.
     */
    public final String textureId;

    /**
     * Specifies whether a face does not need to be rendered when there is a block touching it in the specified
     * position. The position can be: down, up, north, south, west, or east. It will also determine which side of the
     * block to use the light level from for lighting the face, and if unset, defaults to the side.
     */
    public final String cullFace;

    /**
     * Rotates the texture by the specified number of degrees. Can be 0, 90, 180, or 270. Defaults to 0. Rotation does
     * not affect which part of the texture is used. Instead, it amounts to permutation of the selected texture vertexes
     * (selected implicitly, or explicitly though uv).
     */
    public final int rotation;

    /**
     * Determines whether to tint the texture using a hardcoded tint index. The default is not using the tint, and any
     * number causes it to use tint. Note that only certain blocks have a tint index, all others will be unaffected.
     */
    public final int tintIndex;

    /**
     * Contains the properties of the specified face.
     *
     * @param uv        defines the area of the texture to use according to the scheme [x1, y1, x2, y2].
     * @param textureId specifies the texture in form of the texture variable prepended with a #.
     * @param cullFace  specifies whether a face does not need to be rendered when there is a block touching it in the
     *                  specified position.
     * @param rotation  rotates the texture by the specified number of degrees.
     * @param tintIndex determines whether to tint the texture using a hardcoded tint index.
     */
    public Face(@Nonnull final Vector4d uv,
                @Nonnull final String textureId,
                @Nonnull final String cullFace,
                final int rotation,
                final int tintIndex) {
        this.uv = uv;
        this.textureId = textureId;
        this.cullFace = cullFace;
        this.rotation = rotation;
        this.tintIndex = tintIndex;
    }
}
