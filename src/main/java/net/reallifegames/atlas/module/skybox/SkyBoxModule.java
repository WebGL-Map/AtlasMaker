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
package net.reallifegames.atlas.module.skybox;

import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.TextureLoader;
import net.reallifegames.atlas.TextureManager;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import net.reallifegames.atlas.modules.CameraModule;
import org.ajgl.graphics.shaders.Shader;
import org.ajgl.graphics.shaders.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.nio.FloatBuffer;

@ModuleInfo ({"CameraModule"})
public class SkyBoxModule implements Module {

    /**
     * The SkyBox to be rendered with this module.
     */
    private final SkyBox skybox;

    /**
     * The mvp matrix for the SkyBox shader program.
     */
    private final Matrix4f mvpSky;

    /**
     * A temporary matrix to do calculations with.
     */
    private final Matrix4f calcMatrix;

    /**
     * Vertex shader for the SkyBox shader program.
     */
    private Shader vertexShaderSky;

    /**
     * Fragment shader for the SkyBox shader program.
     */
    private Shader fragmentShaderSky;

    /**
     * SkyBox shader program for rendering the grids.
     */
    private ShaderProgram shaderProgramSky;

    /**
     * SkyBox shader program MVP uniform.
     */
    private int uniformSkyMvp;

    /**
     * A module to help manage a SkyBox.
     *
     * @param args constructor arguments.
     */
    public SkyBoxModule(@Nonnull final String[] args) {
        calcMatrix = new Matrix4f();
        mvpSky = new Matrix4f();
        // Load camera module
        Atlas.moduleLoader.getModule("CameraModule").ifPresent(module->{
            if (module instanceof CameraModule) {
                final CameraModule cameraModule = (CameraModule) module;
                // Initialize mvp matrix
                mvpSky.set(cameraModule.getIdentityProjection()).mul(calcMatrix.set(cameraModule.getViewMatrix()).m30(0).m31(0).m32(0));
                // Modify sky-box mvp when camera mvp changes
                cameraModule.getMvpProperty().addListener(((oldValue, newValue)->{
                    mvpSky.set(cameraModule.getIdentityProjection()).mul(calcMatrix.set(cameraModule.getViewMatrix()).m30(0).m31(0).m32(0));
                    // Update OpenGL matrix
                    try (final MemoryStack stack = MemoryStack.stackPush()) {
                        final FloatBuffer dataBuffer = stack.callocFloat(16);
                        mvpSky.get(dataBuffer);
                        GL20.glUseProgram(shaderProgramSky.id);
                        GL20.glUniformMatrix4fv(uniformSkyMvp, false, dataBuffer);
                    }
                }));
            }
        });
        // Setup OpenGL environment
        setup();
        this.skybox = new SkyBox(1, TextureManager.getTexture("skyBox"));
    }

    /**
     * OpenGL setup code.
     */
    @SuppressWarnings ("Duplicates")
    private void setup() {
        // Register sky box texture
        TextureManager.registerTexture("skyBox", Atlas.TEXTURE_DIR, (integer, imgPath)->{
            try {
                GL11.glBindTexture(GL13.GL_TEXTURE_CUBE_MAP, integer);
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_X, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "west.png", 3));
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_X, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "east.png", 3));
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Y, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "top.png", 3));
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Y, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "bottom.png", 3));
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_POSITIVE_Z, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "north.png", 3));
                GL11.glTexImage2D(GL13.GL_TEXTURE_CUBE_MAP_NEGATIVE_Z, 0, GL11.GL_RGB, 2048, 2048, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath + "south.png", 3));

                GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
                GL11.glTexParameteri(GL13.GL_TEXTURE_CUBE_MAP, GL12.GL_TEXTURE_WRAP_R, GL12.GL_CLAMP_TO_EDGE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        // Create shaders
        try {
            // Load Sky-box GLSL Shaders
            vertexShaderSky = Shader.loadShader(GL20.GL_VERTEX_SHADER, "../src/main/resources/shaders/VERTEX_SHADER_SKY.glsl");
            fragmentShaderSky = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "../src/main/resources/shaders/FRAGMENT_SHADER_SKY.glsl");
            // Verify shader compile status
            if (!vertexShaderSky.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(vertexShaderSky.id, 1024));
            }
            if (!fragmentShaderSky.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(fragmentShaderSky.id, 1024));
            }
            // Create shader program
            shaderProgramSky = new ShaderProgram();
            shaderProgramSky.attachShader(vertexShaderSky);
            shaderProgramSky.attachShader(fragmentShaderSky);
            // Bind shader attributes
            GL20.glBindAttribLocation(shaderProgramSky.id, 0, "position");
            // Link and bind shader program
            shaderProgramSky.link();
            shaderProgramSky.validate();
            // Check status of shader program
            if (!shaderProgramSky.verify()) {
                throw new Exception("shader program error " + GL20.glGetProgramInfoLog(shaderProgramSky.id, 1024));
            }
            // Set shader uniforms
            GL20.glUseProgram(shaderProgramSky.id);
            uniformSkyMvp = GL20.glGetUniformLocation(shaderProgramSky.id, "mvp");
            if (uniformSkyMvp != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniformSkyMvp, false, mvpSky.get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [sky] unable to find uniform [mvp]");
            }
            // Shader texture sampler uniform
            int uniformTextureSampler = GL20.glGetUniformLocation(shaderProgramSky.id, "texSampler");
            if (uniformTextureSampler != -1) {
                GL20.glUniform1i(uniformTextureSampler, 0);
            } else {
                System.err.println("Shader program [sky] unable to find uniform [texSampler]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void input(double displacement) {

    }

    @Override
    public void update(double displacement) {

    }

    @Override
    public void render(double displacement) {
        GL20.glUseProgram(shaderProgramSky.id);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        this.skybox.draw();
        GL11.glDepthFunc(GL11.GL_LESS);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
