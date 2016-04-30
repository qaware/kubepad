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

import spock.lang.Specification

import javax.sound.midi.Receiver
import javax.sound.midi.Transmitter

/**
 * Basic specification for our MidiSystemProducer.
 */
class MidiSystemProducerSpec extends Specification {

    def producer = new MidiSystemProducer()

    def "close() called on Receiver disposal"() {
        given: "a mock receiver"
        def receiver = Mock(Receiver)

        when: "we dispose the receiver"
        producer.close(receiver)

        then: "the close method should be called"
        1 * receiver.close()
    }

    def "close() called on Transmitter disposal"() {
        given: "a mock transmitter"
        def transmitter = Mock(Transmitter)

        when: "we dispose the transmitter"
        producer.close(transmitter)

        then: "the close method should be called"
        1 * transmitter.close()
    }
}
