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

import org.joml.Vector3d;
import org.joml.Vector4d;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Contains all the elements of the model. they can only have cubic forms. If both "parent" and "elements" are set, the
 * "elements" tag overrides the "elements" tag from the previous model.
 *
 * @author Tyler Bucher
 */
public class Element {

    /**
     * Start point of a cube according to the scheme [x, y, z]. Values must be between -16 and 32.
     */
    public final Vector3d from;

    /**
     * Stop point of a cube according to the scheme [x, y, z]. Values must be between -16 and 32.
     */
    public final Vector3d to;

    /**
     * Sets the center of the rotation according to the scheme [x, y, z].
     */
    public final Vector3d origin;

    /**
     * Specifies the direction of rotation, can be "x", "y" or "z".
     */
    public final String axis;

    /**
     * Specifies the angle of rotation. Can be 45 through -45 degrees in 22.5 degree increments.
     */
    public final float angle;

    /**
     * Specifies whether or not to scale the faces across the whole block. Can be true or false. Defaults to false.
     */
    public final boolean rescale;

    /**
     * Defines if shadows are rendered (true - default), not (false).
     */
    public final boolean shade;

    /**
     * Holds all the faces of the cube. If a face is left out, it will not be rendered.
     */
    public final Map<String, Face> faces;

    /**
     * Contains all the elements of the model.
     *
     * @param from    start point of a cube according to the scheme.
     * @param to      stop point of a cube according to the scheme.
     * @param origin  sets the center of the rotation according to the scheme.
     * @param axis    specifies the direction of rotation.
     * @param angle   specifies the angle of rotation.
     * @param rescale specifies whether or not to scale the faces across the whole block.
     * @param shade   defines if shadows are rendered.
     * @param faces   holds all the faces of the cube.
     */
    public Element(@Nonnull final Vector3d from,
                   @Nonnull final Vector3d to,
                   @Nonnull final Vector3d origin,
                   @Nonnull final String axis,
                   final float angle,
                   final boolean rescale,
                   final boolean shade,
                   @Nonnull final Map<String, Face> faces) {
        this.from = from;
        this.to = to;
        this.origin = origin;
        this.axis = axis;
        this.angle = angle;
        this.rescale = rescale;
        this.shade = shade;
        this.faces = faces;
        fixFaceUvs();
    }

    /**
     * Fix face uvs to be oriented correctly.
     */
    private void fixFaceUvs() {
        for (Map.Entry<String, Face> kvp : faces.entrySet()) {
            if (kvp.getValue().uv == null) {
                switch (kvp.getKey()) {
                    case "down":
                        kvp.getValue().uv = new Vector4d(to.x, from.z, from.x, to.z);
                        break;
                    case "up":
                        kvp.getValue().uv = new Vector4d(from.x, from.z, to.x, to.z);
                        break;
                    case "north":
                        kvp.getValue().uv = new Vector4d(to.x, 16.0 - to.y, from.x, 16.0 - from.y);
                        break;
                    case "south":
                        kvp.getValue().uv = new Vector4d(from.x, 16.0 - to.y, to.x, 16.0 - from.y);
                        break;
                    case "west":
                        kvp.getValue().uv = new Vector4d(from.z, 16.0 - to.y, to.z, 16.0 - from.y);
                        break;
                    case "east":
                        kvp.getValue().uv = new Vector4d(to.z, 16.0 - to.y, from.z, 16.0 - from.y);
                        break;
                }
            }
        }
    }
}
