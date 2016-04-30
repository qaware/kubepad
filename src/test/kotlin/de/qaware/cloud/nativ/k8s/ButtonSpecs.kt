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
package de.qaware.cloud.nativ.k8s.control

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals

/**
 * A Kotlin Spek test for the round Button enum.
 */
class ButtonSpecs : Spek({
    given("a round button command and ID") {
        on("finding the Mixer button") {
            val mixer = LaunchpadMK2.Button.find(176, 111)
            it("the Mixer should be found") {
                assertEquals(LaunchpadMK2.Button.MIXER, mixer)
            }
        }
    }
    given ("a top round button index") {
        on("getting the top Session button by index") {
            val button = LaunchpadMK2.Button.top(4)
            it("should get the Session button") {
                assertEquals(LaunchpadMK2.Button.SESSION, button)
            }
        }
        on("getting the top MIXER button by index") {
            val button = LaunchpadMK2.Button.top(7)
            it("should get the Session button") {
                assertEquals(LaunchpadMK2.Button.MIXER, button)
            }
        }
    }
    given ("a right round button index") {
        on("getting the Volume button by index") {
            val button = LaunchpadMK2.Button.right(7)
            it("should get the Volume button") {
                assertEquals(LaunchpadMK2.Button.VOLUME, button)
            }
        }
        on("getting the Record button by index") {
            val button = LaunchpadMK2.Button.right(0)
            it("should get the Record button") {
                assertEquals(LaunchpadMK2.Button.RECORD, button)
            }
        }
    }
})
