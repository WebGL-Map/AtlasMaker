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
package net.reallifegames.atlas.module.postprocessing;

import net.reallifegames.atlas.Atlas;
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import net.reallifegames.atlas.module.skybox.SkyBoxModule;
import net.reallifegames.atlas.module.csm.CSMModule;
import org.ajgl.graphics.shaders.Shader;
import org.ajgl.graphics.shaders.ShaderProgram;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

import javax.annotation.Nonnull;
import java.nio.ByteBuffer;

/**
 * A module for dealing with the post processing effects.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ({"CSMModule", "SkyBoxModule"})
public class PostProcessingModule implements Module {

    /**
     * The frame buffer to render the world into pre post processing.
     */
    private int intermediateFBO;

    /**
     * The id of the texture used to render to the PostProcessingQuad.
     */
    private int screenTexture;

    /**
     * Depth renderbuffer attachment.
     */
    private int depthRbo;

    /**
     * Vertex shader for the fxaa shader program.
     */
    private Shader vertexShaderFxaa;

    /**
     * Fragment shader for the fxaa shader program.
     */
    private Shader fragmentShaderFxaa;

    /**
     * FXAA shader program.
     */
    private ShaderProgram shaderProgramFxaa;

    /**
     * 2d quad for post processing effects
     */
    private PostProcessingQuad ppQuad;

    /**
     * CSMModule dependency instance.
     */
    private CSMModule csmModule;

    /**
     * SkyBoxModule dependency instance.
     */
    private SkyBoxModule skyboxModule;

    /**
     * Creates a new module for the PostProcessingQuad.
     *
     * @param args constructor arguments.
     */
    public PostProcessingModule(@Nonnull final String[] args) {
        Atlas.moduleLoader.getModule("CSMModule").ifPresent(module->{
            if (module instanceof CSMModule) {
                csmModule = (CSMModule) module;
            }
        });
        Atlas.moduleLoader.getModule("SkyBoxModule").ifPresent(module->{
            if (module instanceof SkyBoxModule) {
                skyboxModule = (SkyBoxModule) module;
            }
        });
        ppQuad = new PostProcessingQuad();
        setup();
    }

    /**
     * Setup code for OpenGL data.
     */
    @SuppressWarnings ("Duplicates")
    private void setup() {
        intermediateFBO = GL30.glGenFramebuffers();
        // create a color attachment texture
        screenTexture = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTexture);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, (int) Atlas.width, (int) Atlas.height, 0, GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        depthRbo = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthRbo);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL11.GL_DEPTH_COMPONENT, (int) Atlas.width, (int) Atlas.height);
        // Check intermediateFBO
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, intermediateFBO);
        GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, screenTexture, 0);    // we only need a color buffer
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthRbo);
        if (GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER) != GL30.GL_FRAMEBUFFER_COMPLETE) {
            System.out.println("ERROR");
        }
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        // Create shaders
        try {
            // Load platform shaders
            vertexShaderFxaa = Shader.loadShader(GL20.GL_VERTEX_SHADER, "../src/main/resources/shaders/fxaa/VERTEX_FXAA.glsl");
            fragmentShaderFxaa = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "../src/main/resources/shaders/fxaa/FRAGMENT_FXAA.glsl");
            // Verify shader compile status
            if (!vertexShaderFxaa.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(vertexShaderFxaa.id, 1024));
            }
            if (!fragmentShaderFxaa.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(fragmentShaderFxaa.id, 1024));
            }
            // Create shader program
            shaderProgramFxaa = new ShaderProgram();
            shaderProgramFxaa.attachShader(vertexShaderFxaa);
            shaderProgramFxaa.attachShader(fragmentShaderFxaa);
            // Bind shader attributes
            GL20.glBindAttribLocation(shaderProgramFxaa.id, 0, "position");
            GL20.glBindAttribLocation(shaderProgramFxaa.id, 1, "texcoord");
            // Link and bind shader program
            shaderProgramFxaa.link();
            shaderProgramFxaa.validate();
            // Check status of shader program
            if (!shaderProgramFxaa.verify()) {
                throw new Exception("shader program error");
            }
            // Set shader uniforms
            GL20.glUseProgram(shaderProgramFxaa.id);
            // viewport size uniform
            int fxaaScreen = GL20.glGetUniformLocation(shaderProgramFxaa.id, "uViewportSize");
            if (fxaaScreen != -1) {
                GL20.glUniform2f(fxaaScreen, Atlas.width, Atlas.height);
            } else {
                System.err.println("Shader program [fxaa] unable to find uniform [uViewportSize]");
            }
            // texture map for fxaa processing
            int fxaaTex = GL20.glGetUniformLocation(shaderProgramFxaa.id, "textureMap");
            if (fxaaTex != -1) {
                GL20.glUniform1i(fxaaTex, 0);
            } else {
                System.err.println("Shader program [fxaa] unable to find uniform [textureMap]");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void input(final double displacement) {

    }

    @Override
    public void update(final double displacement) {

    }

    @Override
    public void render(final double displacement) {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        csmModule.renderShadows(displacement);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, intermediateFBO);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        csmModule.render(displacement);
        skyboxModule.render(displacement);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
        GL20.glUseProgram(shaderProgramFxaa.id);
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTexture);
        ppQuad.draw();
    }
}
