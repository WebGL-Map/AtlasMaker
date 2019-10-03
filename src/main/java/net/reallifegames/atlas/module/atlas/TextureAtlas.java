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
package net.reallifegames.atlas.module.atlas;

import org.joml.Vector4f;

import javax.annotation.Nonnull;
import java.awt.image.BufferedImage;
import java.util.HashMap;

/**
 * Creates a new texture atlas with mapped coordinates.
 *
 * @author Tyler Bucher
 */
public class TextureAtlas {

    /**
     * The atlas as a image
     */
    private final BufferedImage atlas;

    /**
     * The size of the texture atlas.
     */
    private final int length;

    /**
     * The uv coordinate map.
     */
    private final HashMap<String, Vector4f> uvMap;

    /**
     * @param atlas  atlas as a image
     * @param length size of the texture atlas.
     */
    public TextureAtlas(@Nonnull final BufferedImage atlas, final int length) {
        this.atlas = atlas;
        this.length = length;
        uvMap = new HashMap<>();
    }

    /**
     * @return the atlas as a image.
     */
    public BufferedImage getAtlas() {
        return atlas;
    }

    /**
     * @return the size of the texture atlas.
     */
    public int getLength() {
        return length;
    }

    /**
     * @return the uv coordinate map.
     */
    public HashMap<String, Vector4f> getUvMap() {
        return uvMap;
    }
}
