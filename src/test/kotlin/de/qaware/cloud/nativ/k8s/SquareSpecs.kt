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
package de.qaware.cloud.nativ.k8s

import org.jetbrains.spek.api.Spek
import kotlin.test.assertEquals
import de.qaware.cloud.nativ.k8s.launchpad.LaunchpadMK2

/**
 * A Kotlin Spek test for the Square button data class.
 */
class SquareSpecs : Spek({
    given("a square button ID") {
        on("getting the bottom left by ID 11") {
            val bottomLeft = LaunchpadMK2.Square.from(11)
            it("should be bottom left") {
                assertEquals(LaunchpadMK2.Square(0, 0), bottomLeft)
            }
        }
        on("getting the left on the first row by 21") {
            val square = LaunchpadMK2.Square.from(21)
            it ("should be the correct button") {
                assertEquals(LaunchpadMK2.Square(1, 0), square)
            }
        }
    }
})