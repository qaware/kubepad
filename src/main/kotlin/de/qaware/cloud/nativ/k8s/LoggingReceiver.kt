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

import org.apache.deltaspike.core.api.exclude.Exclude
import org.apache.deltaspike.core.api.projectstage.ProjectStage
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Alternative
import javax.inject.Inject
import javax.sound.midi.MidiMessage
import javax.sound.midi.Receiver

/**
 * An alternative implementation of a MIDI receiver.
 */
@ApplicationScoped
@Alternative
@Exclude(exceptIfProjectStage = arrayOf(ProjectStage.Development::class))
open class LoggingReceiver @Inject constructor(private val logger: Logger) : Receiver {

    override fun send(message: MidiMessage?, timeStamp: Long) {
        logger.debug("Sending {} {}.", message, timeStamp)
    }

    override fun close() {
        logger.debug("Closing receiver.")
    }
}