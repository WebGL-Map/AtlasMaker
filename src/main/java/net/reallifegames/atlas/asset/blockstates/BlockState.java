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
import javax.annotation.Nullable;
import java.util.List;

/**
 * BlockStates list all their existing variants and links them to their corresponding model(s).
 *
 * @author Tyler Bucher
 */
public class BlockState {

    /**
     * Holds the names of all the variants of the block.
     */
    public final List<Variant> blockVariants;

    /**
     * Used instead of variants to combine models based on block state attributes.
     */
    public final List<Multipart> multiparts;

    /**
     * States if this {@link BlockState} should be built by the multiparts or blockVariants.
     */
    public final boolean useMultipart;

    /**
     * Creates a new {@link BlockState} type.
     *
     * @param blockVariants variants used to build this BlockState.
     * @param multiparts    multiparts used to build this BlockState.
     */
    public BlockState(@Nonnull final List<Variant> blockVariants, @Nullable final List<Multipart> multiparts) {
        this.blockVariants = blockVariants;
        this.multiparts = multiparts;
        this.useMultipart = multiparts != null;
    }
}
