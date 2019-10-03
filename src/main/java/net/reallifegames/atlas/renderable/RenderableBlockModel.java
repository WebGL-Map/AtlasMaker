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

import net.reallifegames.atlas.TextureManager;
import net.reallifegames.atlas.asset.blockmodels.BlockModel;
import net.reallifegames.atlas.asset.blockmodels.Element;
import net.reallifegames.atlas.asset.blockmodels.Face;
import net.reallifegames.atlas.asset.blockstates.BlockState;
import net.reallifegames.atlas.module.atlas.TextureAtlas;
import org.ajgl.graphics.VertexBufferedObject;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;
import java.util.List;
import java.util.Map;

/**
 * A renderable version of a {@link BlockModel}.
 *
 * @author Tyler Bucher
 */
public class RenderableBlockModel extends BlockModel implements Renderable {

    /**
     * Interleaved block vertex data.
     */
    private float[] vertexData;

    /**
     * OpenGL vbo object data.
     */
    protected int vboDataHandler;

    /**
     * The block texture atlas.
     */
    private final int textureAtlas;

    /**
     * Creates a new {@link BlockModel} for a {@link BlockState}.
     *
     * @param parentModel      the parent model of this model.
     * @param ambientOcclusion whether to use ambient occlusion.
     * @param textures         holds the textures of the model.
     * @param elements         contains all the elements of the model.
     * @param textAtlas        the block texture atlas.
     * @param useOpenGL        states if we should use OpenGL.
     */
    public RenderableBlockModel(@Nonnull final BlockModel parentModel,
                                final boolean ambientOcclusion,
                                @Nonnull final Map<String, String> textures,
                                @Nonnull final List<Element> elements,
                                @Nonnull final TextureAtlas textAtlas,
                                final boolean useOpenGL) {
        super(parentModel, ambientOcclusion, textures, elements);
        createData(textAtlas);
        textureAtlas = useOpenGL ? TextureManager.getTexture("atlas") : -1;
    }

    /**
     * Creates a new {@link BlockModel} for a {@link BlockState}.
     *
     * @param model     the parent model of this model.
     * @param textAtlas the block texture atlas.
     * @param useOpenGL states if we should use OpenGL.
     */
    public RenderableBlockModel(@Nonnull final BlockModel model, @Nonnull final TextureAtlas textAtlas, final boolean useOpenGL) {
        this(model.parentModel, model.ambientOcclusion, model.textures, model.elements, textAtlas, useOpenGL);
    }

