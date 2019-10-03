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

import net.reallifegames.atlas.asset.blockmodels.BlockModel;

import javax.annotation.Nonnull;

/**
 * Contains the properties of a model, if more than one model is used for the same variant. All specified models
 * alternate in the game.
 *
 * @author Tyler Bucher
 */
public class Model {

    /**
     * The string name of the model in json.
     */
    public final String modelName;

    /**
     * Specifies the path to the model file of the block, starting in {@code assets/<namespace>/models}.
     */
    public final BlockModel blockModel;

    /**
     * Rotation of the model on the x-axis in increments of 90 degrees.
     */
    public final int xRotation;

    /**
     * Rotation of the model on the y-axis in increments of 90 degrees.
     */
    public final int yRotation;

    /**
     * Can be true or false (default). Locks the rotation of the texture of a block, if set to true. This way the
     * texture will not rotate with the block when using the x and y-tags above.
     */
    public final boolean uvLock;

    /**
     * Sets the probability of the model for being used in the game, defaults to 1 (=100%). If more than one model is
     * used for the same variant, the probability will be calculated by dividing the individual model’s weight by the
     * sum of the weights of all models. (For example, if three models are used with weights 1, 1, and 2, then their
     * combined weight would be 4 (1+1+2). The probability of each model being used would then be determined by dividing
     * each weight by 4: 1/4, 1/4 and 2/4, or 25%, 25% and 50%, respectively.)
     */
    public final int weight;

    /**
     * Creates a new {@link Model} for the {@link Variant}.
     *
     * @param modelName  the string name of the model in json.
     * @param blockModel the {@link BlockModel} for this model variant.
     * @param xRotation  x-axis rotation of this model.
     * @param yRotation  y-axis rotation of this model.
     * @param uvLock     locks the rotation of the texture of a block.
     * @param weight     sets the probability of the model for being used in the game.
     */
    public Model(@Nonnull final String modelName,
                 @Nonnull final BlockModel blockModel,
                 final int xRotation,
                 final int yRotation,
                 final boolean uvLock,
                 final int weight) {
        this.modelName = modelName;
        this.blockModel = blockModel;
        this.xRotation = xRotation;
        this.yRotation = yRotation;
        this.uvLock = uvLock;
        this.weight = weight;
    }
}
