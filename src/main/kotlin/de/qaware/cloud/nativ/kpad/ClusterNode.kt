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
package de.qaware.cloud.nativ.kpad

import java.util.concurrent.atomic.AtomicBoolean

/**
 * The data class to represent one node instance on the grid.
 */
data class ClusterNode(val row: Int, val column: Int,
                       var phase: Phase = ClusterNode.Phase.Unknown,
                       val active: AtomicBoolean = AtomicBoolean(false)) {

    fun activate(): ClusterNode {
        active.compareAndSet(false, true)
        phase = Phase.Running
        return this
    }

    fun update(p: Phase) {
        if (active.get()) {
            phase = p
        }
    }

    fun deactivate(): ClusterNode {
        active.compareAndSet(true, false)
        phase = Phase.Terminated
        return this
    }

    enum class Phase {
        Pending, Running, Terminated, Succeeded, Failed, Unknown
    }

}