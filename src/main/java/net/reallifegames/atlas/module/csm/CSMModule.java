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
import net.reallifegames.atlas.module.Module;
import net.reallifegames.atlas.module.ModuleInfo;
import net.reallifegames.atlas.module.fx.FxModule;
import net.reallifegames.atlas.module.platform.PlatformModule;
import net.reallifegames.atlas.modules.CameraModule;
import org.ajgl.graphics.shaders.Shader;
import org.ajgl.graphics.shaders.ShaderProgram;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import javax.annotation.Nonnull;
import java.nio.FloatBuffer;

/**
 * Helps manage the CSM shadowing functions.
 *
 * @author Tyler Bucher
 */
@ModuleInfo ({"CameraModule", "PlatformModule", "FxModule"})
public class CSMModule implements Module {

    /**
     * Vertex shader for the Csm shader program.
     */
    private Shader vertexShaderCsm;

    /**
     * Fragment shader for the Csm shader program.
     */
    private Shader fragmentShaderCsm;

    /**
     * Vertex shader for the Light shader program.
     */
    private Shader vertexShaderLight;

    /**
     * Fragment shader for the Light shader program.
     */
    private Shader fragmentShaderLight;

    /**
     * Light shader program.
     */
    private ShaderProgram shaderProgramLight;

    /**
     * Csm shader program.
     */
    private ShaderProgram shaderProgramCsm;

    /**
     * Light shader program mvp matrix uniform.
     */
    private int uniLightMvp;

    /**
     * Csm shader program mvp matrix uniform.
     */
    private int uniCsmMvp;

    /**
     * Csm shader program ambient amount uniform.
     */
    private int uniCsmAmbientAmount;

    /**
     * Csm shader program use lighting uniform.
     */
    private int uniCsmEnableLighting;

    /**
     * Csm shader program sun position uniform.
     */
    private int uniCsmSunPos;

    /**
     * Csm shader program camera position uniform.
     */
    private int uniCsmCampos;

    /**
     * Csm shader program sun color uniform.
     */
    private int uniCsmSunColor;

    /**
     * Csm shader program shadow map handler uniform.
     */
    private int uniCsmShadowMap;

    /**
     * Csm shader program view matrix uniform.
     */
    private int uniCsmViewMat;

    /**
     * Csm shader program inverse view matrix uniform.
     */
    private int uniCsmInverseViewMat;

    /**
     * Csm shader program light view matrix uniform.
     */
    private int uniCsmLightVpMat;

    /**
     * Csm shader program cascade split uniform.
     */
    private int uniCsmCascadeSplits;

    /**
     * A matrix used for temporary calculations.
     */
    private Matrix4f calcMatrix;

    /**
     * The CameraModule for rendering.
     */
    private CameraModule cameraModule;

    /**
     * The PlatformModule for rendering.
     */
    private PlatformModule platformModule;

    /**
     * The FxModule for rendering.
     */
    private FxModule fxModule;

    /**
     * States if the CSM class needs to be updated.
     */
    private boolean updateCsm = false;

    /**
     * @param args constructor arguments.
     */
    public CSMModule(@Nonnull final String[] args) {
        calcMatrix = new Matrix4f();
        Atlas.moduleLoader.getModule("CameraModule").ifPresent(module->{
            if (module instanceof CameraModule) {
                cameraModule = (CameraModule) module;
            }
        });
        Atlas.moduleLoader.getModule("PlatformModule").ifPresent(module->{
            if (module instanceof PlatformModule) {
                platformModule = (PlatformModule) module;
            }
        });
        Atlas.moduleLoader.getModule("FxModule").ifPresent(module->{
            if (module instanceof FxModule) {
                fxModule = (FxModule) module;
            }
        });
        CSM.init(2048);
        CSM.updateCCSM(2048, 0.1f, 30, cameraModule.getIdentityProjection(), cameraModule.getViewMatrix());
        setup();
        // Modify sky-box mvp when camera mvp changes
        cameraModule.getMvpProperty().addListener(((oldValue, newValue)->{
            // Update OpenGL matrix
            try (final MemoryStack stack = MemoryStack.stackPush()) {
                final FloatBuffer dataBuffer = stack.callocFloat(16);
                newValue.get(dataBuffer);
                GL20.glUseProgram(shaderProgramCsm.id);
                GL20.glUniform3f(uniCsmCampos, cameraModule.getCameraPosition().x(), cameraModule.getCameraPosition().y(), cameraModule.getCameraPosition().z());
                GL20.glUniformMatrix4fv(uniCsmMvp, false, dataBuffer);
                GL20.glUniformMatrix4fv(uniCsmInverseViewMat, false, cameraModule.getViewMatrix().invert(calcMatrix).get(dataBuffer));
                cameraModule.getViewMatrix().get(dataBuffer);
                GL20.glUniformMatrix4fv(uniCsmViewMat, false, dataBuffer);
            }
            updateCsm = true;
        }));
        Atlas.ambientProperty.addListener((oldValue, newValue)->{
            GL20.glUseProgram(shaderProgramCsm.id);
            GL20.glUniform1f(uniCsmAmbientAmount, newValue);
        });
        Atlas.useLightingProperty.addListener((oldValue, newValue)->{
            GL20.glUseProgram(shaderProgramCsm.id);
            GL20.glUniform1f(uniCsmEnableLighting, newValue ? 1.0f : 0.0f);
        });
        Atlas.lightMvpProperty.addListener((oldValue, newValue)->updateCsm = true);
        Atlas.lightPositionProperty.addListener((oldValue, newValue)->updateCsm = true);
    }