    /**
     * Creates OpenGL vbo object.
     */
    public void bake() {
        // VBO vertex handler
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            vboDataHandler = VertexBufferedObject.createVboHandler(GL15.GL_ARRAY_BUFFER, GL15.GL_STATIC_DRAW,
                    (FloatBuffer) stack.callocFloat(vertexData.length).put(vertexData).flip());
        }
    }

    /**
     * Creates model vertex data.
     *
     * @param textAtlas the block texture atlas.
     */
    public void createData(@Nonnull final TextureAtlas textAtlas) {
        BlockModel model = this;
        while (model.elements == null || model.elements.size() == 0) {
            model = model.parentModel;
        }
        int vIndex = 0;
        final Vector3f convertFrom = new Vector3f();
        final Vector3f convertTo = new Vector3f();
        final Vector3f convertNormal = new Vector3f();
        final Vector3f convertColor = new Vector3f(1, 1, 1);
        int faceCount = 0;
        for (Element element : model.elements) {
            faceCount += element.faces.size();
        }
        vertexData = new float[66 * faceCount];
        int elementStartIndex;
        for (Element element : model.elements) {
            elementStartIndex = vIndex;
            for (Map.Entry<String, Face> kvp : element.faces.entrySet()) {
                switch (kvp.getKey()) {
                    case "up":
                        convertFrom.set((float) element.from.x, (float) element.to.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.to.x, (float) element.to.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(0, 1, 0), convertColor, false, textAtlas);
                        vIndex += 66;
                        break;
                    case "north":
                        convertFrom.set((float) element.to.x, (float) element.to.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.from.x, (float) element.from.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(0, 0, -1), convertColor, false, textAtlas);
                        vIndex += 66;
                        break;
                    case "west":
                        convertFrom.set((float) element.from.x, (float) element.to.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.from.x, (float) element.from.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(-1, 0, 0), convertColor, true, textAtlas);
                        vIndex += 66;
                        break;
                    case "south":
                        convertFrom.set((float) element.from.x, (float) element.to.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.to.x, (float) element.from.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(0, 0, 1), convertColor, false, textAtlas);
                        vIndex += 66;
                        break;
                    case "east":
                        convertFrom.set((float) element.to.x, (float) element.to.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.to.x, (float) element.from.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(1, 0, 0), convertColor, true, textAtlas);
                        vIndex += 66;
                        break;
                    case "down":
                        convertFrom.set((float) element.to.x, (float) element.from.y, (float) element.from.z).div(16).sub(0.5f, 0f, 0.5f);
                        convertTo.set((float) element.from.x, (float) element.from.y, (float) element.to.z).div(16).sub(0.5f, 0f, 0.5f);
                        createFace(vIndex, convertFrom, convertTo, kvp.getValue(), convertNormal.set(0, -1, 0), convertColor, false, textAtlas);
                        vIndex += 66;
                        break;
                }
            }
            if (element.angle != 0) {
                rotateElement(element.origin, element.axis, (float) Math.toRadians(element.angle), element.rescale, elementStartIndex, vIndex);
            }
        }
    }

    /**
     * Rotates an element around a point.
     *
     * @param origin     origin point to rotate around.
     * @param axis       the axis to rotate around.
     * @param angle      the angle in radians to rotate.
     * @param rescale    should the rotated element be re scaled.
     * @param startIndex vertex data starting index.
     * @param endIndex   vertex data ending index.
     */
    @SuppressWarnings ("Duplicates")
    private void rotateElement(@Nonnull final Vector3d origin,
                               @Nonnull final String axis,
                               final float angle,
                               final boolean rescale,
                               final int startIndex,
                               final int endIndex) {
        float tx, ty, tz;
        final Vector3d nOrigin = new Vector3d(origin).div(16.0).sub(0.5, 0, 0.5);
        final Vector3f rot = new Vector3f();
        switch (axis) {
            case "x":
                rot.set(0, 1, 1);
                if (rescale) {
                    rot.mul(1.0F / (float) Math.cos(angle) - 1.0F).add(1, 1, 1);
                }
                for (int i = startIndex; i < endIndex; i += 11) {
                    // Translate to center
                    vertexData[i + 1] -= nOrigin.y;
                    vertexData[i + 2] -= nOrigin.z;
                    // Rotate
                    ty = (float) (vertexData[i + 1] * Math.cos(angle) - vertexData[i + 2] * Math.sin(angle));
                    tz = (float) (vertexData[i + 1] * Math.sin(angle) + vertexData[i + 2] * Math.cos(angle));
                    vertexData[i + 1] = ty;
                    vertexData[i + 2] = tz;
                    // Translate back
                    vertexData[i + 1] += nOrigin.y;
                    vertexData[i + 2] += nOrigin.z;
                    // Scale
                    vertexData[i + 1] *= rot.y;
                    vertexData[i + 2] *= rot.z;
                    // Rotate normal
                    ty = (float) (vertexData[i + 6] * Math.cos(angle) - vertexData[i + 7] * Math.sin(angle));
                    tz = (float) (vertexData[i + 6] * Math.sin(angle) + vertexData[i + 7] * Math.cos(angle));
                    vertexData[i + 6] = ty;
                    vertexData[i + 7] = tz;
                }
                break;
            case "y":
                rot.set(1, 0, 1);
                if (rescale) {
                    rot.mul(1.0F / (float) Math.cos(angle) - 1.0F).add(1, 1, 1);
                }
                for (int i = startIndex; i < endIndex; i += 11) {
                    // Translate to center
                    vertexData[i] -= nOrigin.x;
                    vertexData[i + 2] -= nOrigin.z;
                    // Rotate
                    tx = (float) (vertexData[i] * Math.cos(angle) - vertexData[i + 2] * Math.sin(angle));
                    tz = (float) (vertexData[i] * Math.sin(angle) + vertexData[i + 2] * Math.cos(angle));
                    vertexData[i] = tx;
                    vertexData[i + 2] = tz;
                    // Translate back
                    vertexData[i] += nOrigin.x;
                    vertexData[i + 2] += nOrigin.z;
                    // Scale
                    vertexData[i] *= rot.x;
                    vertexData[i + 2] *= rot.z;
                    // Rotate normal
                    tx = (float) (vertexData[i + 5] * Math.cos(angle) - vertexData[i + 7] * Math.sin(angle));
                    tz = (float) (vertexData[i + 5] * Math.sin(angle) + vertexData[i + 7] * Math.cos(angle));
                    vertexData[i + 5] = tx;
                    vertexData[i + 7] = tz;
                }
                break;
            case "z":
                rot.set(1, 1, 0);
                if (rescale) {
                    rot.mul(1.0F / (float) Math.cos(angle) - 1.0F).add(1, 1, 1);
                }
                for (int i = startIndex; i < endIndex; i += 11) {
                    // Translate to center
                    vertexData[i] -= nOrigin.y;
                    vertexData[i + 1] -= nOrigin.z;
                    // Rotate
                    tx = (float) (vertexData[i] * Math.cos(angle) - vertexData[i + 1] * Math.sin(angle));
                    ty = (float) (vertexData[i] * Math.sin(angle) + vertexData[i + 1] * Math.cos(angle));
                    vertexData[i] = tx;
                    vertexData[i + 1] = ty;
                    // Translate back
                    vertexData[i] += nOrigin.x;
                    vertexData[i + 1] += nOrigin.y;
                    // Scale
                    vertexData[i] *= rot.x;
                    vertexData[i + 1] *= rot.y;
                    // Rotate normal
                    tx = (float) (vertexData[i + 5] * Math.cos(angle) - vertexData[i + 6] * Math.sin(angle));
                    ty = (float) (vertexData[i + 5] * Math.sin(angle) + vertexData[i + 6] * Math.cos(angle));
                    vertexData[i + 5] = tx;
                    vertexData[i + 6] = ty;
                }
                break;
        }

    }

    /**
     * Creates faces for a rectangle.
     *
     * @param index     insertion point for the vertex data.
     * @param from      vertex starting vertex.
     * @param to        diagonal across ending vertex.
     * @param face      properties for the specified face.
     * @param normal    the face plane normal.
     * @param color     face vertex color.
     * @param zFace     is face parallel to the z axis.
     * @param textAtlas the block texture atlas.
     */
    @SuppressWarnings ("Duplicates")
    private void createFace(int index,
                            @Nonnull final Vector3f from,
                            @Nonnull final Vector3f to,
                            @Nonnull final Face face,
                            @Nonnull final Vector3f normal,
                            @Nonnull final Vector3f color,
                            final boolean zFace,
                            @Nonnull final TextureAtlas textAtlas) {
        Vector4f uvs = textAtlas.getUvMap().get(getTextureId(this, face.textureId));
        final Vector4f faceUvs = new Vector4f((float) face.uv.x, (float) face.uv.y, (float) face.uv.z, (float) face.uv.w).div((float) 16.0);
        faceUvs.x = uvs.x + (Math.abs(uvs.z - uvs.x) * faceUvs.x);
        faceUvs.y = uvs.y + (Math.abs(uvs.w - uvs.y) * faceUvs.y);
        faceUvs.z = uvs.x + (Math.abs(uvs.z - uvs.x) * faceUvs.z);
        faceUvs.w = uvs.y + (Math.abs(uvs.w - uvs.y) * faceUvs.w);
        final float[] nUvs = {faceUvs.x, faceUvs.y, faceUvs.x, faceUvs.w, faceUvs.z, faceUvs.w, faceUvs.z, faceUvs.y};
        rotateVectorN(face.rotation, nUvs);
        // Triangle 1
        createVertexPoint(index, from.x, from.y, from.z, nUvs[0], nUvs[1], normal.x, normal.y, normal.z, color.x, color.y, color.z);
        index += 11;
        createVertexPoint(index, from.x, to.y, zFace ? from.z : to.z, nUvs[2], nUvs[3], normal.x, normal.y, normal.z, color.x, color.y, color.z);
        index += 11;
        createVertexPoint(index, to.x, from.y, zFace ? to.z : from.z, nUvs[6], nUvs[7], normal.x, normal.y, normal.z, color.x, color.y, color.z);
        index += 11;
        // Triangle 2
        createVertexPoint(index, to.x, from.y, zFace ? to.z : from.z, nUvs[6], nUvs[7], normal.x, normal.y, normal.z, color.x, color.y, color.z);
        index += 11;
        createVertexPoint(index, from.x, to.y, zFace ? from.z : to.z, nUvs[2], nUvs[3], normal.x, normal.y, normal.z, color.x, color.y, color.z);
        index += 11;
        createVertexPoint(index, to.x, to.y, to.z, nUvs[4], nUvs[5], normal.x, normal.y, normal.z, color.x, color.y, color.z);
    }

    /**
     * Retrieve the texture id for
     *
     * @param model  the model to get a texture for.
     * @param initId the string texture id.
     * @return the final texture id.
     */
    private String getTextureId(@Nonnull final BlockModel model, @Nonnull final String initId) {
        BlockModel cModel = model;
        while (cModel.textures == null || !cModel.textures.containsKey(initId)) {
            cModel = cModel.parentModel;
        }
        final String textureId = cModel.textures.get(initId);
        if (textureId.startsWith("#")) {
            return getTextureId(model, textureId.replace("#", ""));
        } else {
            return textureId;
        }
    }

    /**
     * Rotates texture coordinates.
     *
     * @param angle     angle in 90 degree offsets to rotate at.
     * @param uvsCoords the texture coordinates to rotate.
     */
    private void rotateVectorN(final float angle, @Nonnull final float[] uvsCoords) {
        final float[] nUvs = new float[uvsCoords.length];
        System.arraycopy(uvsCoords, 0, nUvs, 0, uvsCoords.length);
        switch ((int) angle) {
            case 0:
                break;
            case 90:
                uvsCoords[0] = nUvs[6];
                uvsCoords[1] = nUvs[7];
                uvsCoords[2] = nUvs[0];
                uvsCoords[3] = nUvs[1];
                uvsCoords[4] = nUvs[2];
                uvsCoords[5] = nUvs[3];
                uvsCoords[6] = nUvs[4];
                uvsCoords[7] = nUvs[5];
                break;
            case 180:
                uvsCoords[0] = nUvs[4];
                uvsCoords[1] = nUvs[5];
                uvsCoords[2] = nUvs[6];
                uvsCoords[3] = nUvs[7];
                uvsCoords[4] = nUvs[0];
                uvsCoords[5] = nUvs[1];
                uvsCoords[6] = nUvs[2];
                uvsCoords[7] = nUvs[3];
                break;
            case 270:
                uvsCoords[0] = nUvs[2];
                uvsCoords[1] = nUvs[3];
                uvsCoords[2] = nUvs[4];
                uvsCoords[3] = nUvs[5];
                uvsCoords[4] = nUvs[6];
                uvsCoords[5] = nUvs[7];
                uvsCoords[6] = nUvs[0];
                uvsCoords[7] = nUvs[1];
                break;
        }
    }

    /**
     * Inserts vertex data at the provided index.
     *
     * @param index   the index to insert the data at.
     * @param x       vertex x position.
     * @param y       vertex y position.
     * @param z       vertex z position.
     * @param uvX     x texture uv coordinate.
     * @param uvY     y texture uv coordinate.
     * @param normalX x component for vertex normal.
     * @param normalY y component for vertex normal.
     * @param normalZ z component for vertex normal.
     * @param r       red component.
     * @param g       green component.
     * @param b       blue component.
     */
    @SuppressWarnings ("Duplicates")
    private void createVertexPoint(int index,
                                   final float x,
                                   final float y,
                                   final float z,
                                   final float uvX,
                                   final float uvY,
                                   final float normalX,
                                   final float normalY,
                                   final float normalZ,
                                   final float r,
                                   final float g,
                                   final float b) {
        // Vertex data
        vertexData[index++] = x;
        vertexData[index++] = y;
        vertexData[index++] = z;
        // Texture coordinates
        vertexData[index++] = uvX;
        vertexData[index++] = uvY;
        // Normal data
        vertexData[index++] = normalX;
        vertexData[index++] = normalY;
        vertexData[index++] = normalZ;
        // Color
        vertexData[index++] = r;
        vertexData[index++] = g;
        vertexData[index] = b;
    }

    /**
     * @return interleaved OpenGL vertex data.
     */
    public float[] getVertexData() {
        return vertexData;
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
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureAtlas);
        // Render call
        VertexBufferedObject.drawVboArrays(GL11.GL_TRIANGLES, 0, vertexData.length / 11);
        // Disable pointers
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);
        GL20.glDisableVertexAttribArray(2);
        GL20.glDisableVertexAttribArray(3);
    }
}
