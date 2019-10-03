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
package net.reallifegames.atlas.asset.blockstates;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Name of a variant, which consists of the relevant block states separated by commas. A block with just one variant
 * uses "" as a name for its variant. Each variant can have one model or an array of models and contains their
 * properties. If set to an array, the model will randomly be chosen from the options given, with each option being
 * specified in separate subsidiary -tags. Item frames are treated as blocks and will use "map=false" for a map-less
 * item frame, and "map=true" for item frames with maps.
 *
 * @author Tyler Bucher
 */
public class Variant {

    /**
     * Name of a variant, which consists of the relevant block states separated by commas.
     */
    public final String name;

    /**
     * Contains the properties of a model, if more than one model is used for the same variant. All specified models
     * alternate in the game.
     */
    public final List<Model> modelList;

    /**
     * Creates a new block variant.
     *
     * @param name      the name of this variant.
     * @param modelList the list of models for this variant.
     */
    public Variant(@Nonnull final String name, @Nonnull final List<Model> modelList) {
        this.name = name;
        this.modelList = modelList;
    }
}
