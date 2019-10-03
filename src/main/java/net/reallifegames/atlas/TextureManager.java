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

import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Helps keep textures organized.
 *
 * @author Tyler Bucher
 */
public class TextureManager {

    /**
     * The map of textures for fetching.
     */
    private static final Map<String, Integer> textureMap = new HashMap<>();

    /**
     * Adds a texture to the texture map.
     *
     * @param id       the name or id of the texture.
     * @param path     the path of the texture file.
     * @param consumer the function to call to make the texture.
     * @return true if the texture was added false otherwise.
     */
    public static boolean registerTexture(@Nonnull final String id,
                                          @Nonnull final String path,
                                          @Nonnull final BiConsumer<Integer, String> consumer) {
        final int textureId = GL11.glGenTextures();
        consumer.accept(textureId, path);
        textureMap.put(id, textureId);
        return true;
    }

    /**
     * Returns a texture handler if form a name.
     *
     * @param id the name or id of the texture.
     * @return the id or -1 if not found.
     */
    public static int getTexture(@Nonnull final String id) {
        return textureMap.getOrDefault(id, -1);
    }
}
