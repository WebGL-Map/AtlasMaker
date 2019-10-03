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
 * Allows you change the value of a property created in the {@link Listenable} class and only notify the callbacks when
 * you want to.
 *
 * @param <T> A type of object to listen to.
 * @author Tyler Bucher
 */
public class GameProperty<T> extends Property<T> {

    /**
     * Creates a new {@link GameProperty} object.
     *
     * @param property the default starting value.
     */
    public GameProperty(@Nonnull final T property) {
        super(property);
    }

    /**
     * Temporarily set the property member till the {@link #updateProperty()} function is called.
     *
     * @param property the new value to be set.
     */
    @Override
    public void setProperty(@Nonnull final T property) {
        this.property = property;
    }

    /**
     * Should be called in the update loop of a game or similar application. The property member is only update when
     * this function is called and it does not equal the previous value.
     */
    public void updateProperty() {
        if (!property.equals(oldProperty)) {
            this.changeListenerList.forEach(i->i.changed(this.oldProperty, this.property));
            this.oldProperty = this.property;
        }
    }
}
