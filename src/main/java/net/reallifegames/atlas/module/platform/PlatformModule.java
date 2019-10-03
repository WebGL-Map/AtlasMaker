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
import net.reallifegames.atlas.TextureLoader;
import net.reallifegames.atlas.TextureManager;
import net.reallifegames.atlas.listenable.GameProperty;
import net.reallifegames.atlas.listenable.Property;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import net.reallifegames.atlas.modules.CameraModule;
import net.reallifegames.atlas.renderable.Renderable;
import org.ajgl.graphics.shaders.Shader;
import org.ajgl.graphics.shaders.ShaderProgram;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * A module for dealing with the platform object.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ("CameraModule")
public class PlatformModule implements Module {

    /**
     * The platform to hold blocks and help with perspective.
     */
    private final Platform platform;

    /**
     * Property for rendering the platform.
     */
    private final Property<Boolean> renderPlatformProperty = new Property<>(Boolean.TRUE);

    /**
     * Vertex shader for the basic shader program.
     */
    private Shader vertexShaderBasic;

    /**
     * Fragment shader for the basic shader program.
     */
    private Shader fragmentShaderBasic;

    /**
     * Basic shader program for rendering the grids.
     */
    private ShaderProgram shaderProgramBasic;

    /**
     * The MVP matrix uniform.
     */
    private int uniformBasicMvp;

    /**
     * The module for the camera object.
     */
    private CameraModule cameraModule;

    /**
     * The property used for displaying the grid.
     */
    private final GameProperty<Boolean> useGridProperty;

    /**
     * The property used for displaying the micro grid.
     */
    private final GameProperty<Boolean> useMicroGridProperty;

    /**
     * List of objects to render when the render function is called.
     */
    private final List<Renderable> renderableList;

    /**
     * Creates a new module for the platform object.
     *
     * @param args constructor arguments.
     */
    public PlatformModule(@Nonnull final String[] args) {
        renderableList = new ArrayList<>();
        // Load camera module
        Atlas.moduleLoader.getModule("CameraModule").ifPresent(module->{
            if (module instanceof CameraModule) {
                cameraModule = (CameraModule) module;
                // Modify sky-box mvp when camera mvp changes
                cameraModule.getMvpProperty().addListener(((oldValue, newValue)->{
                    // Update OpenGL matrix
                    try (final MemoryStack stack = MemoryStack.stackPush()) {
                        final FloatBuffer dataBuffer = stack.callocFloat(16);
                        newValue.get(dataBuffer);
                        GL20.glUseProgram(shaderProgramBasic.id);
                        GL20.glUniformMatrix4fv(uniformBasicMvp, false, dataBuffer);
                    }
                }));
            }
        });
        // Setup OpenGL environment
        setup();
        // Create platform
        platform = new Platform(3, -1, new Vector3f(1, 1, 1), TextureManager.getTexture("marble"), true);
        useGridProperty = new GameProperty<>(Boolean.FALSE);
        useMicroGridProperty = new GameProperty<>(Boolean.FALSE);
        // Grid render list listeners
        useGridProperty.addListener((oldValue, newValue)->{
            if (newValue) {
                renderableList.add(platform.getGrid());
            } else {
                renderableList.remove(platform.getGrid());
            }
        });
        useMicroGridProperty.addListener((oldValue, newValue)->{
            if (newValue) {
                renderableList.add(platform.getMicroGrid());
            } else {
                renderableList.remove(platform.getMicroGrid());
            }
        });
        // Grid setup
        setGridRadius(1.5f);
        setGridColor(0, 0, 0);
        setMicroGridRadius(1.5f);
        setMicroGridColor(0, 0, 0);
    }

