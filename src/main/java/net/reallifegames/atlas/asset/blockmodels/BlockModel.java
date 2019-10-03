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
package net.reallifegames.atlas.asset.blockmodels;

import net.reallifegames.atlas.asset.blockstates.BlockState;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

/**
 * Holds the model files for all the specified variants.
 *
 * @author Tyler Bucher
 */
public class BlockModel {

    /**
     * Loads a different model from the given path. If both "parent" and "elements" are set, the "elements" tag
     * overrides the "elements" tag from the previous model.
     */
    public final BlockModel parentModel;

    /**
     * Whether to use ambient occlusion.
     */
    public final boolean ambientOcclusion;

    /**
     * Holds the textures of the model.
     */
    public final Map<String, String> textures;

    /**
     * Contains all the elements of the model. They can only have cubic forms.
     */
    public final List<Element> elements;

    /**
     * Creates a new {@link BlockModel} for a {@link BlockState}.
     *
     * @param parentModel      the parent model of this model.
     * @param ambientOcclusion whether to use ambient occlusion.
     * @param texture          holds the textures of the model.
     * @param elements         contains all the elements of the model.
     */
    public BlockModel(@Nonnull final BlockModel parentModel,
                      final boolean ambientOcclusion,
                      @Nonnull final Map<String, String> texture,
                      @Nonnull final List<Element> elements) {
        this.parentModel = parentModel;
        this.ambientOcclusion = ambientOcclusion;
        this.textures = texture;
        this.elements = elements;
    }
}
