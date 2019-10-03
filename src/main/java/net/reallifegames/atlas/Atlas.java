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
package net.reallifegames.atlas;

import net.reallifegames.atlas.listenable.GameProperty;
import net.reallifegames.atlas.listenable.properties.Matrix4fProperty;
import net.reallifegames.atlas.listenable.properties.Vector3fProperty;
import net.reallifegames.atlas.module.ModuleLoader;
import net.reallifegames.atlas.module.atlas.AtlasModule;
import net.reallifegames.atlas.module.atlas.ZipManager;
import net.reallifegames.atlas.module.csm.CSMModule;
import net.reallifegames.atlas.module.fx.FxModule;
import net.reallifegames.atlas.module.platform.PlatformModule;
import net.reallifegames.atlas.module.postprocessing.PostProcessingModule;
import net.reallifegames.atlas.module.skybox.SkyBoxModule;
import net.reallifegames.atlas.modules.CameraModule;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL32;

import javax.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

/**
 * Extracts texture files from a Minecrfat.jar file or a texture pack and compiles them into an atlas.
 *
 * @author Tyler Bucher
 */
public class Atlas {

    /**
     * Temp folder name.
     */
    public static final String TEMP_FOLDER_NAME = "{AM}3a23972a-7ce7-4eed-8358-20e53c1604e6";

    /**
     * System temp dir.
     */
    public static final File TEMP_DIR = new File(System.getProperty("java.io.tmpdir"));

    /**
     * Full path to the temp folder.
     */
    public static final String FULL_TEMP_FOLDER_DIR = new File(TEMP_DIR, TEMP_FOLDER_NAME).toString();

    /**
     * A temporary calculation matrix.
     */
    private static final Matrix4f calcMatrix = new Matrix4f();

    /**
     * How much ambient light to use during rendering.
     */
    public static volatile GameProperty<Float> ambientProperty = new GameProperty<>(0.15f);

    /**
     * States if the lighting should be updated.
     */
    public static volatile boolean updateLighting = false;

    /**
     * States if lighting should be used.
     */
    public static final GameProperty<Boolean> useLightingProperty = new GameProperty<>(Boolean.TRUE);

    /**
     * Up-datable light arc position.
     */
    public static volatile Vector3fProperty lightArcProperty = new Vector3fProperty(new Vector3f());

    /**
     * Light orthographic projection.
     */
    private static Matrix4f identityOrthoLight = new Matrix4f().setOrtho(-10, 10, -10, 10, 0.1f, 30f);

    /**
     * Light arc position.
     */
    private static Matrix4f lightArcMatrix = new Matrix4f().arcball(lightArcProperty.getProperty().z, 0, 0, 0, lightArcProperty.getProperty().x, lightArcProperty.getProperty().y);

    /**
     * Light position property.
     */
    public static final Vector3fProperty lightPositionProperty = new Vector3fProperty(new Vector3f());

    /**
     * Light mvp property.
     */
    public static final Matrix4fProperty lightMvpProperty = new Matrix4fProperty(new Matrix4f(identityOrthoLight).mul(lightArcMatrix));

    /**
     * Primary texture directory.
     */
    public static final String TEXTURE_DIR = "../src/main/resources/textures/";

    /**
     * The window preferred width.
     */
    public static final float width = 1000;

    /**
     * The window preferred height.
     */
    public static final float height = 600;

    /**
     * The FxController width.
     */
    static final float fxControllerWidth = 480;

    /**
     * The FxController height.
     */
    static final float fxControllerHeight = 600;

    /**
     * The screen width.
     */
    public static int videoModeWidth;

    /**
     * The screen height.
     */
    public static int videoModeHeight;

    /**
     * The texture id of a blank texture.
     */
    public static int nullTextureId;

    /**
     * Primary window for thi s application.
     */
    public static MainWindow windowTest;
    /**
     * GLFW error callback.
     */
    private static GLFWErrorCallback errorCallback;

