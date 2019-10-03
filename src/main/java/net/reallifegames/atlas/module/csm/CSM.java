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
package net.reallifegames.atlas.module.csm;

import net.reallifegames.atlas.Atlas;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

/**
 * A class to do the cascaded shadow calculations.
 *
 * @author Tyler Bucher
 */
public class CSM {

    /**
     * The OpenGL frame buffer object.
     */
    static int mCascadedShadowFBO;

    /**
     * The number of cascade splits.
     */
    static final int MAX_SPLITS = 4;

    /**
     * The list of texture ids.
     */
    static int[] shadowTextureArray = new int[MAX_SPLITS];

    /**
     * The change in shadow clipping distance. Between 0 and 1, change in order to see the results.
     */
    static float lambda = 1.0f;

    /**
     * The min distance for shadow clipping. Between 0 and 1, change these to check the results.
     */
    private static float minDistance = 0.0f;

    /**
     * The max distance for shadow clipping. Between 0 and 1, change these to check the results.
     */
    private static float maxDistance = 1.0f;

    /**
     * The view matrices for each cascade split.
     */
    static final Matrix4f[] cascadedMatrices = new Matrix4f[MAX_SPLITS];

    /**
     * The positions at which the cascades split.
     */
    static final float[] cascadeSplitArray = new float[MAX_SPLITS];

    /**
     * The camera position vector.
     */
    private static final Vector3f pos = new Vector3f();

    /**
     * The projection look at vector.
     */
    private static final Vector3f lookAT = new Vector3f();

