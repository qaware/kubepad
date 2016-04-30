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
import javax.sound.midi.ShortMessage

import static de.qaware.cloud.nativ.k8s.LaunchpadMK2.Button.*
import static de.qaware.cloud.nativ.k8s.LaunchpadMK2.Color.*

/**
 * Simple Button spec to check basic receiver interaction.
 */
class ButtonSpec extends Specification {

    def receiver = Mock(Receiver)

    def "turn Mixer button on red"() {
        ShortMessage message
        int time

        when:
        MIXER.on(receiver, RED)

        then:
        1 * receiver.send(*_) >> { arguments ->
            message = arguments[0] as ShortMessage
            time = arguments[1] as int
        }

        message.command == 176
        message.channel == 0
        message.data1 == 111
        message.data2 == RED.value
        time == -1
    }

    def "blink Session button with purple"() {
        ShortMessage message
        int time

        when:
        SESSION.blink(receiver, PURPLE)

        then:
        1 * receiver.send(*_) >> { arguments ->
            message = arguments[0] as ShortMessage
            time = arguments[1] as int
        }

        message.command == 176
        message.channel == 1
        message.data1 == 108
        message.data2 == PURPLE.value
        time == -1
    }

    def "pulse Volume button with blue"() {
        ShortMessage message
        int time

        when:
        VOLUME.pulse(receiver, BLUE)

        then:
        1 * receiver.send(*_) >> { arguments ->
            message = arguments[0] as ShortMessage
            time = arguments[1] as int
        }

        message.command == 144
        message.channel == 2
        message.data1 == 89
        message.data2 == BLUE.value
        time == -1
    }

    def "turn Record button off"() {
        ShortMessage message
        int time

        when:
        RECORD.off(receiver)

        then:
        1 * receiver.send(*_) >> { arguments ->
            message = arguments[0] as ShortMessage
            time = arguments[1] as int
        }

        message.command == 144
        message.channel == 0
        message.data1 == 19
        message.data2 == 0
        time == -1
    }
}