    /**
     * OpenGL setup code.
     */
    @SuppressWarnings ("Duplicates")
    private void setup() {
        // Create shaders
        try {
            GL20.glUseProgram(0);
            // Light shader setup
            // Load platform shaders
            vertexShaderLight = Shader.loadShader(GL20.GL_VERTEX_SHADER, "../src/main/resources/shaders/shadow/VERTEX.glsl");
            fragmentShaderLight = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "../src/main/resources/shaders/shadow/FRAGMENT.glsl");
            // Verify shader compile status
            if (!vertexShaderLight.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(vertexShaderLight.id, 1024));
            }
            if (!fragmentShaderLight.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(fragmentShaderLight.id, 1024));
            }
            // Create shader program
            shaderProgramLight = new ShaderProgram();
            shaderProgramLight.attachShader(vertexShaderLight);
            shaderProgramLight.attachShader(fragmentShaderLight);
            // Bind shader attributes
            GL20.glBindAttribLocation(shaderProgramLight.id, 0, "position");
            GL20.glBindAttribLocation(shaderProgramLight.id, 1, "texcoord");
            // Link and bind shader program
            shaderProgramLight.link();
            shaderProgramLight.validate();
            // Check status of shader program
            if (!shaderProgramLight.verify()) {
                throw new Exception("shader program error");
            }
            // Set shader uniforms
            GL20.glUseProgram(shaderProgramLight.id);
            // Shader mvp uniform
            uniLightMvp = GL20.glGetUniformLocation(shaderProgramLight.id, "mvp");
            if (uniLightMvp != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniLightMvp, false, Atlas.lightMvpProperty.getProperty().get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [light] unable to find uniform [mvp]");
            }
            // Shader texture map uniform
            int UNI_LIGHT_LIGHT_TEXTURE = GL20.glGetUniformLocation(shaderProgramLight.id, "textureMap");
            if (UNI_LIGHT_LIGHT_TEXTURE != -1) {
                GL20.glUniform1i(UNI_LIGHT_LIGHT_TEXTURE, 0);
            } else {
                System.err.println("Shader program [light] unable to find uniform [textureMap]");
            }
            GL20.glUseProgram(0);
            // CSM shader setup
            // Load platform shaders
            vertexShaderCsm = Shader.loadShader(GL20.GL_VERTEX_SHADER, "../src/main/resources/shaders/ccsm/VERTEX_SHADER_CCSM.glsl");
            fragmentShaderCsm = Shader.loadShader(GL20.GL_FRAGMENT_SHADER, "../src/main/resources/shaders/ccsm/FRAGMENT_SHADER_CCSM.glsl");
            // Verify shader compile status
            if (!vertexShaderCsm.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(vertexShaderCsm.id, 1024));
            }
            if (!fragmentShaderCsm.verify()) {
                throw new Exception("shader load error: " + GL20.glGetShaderInfoLog(fragmentShaderCsm.id, 1024));
            }
            // Create shader program
            shaderProgramCsm = new ShaderProgram();
            shaderProgramCsm.attachShader(vertexShaderCsm);
            shaderProgramCsm.attachShader(fragmentShaderCsm);
            // Bind shader attributes
            GL20.glBindAttribLocation(shaderProgramCsm.id, 0, "position");
            GL20.glBindAttribLocation(shaderProgramCsm.id, 1, "texcoord");
            GL20.glBindAttribLocation(shaderProgramCsm.id, 2, "normal");
            GL20.glBindAttribLocation(shaderProgramCsm.id, 3, "color");
            // Link and bind shader program
            shaderProgramCsm.link();
            shaderProgramCsm.validate();
            // Check status of shader program
            if (!shaderProgramCsm.verify()) {
                throw new Exception("shader program error " + GL20.glGetProgramInfoLog(shaderProgramCsm.id, 1024));
            }
            // Set shader uniforms
            GL20.glUseProgram(shaderProgramCsm.id);
            uniCsmMvp = GL20.glGetUniformLocation(shaderProgramCsm.id, "mvp");
            if (uniCsmMvp != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniCsmMvp, false, cameraModule.getMvpProperty().getProperty().get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [csm] unable to find uniform [mvp]");
            }
            // Shader view matrix uniform
            uniCsmViewMat = GL20.glGetUniformLocation(shaderProgramCsm.id, "viewMatrix");
            if (uniCsmViewMat != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniCsmViewMat, false, cameraModule.getViewMatrix().get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [csm] unable to find uniform [viewMatrix]");
            }
            // Shader light ambient uniform
            uniCsmAmbientAmount = GL20.glGetUniformLocation(shaderProgramCsm.id, "ambientAmount");
            if (uniCsmAmbientAmount != -1) {
                GL20.glUniform1f(uniCsmAmbientAmount, Atlas.ambientProperty.getProperty());
            } else {
                System.err.println("Shader program [csm] unable to find uniform [ambientAmount]");
            }
            // Shader use lighting uniform
            uniCsmEnableLighting = GL20.glGetUniformLocation(shaderProgramCsm.id, "enableLighting");
            if (uniCsmEnableLighting != -1) {
                GL20.glUniform1f(uniCsmEnableLighting, Atlas.useLightingProperty.getProperty() ? 1.0f : 0.0f);
            } else {
                System.err.println("Shader program [csm] unable to find uniform [enableLighting]");
            }
            // Shader light position uniform
            uniCsmSunPos = GL20.glGetUniformLocation(shaderProgramCsm.id, "lightPos");
            if (uniCsmSunPos != -1) {
                GL20.glUniform3f(uniCsmSunPos,
                        Atlas.lightPositionProperty.getProperty().x,
                        Atlas.lightPositionProperty.getProperty().y,
                        Atlas.lightPositionProperty.getProperty().z
                );
            } else {
                System.err.println("Shader program [csm] unable to find uniform [lightPos]");
            }
            // Shader view position uniform
            uniCsmCampos = GL20.glGetUniformLocation(shaderProgramCsm.id, "viewPos");
            if (uniCsmCampos != -1) {
                GL20.glUniform3f(uniCsmCampos, cameraModule.getCameraPosition().x(), cameraModule.getCameraPosition().y(), cameraModule.getCameraPosition().z());
            } else {
                System.err.println("Shader program [csm] unable to find uniform [viewPos]");
            }
            // Shader light color uniform
            uniCsmSunColor = GL20.glGetUniformLocation(shaderProgramCsm.id, "lightColor");
            if (uniCsmSunColor != -1) {
                GL20.glUniform3f(uniCsmSunColor, 1f, 1f, 1f);
            } else {
                System.err.println("Shader program [csm] unable to find uniform [lightColor]");
            }
            // Shader shadow map uniform
            uniCsmShadowMap = GL20.glGetUniformLocation(shaderProgramCsm.id, "shadowMap");
            if (uniCsmShadowMap != -1) {
                for (int i = 0; i < CSM.MAX_SPLITS; i++) {
                    GL20.glUniform1i(GL20.glGetUniformLocation(shaderProgramCsm.id, "shadowMap[" + i + "]"), 2 + i);
                }
            } else {
                System.err.println("Shader program [csm] unable to find uniform [shadowMap]");
            }
            // Shader texture map uniform
            int UNI_CCSM_LIGHT_TEXTURE = GL20.glGetUniformLocation(shaderProgramCsm.id, "textureMap");
            if (UNI_CCSM_LIGHT_TEXTURE != -1) {
                GL20.glUniform1i(UNI_CCSM_LIGHT_TEXTURE, 0);
            } else {
                System.err.println("Shader program [csm] unable to find uniform [textureMap]");
            }
            // Shader cascade splits uniform
            uniCsmCascadeSplits = GL20.glGetUniformLocation(shaderProgramCsm.id, "cascadedSplits");
            if (uniCsmCascadeSplits != -1) {
                GL20.glUniform4f(uniCsmCascadeSplits, CSM.cascadeSplitArray[0], CSM.cascadeSplitArray[1], CSM.cascadeSplitArray[2], CSM.cascadeSplitArray[3]);
            } else {
                System.err.println("Shader program [csm] unable to find uniform [cascadedSplits]");
            }
            // Shader projection matrices uniform
            uniCsmLightVpMat = GL20.glGetUniformLocation(shaderProgramCsm.id, "lightViewProjectionMatrices");
            if (uniCsmLightVpMat != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    for (int i = 0; i < CSM.MAX_SPLITS; i++) {
                        GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderProgramCsm.id, "lightViewProjectionMatrices[" + i + "]"), false, CSM.cascadedMatrices[i].get(stack.callocFloat(16)));
                    }
                }
            } else {
                System.err.println("Shader program [csm] unable to find uniform [lightViewProjectionMatrices]");
            }
            // Shader inverse view matrix uniform
            uniCsmInverseViewMat = GL20.glGetUniformLocation(shaderProgramCsm.id, "inverseViewMatrix");
            if (uniCsmInverseViewMat != -1) {
                try (final MemoryStack stack = MemoryStack.stackPush()) {
                    GL20.glUniformMatrix4fv(uniCsmInverseViewMat, false, cameraModule.getViewMatrix().invert(calcMatrix).get(stack.callocFloat(16)));
                }
            } else {
                System.err.println("Shader program [csm] unable to find uniform [inverseViewMatrix]");
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
        // call on MVP LIGHT_POS
        if (updateCsm) {
            updateCsm = false;
            CSM.updateCCSM(2048, 0.1f, 30, cameraModule.getIdentityProjection(), cameraModule.getViewMatrix());
            // Update OpenGL matrix
            try (final MemoryStack stack = MemoryStack.stackPush()) {
                final FloatBuffer dataBuffer = stack.callocFloat(16);
                Atlas.lightMvpProperty.getProperty().get(dataBuffer);
                GL20.glUseProgram(shaderProgramCsm.id);
                GL20.glUniform3f(uniCsmSunPos, Atlas.lightPositionProperty.getProperty().x, Atlas.lightPositionProperty.getProperty().y, Atlas.lightPositionProperty.getProperty().z);
                for (int i = 0; i < CSM.MAX_SPLITS; i++) {
                    GL20.glUniformMatrix4fv(GL20.glGetUniformLocation(shaderProgramCsm.id, "lightViewProjectionMatrices[" + i + "]"), false, CSM.cascadedMatrices[i].get(dataBuffer));
                }
                GL20.glUniform4f(uniCsmCascadeSplits, CSM.cascadeSplitArray[0], CSM.cascadeSplitArray[1], CSM.cascadeSplitArray[2], CSM.cascadeSplitArray[3]);
                GL20.glUniformMatrix4fv(uniCsmInverseViewMat, false, cameraModule.getViewMatrix().invert(calcMatrix).get(dataBuffer));
            }
        }
    }

    @Override
    public void render(final double displacement) {
        GL20.glUseProgram(shaderProgramCsm.id);
        for (int i = 0; i < CSM.MAX_SPLITS; i++) {
            GL13.glActiveTexture(GL13.GL_TEXTURE2 + i);
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, CSM.shadowTextureArray[i]);
        }
        platformModule.render(displacement);
        fxModule.render(displacement);
        platformModule.renderGrids(displacement);
    }

    /**
     * Renders the shadows from the lights point of view.
     *
     * @param displacement the amount of time difference from the previous to the current tick.
     */
    public void renderShadows(final double displacement) {
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, CSM.mCascadedShadowFBO);
        GL11.glViewport(0, 0, 2048, 2048);
        GL20.glUseProgram(shaderProgramLight.id);
        GL11.glCullFace(GL11.GL_FRONT);
        try (final MemoryStack stack = MemoryStack.stackPush()) {
            final FloatBuffer dataBuffer = stack.callocFloat(16);
            for (int i = 0; i < CSM.MAX_SPLITS; i++) {
                GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL11.GL_TEXTURE_2D, CSM.shadowTextureArray[i], 0);
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                CSM.cascadedMatrices[i].get(dataBuffer);
                GL20.glUniformMatrix4fv(uniLightMvp, false, dataBuffer);
                //This function just renders the depth of the 4 cubes we previously saw
                platformModule.render(displacement);
                fxModule.render(displacement);
            }
        }
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glViewport(0, 0, (int) Atlas.width, (int) Atlas.height);
        GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, 0);
    }
}
