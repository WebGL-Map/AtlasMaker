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

import org.ajgl.Window;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

/**
 * The windows implementation for the Atlas application.
 *
 * @author Tyler Bucher
 */
public class MainWindow extends Window {

    /**
     * Main window constructor.
     *
     * @param width   display width.
     * @param height  display height.
     * @param title   display title.
     * @param monitor monitor to use.
     * @param share   window handler to share OpenGL context with.
     */
    public MainWindow(final int width, final int height, @Nonnull final String title, final long monitor, final long share) {
        super(width, height, title, monitor, share);
    }

    @Override
    public void preWindowCreation() {
        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GL11.GL_FALSE);      // Keep the window hidden
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);    // Do not allow resizing
    }

    @Override
    public void postWindowCreation() {
        // Get the resolution of the primary monitor
        GLFWVidMode videoMode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());
        // Center our window
        GLFW.glfwSetWindowPos(
                this.getWindowHandler(),
                (videoMode.width() - (this.getWidth() + (int) Atlas.fxControllerWidth)) / 2,
                (videoMode.height() - this.getHeight()) / 2
        );
    }
}
