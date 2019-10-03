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
package net.reallifegames.atlas.modules;

import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.listenable.GameProperty;
import net.reallifegames.atlas.listenable.Property;
import net.reallifegames.atlas.listenable.properties.Matrix4fProperty;
import net.reallifegames.atlas.listenable.properties.Vector3fProperty;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

/**
 * A class the help manage camera fields and properties.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ("")
public class CameraModule implements Module {

    /**
     * A temporary calculation matrix.
     */
    private Matrix4f tempMatrix = new Matrix4f();

    /**
     * The cameras current arc positions.
     */
    private volatile Vector3f cameraArc = new Vector3f(0.3926991f, 0.0f, 2.0f);

    /**
     * The original projection matrix of the camera.
     */
    private Matrix4f identityProjection;

    /**
     * The independent view matrix of the camera.
     */
    private Matrix4f viewMatrix = new Matrix4f().arcball(cameraArc.z, 0, 0, 0, cameraArc.x, cameraArc.y);

    /**
     * The rotation information for auto rotating the camera.
     */
    private volatile Vector3f autoRotateVector = new Vector3f(cameraArc.x, 1.5f, cameraArc.z);

    /**
     * The position of the camera.
     */
    private Vector3f cameraPosition = new Vector3f();

    /**
     * The camera model view projection matrix.
     */
    private Matrix4fProperty mvpProperty;

    /**
     * The rotation information for auto rotating the camera.
     */
    private final Vector3fProperty autoRotateVectorProperty;

    /**
     * States the the autoRotateVector needs to be changed.
     */
    private final GameProperty<Boolean> autoRotateProperty;

    /**
     * A full rotation in radians.
     */
    private final double radiansCycle = Math.toRadians(360.0);

    /**
     * Right angle in radians.
     */
    private final double radiansRightAngle = Math.toRadians(90.0);

    /**
     * Updates the mvp matrix.
     */
    private volatile boolean updateMvp = false;

    /**
     * Creates a new CameraModule to help manage the camera fields.
     *
     * @param args constructor arguments.
     */
    public CameraModule(@Nonnull final String[] args) {
        identityProjection = new Matrix4f().setPerspective((float) Math.toRadians(45.0f), Atlas.width / Atlas.height, 0.1f, 30);
        mvpProperty = new Matrix4fProperty(new Matrix4f());
        mvpProperty.setProperty(tempMatrix.set(identityProjection).mul(viewMatrix));
        autoRotateVectorProperty = new Vector3fProperty(autoRotateVector);
        autoRotateProperty = new GameProperty<>(Boolean.FALSE);

        viewMatrix.invert(tempMatrix);
        cameraPosition.set(tempMatrix.m30(), tempMatrix.m31(), tempMatrix.m32());
    }

    /**
     * @return the mvp property notifier.
     */
    public Matrix4fProperty getMvpProperty() {
        return mvpProperty;
    }

    /**
     * Changes the value of the auto rotate property.
     *
     * @param value the new value of the property.
     */
    public void updateAutoRotate(final boolean value) {
        autoRotateProperty.setProperty(value);
    }

    /**
     * Changes the position of the auto rotation vector.
     *
     * @param pitch  the new pitch of the camera.
     * @param radius the new radius of the camera.
     */
    public void updateAutoRotatePosition(final float pitch, final float radius) {
        autoRotateVector.set(pitch, autoRotateVector.y, radius);
        cameraArc.set(autoRotateVector.x, cameraArc.y, autoRotateVector.z);
        updateMvp = true;
    }

    /**
     * Changes how fast to rotate the camera.
     *
     * @param value the new speed to rotate at.
     */
    public void updateAutoRotateSpeed(final float value) {
        autoRotateVector.y = value;
        updateMvp = true;
    }

    /**
     * @return the auto rotate vector notifier.
     */
    public Vector3fProperty getAutoRotateVectorProperty() {
        return autoRotateVectorProperty;
    }

    /**
     * @return the auto rotate boolean notifier.
     */
    public Property<Boolean> getAutoRotateProperty() {
        return autoRotateProperty;
    }

    /**
     * @return the current state of the view matrix.
     */
    public Matrix4fc getViewMatrix() {
        return viewMatrix;
    }

    /**
     * @return the current position of the camera.
     */
    public Vector3fc getCameraPosition() {
        return cameraPosition;
    }

    /**
     * @return the original projection matrix.
     */
    public Matrix4fc getIdentityProjection() {
        return identityProjection;
    }

    /**
     * @return the current state of the camera arc angle.
     */
    public Vector3fc getCameraArc() {
        return cameraArc;
    }

    @Override
    public void input(final double displacement) {
        // Camera A key callback
        int state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_A);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            updateAutoRotate(false);
            cameraArc.y += autoRotateVector.y * displacement;
            if (cameraArc.y > radiansCycle) {
                cameraArc.y -= radiansCycle;
            }
            updateMvp = true;
        }
        // Camera S key callback
        state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_S);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            cameraArc.z += autoRotateVector.y * displacement;
            autoRotateVector.z = cameraArc.z;
            updateMvp = true;
        }
        // Camera D key callback
        state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_D);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            updateAutoRotate(false);
            cameraArc.y -= autoRotateVector.y * displacement;
            if (cameraArc.y < -radiansCycle) {
                cameraArc.y += radiansCycle;
            }
            updateMvp = true;
        }
        // Camera W key callback
        state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_W);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            if (cameraArc.z > 0) {
                cameraArc.z -= autoRotateVector.y * displacement;
                autoRotateVector.z = cameraArc.z;
                updateMvp = true;
            }
        }
        // Camera SPACE key callback
        state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_SPACE);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            if (cameraArc.x < radiansRightAngle) {
                cameraArc.x += autoRotateVector.y * displacement;
                autoRotateVector.x = cameraArc.x;
                updateMvp = true;
            }
        }
        // Camera LEFT_CONTROL key callback
        state = GLFW.glfwGetKey(Atlas.windowTest.getWindowHandler(), GLFW.GLFW_KEY_LEFT_CONTROL);
        if ((state & GLFW.GLFW_PRESS) == 1) {
            if (cameraArc.x > -radiansRightAngle) {
                cameraArc.x -= autoRotateVector.y * displacement;
                autoRotateVector.x = cameraArc.x;
                updateMvp = true;
            }
        }
        // Auto rotate the camera if necessary
        if (autoRotateProperty.getProperty()) {
            cameraArc.y -= autoRotateVector.y * displacement;
            if (cameraArc.y < -radiansCycle) {
                cameraArc.y += radiansCycle;
            }
            updateMvp = true;
        }
    }

    @Override
    public void update(final double displacement) {
        if (updateMvp) {
            updateMvp = false;
            viewMatrix.identity().arcball(cameraArc.z, 0, 0, 0, cameraArc.x, cameraArc.y);
            viewMatrix.invert(tempMatrix);
            cameraPosition.set(tempMatrix.m30(), tempMatrix.m31(), tempMatrix.m32());
            mvpProperty.setProperty(tempMatrix.set(identityProjection).mul(viewMatrix));
            autoRotateVectorProperty.setProperty(autoRotateVector);

            autoRotateProperty.updateProperty();
            autoRotateVectorProperty.updateProperty();
            mvpProperty.updateProperty();
        }
    }

    @Override
    public void render(final double displacement) {

    }
}
