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
package net.reallifegames.atlas.listenable.properties;

import net.reallifegames.atlas.listenable.GameProperty;
import net.reallifegames.atlas.listenable.Listenable;
import org.joml.Vector3f;

import javax.annotation.Nonnull;

/**
 * Allows you change the value of a {@link Vector3f} created in the {@link Listenable} class and only notify the
 * callbacks when you want to.
 *
 * @author Tyler Bucher
 */
public class Vector3fProperty extends GameProperty<Vector3f> {

    /**
     * Creates a new {@link Vector3fProperty} object.
     *
     * @param property the default starting value.
     */
    public Vector3fProperty(@Nonnull final Vector3f property) {
        super(property);
    }

    @Override
    public void setProperty(@Nonnull final Vector3f property) {
        this.property.set(property);
    }

    /**
     * Set the underlying vector.
     *
     * @param x vector x component.
     * @param y vector y component.
     * @param z vector z component.
     */
    public void setProperty(final float x, final float y, final float z) {
        this.property.set(x, y, z);
    }

    @Override
    public void updateProperty() {
        this.changeListenerList.forEach(i->i.changed(this.oldProperty, this.property));
        this.oldProperty.set(this.property);
    }
}
