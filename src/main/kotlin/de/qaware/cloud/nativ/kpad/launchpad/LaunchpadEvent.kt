/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 QAware GmbH, Munich, Germany
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
package de.qaware.cloud.nativ.kpad.launchpad

import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.Color
import javax.inject.Qualifier

/**
 * The data class for any trigger event we send to the Launchpad.
 */
data class LaunchpadEvent constructor(val switch: Switch?,
                                      val switchable: LaunchpadMK2.Switchable?,
                                      val text: String?,
                                      val color: Color) {

    constructor(switch: Switch, switchable: LaunchpadMK2.Switchable?, color: Color = Color.NONE) : this(switch, switchable, null, color)

    constructor(text: String, color: Color) : this(null, null, text, color)

    /**
     * Switch ON or OFF.
     */
    enum class Switch {
        ON, OFF
    }

    @Qualifier
    @Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Light

    @Qualifier
    @Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Text

    @Qualifier
    @Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Blink

    @Qualifier
    @Target(AnnotationTarget.TYPE, AnnotationTarget.FUNCTION, AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD)
    @Retention(AnnotationRetention.RUNTIME)
    annotation class Pulse

    companion object {
        /**
         * Factory method for light events.
         */
        fun light(switch: Switch, switchable: LaunchpadMK2.Switchable?, color: Color = Color.NONE) = LaunchpadEvent(switch, switchable, color)

        /**
         * Factory method for text events.
         */
        fun text(message: String, color: Color) = LaunchpadEvent(message, color)
    }
}