    /**
     * Mouse X position.
     */
    private static double[] mouseX = new double[1];

    /**
     * Mouse Y position.
     */
    private static double[] mouseY = new double[1];

    /**
     * The current time in seconds.
     */
    private static double currTime = 0;

    /**
     * States if the window should close.
     */
    public static volatile boolean closed = false;

    /**
     * Loads and helps manage modules.
     */
    public static ModuleLoader moduleLoader;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static CameraModule cameraModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static SkyBoxModule skyboxModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static PlatformModule platformModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static PostProcessingModule postProcessingModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static FxModule fxModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static AtlasModule atlasModule;

    /**
     * A module for updating / inputting/ or rendering.
     */
    private static CSMModule csmModule;

    /**
     * Pre OpenGL-initialization.
     */
    private static void preInitGL(@Nonnull final String[] args) {
        lightArcProperty.addListener((oldValue, newValue)->{
            lightArcMatrix.identity().arcball(newValue.z, 0, 0, 0, newValue.x, newValue.y);
            calcMatrix.set(lightArcMatrix).invert();
            lightPositionProperty.setProperty(calcMatrix.m30(), calcMatrix.m31(), calcMatrix.m32());
            lightPositionProperty.updateProperty();
            lightMvpProperty.setProperty(calcMatrix.set(identityOrthoLight).mul(lightArcMatrix));
        });
        lightArcProperty.setProperty(new Vector3f(2.0344f, 6.0213f, 18.00f));
        lightArcProperty.updateProperty();
        lightMvpProperty.updateProperty();

        final String[] nullArray = new String[]{""};
        moduleLoader = new ModuleLoader();
        if (Boolean.parseBoolean(args[0])) {
            moduleLoader.registerModule(SkyBoxModule.class, nullArray);
            moduleLoader.registerModule(PlatformModule.class, nullArray);
            moduleLoader.registerModule(CSMModule.class, nullArray);
            moduleLoader.registerModule(PostProcessingModule.class, nullArray);
        }
        moduleLoader.registerModule(CameraModule.class, nullArray);
        moduleLoader.registerModule(FxModule.class, new String[]{args[0]});
        moduleLoader.registerModule(AtlasModule.class, new String[]{args[1], args.length == 4 ? args[2] : "", args[0]});

        moduleLoader.prioritizeModules();
    }

    /**
     * OpenGL initialization.
     */
    private static void initGL() {
        // Bind context
        GLFW.glfwMakeContextCurrent(windowTest.getWindowHandler());  // Make the OpenGL context current
        GLFW.glfwShowWindow(windowTest.getWindowHandler());          // Make the window visible
        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        videoModeWidth = vidmode.width();
        videoModeHeight = vidmode.height();
        GL.createCapabilities();
        // Initialize openGl
        GL11.glEnable(GL32.GL_TEXTURE_CUBE_MAP_SEAMLESS);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthFunc(GL11.GL_LEQUAL);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        // AKA vsync
        GLFW.glfwSwapInterval(0);
    }

    /**
     * Program initialization.
     */
    private static void init(final boolean useOpenGL) {
        moduleLoader.instantiateModules();
        cameraModule = (CameraModule) moduleLoader.getModule("CameraModule").orElse(null);
        skyboxModule = (SkyBoxModule) moduleLoader.getModule("SkyBoxModule").orElse(null);
        platformModule = (PlatformModule) moduleLoader.getModule("PlatformModule").orElse(null);
        postProcessingModule = (PostProcessingModule) moduleLoader.getModule("PostProcessingModule").orElse(null);
        fxModule = (FxModule) moduleLoader.getModule("FxModule").orElse(null);
        atlasModule = (AtlasModule) moduleLoader.getModule("AtlasModule").orElse(null);
        csmModule = (CSMModule) moduleLoader.getModule("CSMModule").orElse(null);
        if (useOpenGL) {
            registerTextures();
        }
        Atlas.nullTextureId = TextureManager.getTexture("white");
    }

