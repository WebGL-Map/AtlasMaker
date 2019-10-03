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
package net.reallifegames.atlas.listenable;

import javax.annotation.Nonnull;

/**
 * Allows you change the value of a property created in the {@link Listenable} class.
 *
 * @param <T> A type of object to listen to.
 * @author Tyler Bucher
 */
public class Property<T> extends Listenable<T> {

    /**
     * Creates a new {@link Property} object.
     *
     * @param property the default starting value.
     */
    public Property(@Nonnull final T property) {
        super(property);
    }

    /**
     * @return the current value of the property.
     */
    public T getProperty() {
        return property;
    }

    /**
     * Set the new value for the property member and notify all callbacks.
     *
     * @param property the new value to be set.
     */
    public void setProperty(@Nonnull final T property) {
        this.oldProperty = this.property;
        this.property = property;
        this.changeListenerList.forEach(i->i.changed(this.oldProperty, this.property));
    }
}
