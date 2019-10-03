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
import java.util.Map;

/**
 * Used instead of variants to combine models based on block state attributes.
 *
 * @author Tyler Bucher
 */
public class Multipart {

    /**
     * Contains the properties of a model, if more than one model is used for the same variant. All specified models
     * alternate in the game.
     */
    public final List<Model> modelList;

    /**
     * A list of cases that all have to match the block to return true.
     */
    public final List<List<Map.Entry<String, String>>> stateList;

    /**
     * Matches if any of the contained cases return true. Cannot be set along side other cases.
     */
    public final boolean conditionalOr;

    /**
     * Used to make a block state.
     *
     * @param modelList     the list of models for this variant.
     * @param stateList     a list of cases that all have to match the block to return true.
     * @param conditionalOr states if this multipart uses the conditional.
     */
    public Multipart(@Nonnull final List<Model> modelList,
                     @Nonnull final List<List<Map.Entry<String, String>>> stateList,
                     @Nonnull final boolean conditionalOr) {
        this.modelList = modelList;
        this.stateList = stateList;
        this.conditionalOr = conditionalOr;
    }
}