    /**
     * Setup code for OpenGL data.
     */
    @SuppressWarnings ("Duplicates")
    private void setup() {
        // Register platform texture
        TextureManager.registerTexture("marble", Atlas.TEXTURE_DIR + "marble3.png", (integer, imgPath)->{
            try {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, integer);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 1024, 1024, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath, 3));
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Create shaders
        try {
            // Load platform shaders
            vertexShaderBasic = Shader.loadShader(GL20.GL_VERTEX_SHADER, "../src/main/resources/shaders/VERTEX_SHADER_BASIC.glsl");
            fragmentShaderBasic = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "../src/main/resources/shaders/FRAGMENT_SHADER_BASIC.glsl");
            // Verify shader compile status
            if (!vertexShaderBasic.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(vertexShaderBasic.id, 1024));
            }
            if (!fragmentShaderBasic.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(fragmentShaderBasic.id, 1024));
            }
            // Create shader program
            shaderProgramBasic = new ShaderProgram();
            shaderProgramBasic.attachShader(vertexShaderBasic);
            shaderProgramBasic.attachShader(fragmentShaderBasic);
            // Bind shader attributes
            GL20.glBindAttribLocation(shaderProgramBasic.id, 0, "position");
            GL20.glBindAttribLocation(shaderProgramBasic.id, 1, "color");
            // Link and bind shader program
            shaderProgramBasic.link();
            shaderProgramBasic.validate();
            // Check status of shader program
            if (!shaderProgramBasic.verify()) {
                throw new Exception("shader program error");
            }
            // Set shader uniforms
            GL20.glUseProgram(shaderProgramBasic.id);
            uniformBasicMvp = GL20.glGetUniformLocation(shaderProgramBasic.id, "mvp");
            // Shader mvp uniform
            if (uniformBasicMvp != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniformBasicMvp, false, cameraModule.getMvpProperty().getProperty().get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [basic] unable to find uniform [mvp]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @return the use grid property used for displaying the micro grid.
     */
    public Property<Boolean> getUseMicroGridProperty() {
        return useMicroGridProperty;
    }

    /**
     * @return the use grid property used for displaying the grid.
     */
    public Property<Boolean> getUseGridProperty() {
        return useGridProperty;
    }

    /**
     * @return the property for rendering the platform object.
     */
    public Property<Boolean> getRenderPlatformProperty() {
        return renderPlatformProperty;
    }

    /**
     * @return the platform object.
     */
    public Platform getPlatform() {
        return platform;
    }

    /**
     * Set the outer platform's radius.
     *
     * @param radius the new radius of the platform.
     */
    public void setPlatformRadius(final float radius) {
        platform.setRadius(radius);
    }

    /**
     * Changes the color of the platform.
     *
     * @param r red color component.
     * @param g green color component.
     * @param b blue color component.
     */
    public void setPlatformColor(final float r, final float g, final float b) {
        platform.setColor(r, g, b);
    }

    /**
     * Changes the color of the outer grid.
     *
     * @param r red color component.
     * @param g green color component.
     * @param b blue color component.
     */
    public void setGridColor(final float r, final float g, final float b) {
        platform.getGrid().setColor(r, g, b);
    }

    /**
     * Set the outer grid's radius.
     *
     * @param radius the new radius of the outer grid.
     */
    public void setGridRadius(final float radius) {
        platform.getGrid().setRadius(radius, radius);
    }

    /**
     * Changes the color of the inner grid.
     *
     * @param r red color component.
     * @param g green color component.
     * @param b blue color component.
     */
    public void setMicroGridColor(final float r, final float g, final float b) {
        platform.getMicroGrid().setColor(r, g, b);
    }

    /**
     * Set the inner grid's radius.
     *
     * @param radius the new radius of the inner grid.
     */
    public void setMicroGridRadius(final float radius) {
        platform.getMicroGrid().setRadius(radius, radius - 0.9375f);
    }

    @Override
    public void input(final double displacement) {

    }

    @Override
    public void update(final double displacement) {
        platform.update();
        platform.getGrid().update();
        platform.getMicroGrid().update();
        useGridProperty.updateProperty();
        useMicroGridProperty.updateProperty();
    }

    @Override
    public void render(final double displacement) {
        if (renderPlatformProperty.getProperty()) {
            platform.draw();
        }
    }

    /**
     * Renders the platform grids.
     *
     * @param displacement the amount of time difference from the previous to the current tick.
     */
    public void renderGrids(final double displacement) {
        GL20.glUseProgram(shaderProgramBasic.id);
        renderableList.forEach(Renderable::draw);
    }
}