    /**
     * Registers textures for OpenGL.
     */
    private static void registerTextures() {
        TextureManager.registerTexture("white", Atlas.TEXTURE_DIR + "white.png", (integer, imgPath)->{
            try {
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, integer);
                GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGB, 1, 1, 0,
                        GL11.GL_RGB, GL11.GL_UNSIGNED_BYTE, TextureLoader.loadImage(imgPath, 3));
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Starts the game process.
     */
    private static void gameStart() {
        //GL11.glClearColor(0f, 0f, 0f, 1f);
        GL11.glClearColor(0, 0, 0, 1);
        double frameStart;
        double displacement;
        currTime = GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(windowTest.getWindowHandler())) {
            frameStart = GLFW.glfwGetTime();
            displacement = frameStart - currTime;
            currTime = frameStart;
            // Run Cycles
            input(displacement);
            update(displacement);
            render(displacement);

            GLFW.glfwSwapBuffers(windowTest.getWindowHandler());
        }
        // Release window and window call backs
        exit();
    }

    /**
     * Input method.
     */
    private static void input(final double displacement) {
        GLFW.glfwPollEvents();
        GLFW.glfwGetCursorPos(windowTest.getWindowHandler(), mouseX, mouseY);
        cameraModule.input(displacement);
        skyboxModule.input(displacement);
        platformModule.input(displacement);
    }

    /**
     * Update method for moving objects.
     */
    private static void update(final double displacement) {
        if (updateLighting) {
            updateLighting = false;
            ambientProperty.updateProperty();
            lightArcProperty.updateProperty();
            lightMvpProperty.updateProperty();
        }
        useLightingProperty.updateProperty();

        cameraModule.update(displacement);
        skyboxModule.update(displacement);
        platformModule.update(displacement);
        csmModule.update(displacement);
    }

    /**
     * Render method.
     */
    private static void render(final double displacement) {
        postProcessingModule.render(displacement);
    }

    /**
     * @return The current frames per second in seconds, since the last call.
     */
    private static int getFPS(final double time) {
        return (int) (1.0 / time);
    }

    /**
     * Exits the program.
     */
    private static void exit() {
        javafx.application.Platform.exit();
        // Close all callbacks and free the window.
        windowTest.finalize();
        // Close the error callback.
        errorCallback.free();
        // Terminate glfw.
        GLFW.glfwTerminate();
        // Set closed for fx just in-case
        closed = true;
    }

    /**
     * @param args program arguments.
     * @throws InterruptedException if the fx thread was unable to be joined.
     */
    public static void main(final String[] args) throws InterruptedException, IOException {
        if (Boolean.parseBoolean(args[0])) {
            GLFW.glfwSetErrorCallback(errorCallback = GLFWErrorCallback.createPrint(System.err));
            // Construct main window
            windowTest = new MainWindow((int) width, (int) height, "GLM Atlas Maker", 0, 0);
            // Initialize main window.
            if (!windowTest.setup()) {
                errorCallback.free();
                return;
            }
        }
        Atlas.preInitGL(args);
        if (Boolean.parseBoolean(args[0])) {
            Atlas.initGL();
        }
        Atlas.init(Boolean.parseBoolean(args[0]));

        if (Boolean.parseBoolean(args[0])) {
            fxModule.getFxApplicationThread().start();
            Atlas.gameStart();
            fxModule.getFxApplicationThread().join();
        }
        System.out.println("Compressing exported WebGL-Map data.");
        ZipManager.compressFiles(Arrays.asList(Objects.requireNonNull(new File(FULL_TEMP_FOLDER_DIR + "/export").listFiles())), "dataPack.zip");
        System.out.println("Cleaning up temporary files.");
        ZipManager.delete(new File(FULL_TEMP_FOLDER_DIR));
    }
}