    /**
     * Setup code for OpenGL.
     *
     * @param mShadowMapSize the size of the shadow map texture.
     */
    static void init(final int mShadowMapSize) {

        // Directional light shadow map buffer
        mCascadedShadowFBO = GL30.glGenFramebuffers();
        GL11.glGenTextures(shadowTextureArray);
        for (int i = 0; i < MAX_SPLITS; i++) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, shadowTextureArray[i]);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_DEPTH_COMPONENT, mShadowMapSize, mShadowMapSize, 0, GL11.GL_DEPTH_COMPONENT, GL11.GL_FLOAT, (ByteBuffer) null);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL14.GL_TEXTURE_COMPARE_MODE, GL11.GL_LINEAR);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
            float[] borderColor = {1.0f, 1.0f, 1.0f, 1.0f};
            GL11.glTexParameterfv(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_BORDER_COLOR, borderColor);
        }
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, mCascadedShadowFBO);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, shadowTextureArray[0], 0);
        GL11.glDrawBuffer(GL11.GL_NONE);
        GL11.glReadBuffer(GL11.GL_NONE);
        // restore default FBO
        GL30.glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, 0);
    }

    /**
     * Updates the current state of the csm shadows.
     *
     * @param mShadowMapSize    the size of the shadow map.
     * @param near              the camera near field.
     * @param far               the camera far field.
     * @param mProjectionMatrix the camera projection matrix.
     * @param viewMatrix        the camera view matrix.
     */
    static void updateCCSM(final int mShadowMapSize,
                           final float near,
                           final float far,
                           @Nonnull final Matrix4fc mProjectionMatrix,
                           @Nonnull final Matrix4fc viewMatrix) {
        float clipRange = far - near;

        float minZ = near + minDistance * clipRange;
        float maxZ = near + maxDistance * clipRange;

        float range = maxZ - minZ;
        float ratio = maxZ / minZ;
        float[] cascadeSplits = new float[MAX_SPLITS];
        for (int i = 0; i < MAX_SPLITS; ++i) {
            float p = (float) (i + 1) / (float) MAX_SPLITS;
            float log = (float) (minZ * Math.pow(ratio, p));
            float uniform = minZ + range * p;
            float d = lambda * (log - uniform) + uniform;
            cascadeSplits[i] = (d - near) / clipRange;
        }

        for (int cascadeIterator = 0; cascadeIterator < MAX_SPLITS; cascadeIterator++) {
            float prevSplitDistance = cascadeIterator == 0 ? minDistance : cascadeSplits[cascadeIterator - 1];
            float splitDistance = cascadeSplits[cascadeIterator];

            final Vector3f[] frustumCornersWS = {
                    new Vector3f(-1.0f, 1.0f, -1.0f),
                    new Vector3f(1.0f, 1.0f, -1.0f),
                    new Vector3f(1.0f, -1.0f, -1.0f),
                    new Vector3f(-1.0f, -1.0f, -1.0f),
                    new Vector3f(-1.0f, 1.0f, 1.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(1.0f, -1.0f, 1.0f),
                    new Vector3f(-1.0f, -1.0f, 1.0f),
            };

            Matrix4f invViewProjection = new Matrix4f(mProjectionMatrix).mul(viewMatrix).invert();
            Vector4f inversePoint = new Vector4f();
            for (Vector3f frustumCornersW : frustumCornersWS) {
                inversePoint.set(frustumCornersW, 1.0f).mul(invViewProjection).div(inversePoint.w);
                frustumCornersW.set(inversePoint.x, inversePoint.y, inversePoint.z);
            }

            Vector3f cornerRay = new Vector3f();
            Vector3f nearCornerRay = new Vector3f();
            Vector3f farCornerRay = new Vector3f();
            for (int i = 0; i < 4; ++i) {
                cornerRay.set(frustumCornersWS[i + 4]).sub(frustumCornersWS[i]);
                nearCornerRay.set(cornerRay).mul(prevSplitDistance);
                farCornerRay.set(cornerRay).mul(splitDistance);
                frustumCornersWS[i + 4].set(frustumCornersWS[i]).add(farCornerRay);
                frustumCornersWS[i].set(frustumCornersWS[i]).add(nearCornerRay);
            }

            Vector3f frustumCenter = new Vector3f();
            for (int i = 0; i < 8; ++i) {
                frustumCenter.add(frustumCornersWS[i]);
            }
            frustumCenter.div(8.0f);

            float radius = 0.0f;
            Vector3f length = new Vector3f();
            for (int i = 0; i < 8; ++i) {
                float distance = length.set(frustumCornersWS[i]).sub(frustumCenter).length();
                radius = Math.max(radius, distance);
            }
            radius = (float) (Math.ceil(radius * 16.0f) / 16.0f);

            final Vector3f maxExtents = new Vector3f(radius, radius, radius);
            final Vector3f minExtents = new Vector3f(maxExtents).negate();

            //Position the view matrix looking down the center of the frustum with an arbitrary light direction
            Vector3f lightDirVec = new Vector3f(Atlas.lightPositionProperty.getProperty()).normalize();
            float llhypot = (float) Math.sqrt((lightDirVec.x * lightDirVec.x) + (lightDirVec.z * lightDirVec.z));
            float theta = (float) Math.atan(lightDirVec.y / llhypot);

            float nn = Math.max(60.0f - frustumCenter.y, -minExtents.z);
            float nl = (float) (nn / Math.sin(theta));

            Vector3f lightDirection = new Vector3f(frustumCenter).sub(new Vector3f(Atlas.lightPositionProperty.getProperty()).negate().normalize().mul(nl));
            Matrix4f lightViewMatrix;
            lightViewMatrix = new Matrix4f().lookAt(lightDirection, frustumCenter, new Vector3f(0.0f, 1.0f, 0.0f));

            pos.set(lightDirection);
            lookAT.set(frustumCenter);

            Matrix4f lightOrthoMatrix = new Matrix4f().ortho(minExtents.x, maxExtents.x, minExtents.y, maxExtents.y, 0, nl * 2.0f);

            // The rounding matrix that ensures that shadow edges do not shimmer
            Matrix4f shadowMatrix = new Matrix4f(lightOrthoMatrix).mul(lightViewMatrix);
            Vector4f shadowOrigin = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
            shadowOrigin.mul(shadowMatrix, shadowOrigin);
            shadowOrigin.mul(mShadowMapSize / 2.0f, shadowOrigin);

            Vector4f roundedOrigin = new Vector4f(Math.round(shadowOrigin.x), Math.round(shadowOrigin.y), Math.round(shadowOrigin.z), Math.round(shadowOrigin.w));
            Vector4f roundOffset = new Vector4f(roundedOrigin).sub(shadowOrigin);
            roundOffset.mul(2.0f).div(mShadowMapSize);
            roundOffset.z = 0.0f;
            roundOffset.w = 0.0f;

            Matrix4f shadowProjection = new Matrix4f(lightOrthoMatrix);
            shadowProjection.m30(shadowProjection.m30() + roundOffset.x);
            shadowProjection.m31(shadowProjection.m31() + roundOffset.y);
            shadowProjection.m32(shadowProjection.m32() + roundOffset.z);
            shadowProjection.m33(shadowProjection.m33() + roundOffset.w);
            lightOrthoMatrix = shadowProjection;

            //Store the split distances and the relevant matrices
            float clipDist = far - near;
            cascadeSplitArray[cascadeIterator] = (near + splitDistance * clipDist);
            cascadedMatrices[cascadeIterator] = new Matrix4f(lightOrthoMatrix).mul(lightViewMatrix);
        }
    }
}
