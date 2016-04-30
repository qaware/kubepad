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

import de.qaware.cloud.nativ.k8s.LaunchpadEvent.Switch.ON
import org.slf4j.Logger
import java.nio.charset.Charset
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.enterprise.util.AnnotationLiteral
import javax.inject.Inject
import javax.sound.midi.*

/**
 * This class models the main interaction with the Novation Launchpad MK2 midi device.
 */
@ApplicationScoped
open class LaunchpadMK2 @Inject constructor(private val transmitter: Transmitter,
                                            private val receiver: Receiver,
                                            private val event: Event<SwitchableEvent>,
                                            private val logger: Logger) {

    /**
     * Register with the Launchpad to receive MIDI messages.
     */
    @PostConstruct
    open fun postConstruct() {
        transmitter.receiver = object : Receiver {
            override fun send(message: MidiMessage, timeStamp: Long) {
                if (message is ShortMessage) handle(message)
            }

            override fun close() {
            }
        }
    }

    private fun handle(message: ShortMessage) {
        logger.debug("Handling {}[{} {} {} {}]", message, message.command, message.channel, message.data1, message.data2)

        // determine the actual Switchable based in the ID in data1
        val switchable = when (message.data1) {
            in 104..111 -> Button.find(message.command, message.data1)
            in 19..89 step 10 -> Button.find(message.command, message.data1)
            else -> Square.from(message.data1)
        }

        when (message.data2) {
            127 -> event.select(object : AnnotationLiteral<SwitchableEvent.Pressed>() {}).fire(SwitchableEvent(switchable))
            else -> event.select(object : AnnotationLiteral<SwitchableEvent.Released>() {}).fire(SwitchableEvent(switchable))
        }
    }

    /**
     * Switch the light on or off for a given event.
     */
    open fun light(@Observes @LaunchpadEvent.Light event: LaunchpadEvent) {
        when (event.switch) {
            ON -> event.switchable?.on(receiver, event.color)
            else -> event.switchable?.off(receiver)
        }
    }

    /**
     * Pulse the switchable for a given event.
     */
    open fun pulse(@Observes @LaunchpadEvent.Pulse event: LaunchpadEvent) {
        event.switchable?.pulse(receiver, event.color)
    }

    /**
     * Blink the switchable for a given event.
     */
    open fun blink(@Observes @LaunchpadEvent.Blink event: LaunchpadEvent) {
        event.switchable?.blink(receiver, event.color)
    }

    /**
     * Write the given text event as a SysexMessage to the LaunchpadMK2. Due to a bug in the
     * MacOSX JVM, this function will only work on Windows for now.
     */
    open fun text(@Observes @LaunchpadEvent.Text event: LaunchpadEvent) {
        val preamble = byteArrayOf(240.toByte(), 0, 32, 41, 2, 24, 20)
        val text = event.text?.toByteArray(Charset.defaultCharset())

        val data = ArrayList<Byte>()
        data.addAll(preamble.toList())
        data.add(event.color.value.toByte())
        data.add(0) // do not loop
        data.addAll(text!!.toList())
        data.add(247.toByte())

        receiver.send(SysexMessage(data.toByteArray(), data.size), -1)
    }

    /**
     * Reset the Launchpad and turn off all the buttons.
     */
    @PreDestroy
    open fun reset() {
        Button.values().forEach { it.off(receiver) }

        IntRange(0, 7).forEach { r ->
            IntRange(0, 7).forEach { c ->
                Square(r, c).off(receiver)
            }
        }
    }

    /**
     * The common interface for Switchable elements of the Launchpad.
     */
    interface Switchable {
        val command: Int
        val id: Int
        val row: Int

        /**
         * Turn button on by setting the color to specified value.
         *
         * @param receiver the MIDI receiver
         * @param color the color value
         */
        fun on(receiver: Receiver, color: Color) = receiver.send(ShortMessage(command, 0, id, color.value), -1)

        /**
         * Start blinking between the current color and the given color.
         *
         * @param receiver the MIDI receiver
         * @param color the color value
         */
        fun blink(receiver: Receiver, color: Color) = receiver.send(ShortMessage(command + 1, 1, id, color.value), -1)

        /**
         * Start pulsing at the given color.
         *
         * @param receiver the MIDI receiver
         * @param color the color value
         */
        fun pulse(receiver: Receiver, color: Color) = receiver.send(ShortMessage(command + 2, 2, id, color.value), -1)

        /**
         * Turn this color off, by setting color to zero.
         *
         * @param receiver the MIDI receiver
         */
        fun off(receiver: Receiver) = receiver.send(ShortMessage(command, 0, id, 0), -1)
    }

    /**
     * The buttons on our Launchpad MK2. Read the programmers reference for more details.
     *
     * Commands:
     * - 176 are the top buttons,
     * - 144 are the other buttons
     *
     * Id:
     * - top buttons start from left with 104 to 111
     * - other buttons start at the lower left corner with 11 up to 89
     * - move one to the right with +1
     * - move one row up with +10
     *
     */
    enum class Button(override val command: Int, override val id: Int, override val row: Int) : Switchable {
        CURSOR_UP(176, 104, 8), CURSOR_DOWN(176, 105, 8),
        CURSOR_LEFT(176, 106, 8), CURSOR_RIGHT(176, 107, 8),
        SESSION(176, 108, 8), USER_1(176, 109, 8),
        USER_2(176, 110, 8), MIXER(176, 111, 8),
        RECORD(144, 19, 0), SOLO(144, 29, 1),
        MUTE(144, 39, 2), STOP(144, 49, 3),
        SEND_B(144, 59, 4), SEND_A(144, 69, 5),
        PAN(144, 79, 6), VOLUME(144, 89, 7);

        companion object {
            /**
             * Finds a Button by a given command and ID.
             *
             * @param command the command
             * @param id the ID
             * @return the button if found
             */
            fun find(command: Int, id: Int) = values().find { it.command == command && it.id == id }

            /**
             * Get one of the top buttons by index.
             *
             * @param index the index between 0..7
             * @return a button
             */
            fun top(index: Int) = values()[index]

            /**
             * Get one of the right buttons by index.
             *
             * @param index the index between 0..7
             * @return a button
             */
            fun right(index: Int) = values()[index + 8]
        }
    }

    /**
     * The data class to represent the square buttons on the Launchpad.
     * A square button is located by its position in the 64x64 grid identified by row and column.
     * The origin of the grid is in the lower left corner.
     *
     * The command value for square buttons is always 144.
     *
     * The ID can be calculated, the lower left corner starts at 11 and advances to the right.
     * Every row adds 10 to the ID value.
     */
    data class Square(override val row: Int, val column: Int) : Switchable {
        override val command = 144
        override val id = 11 + column + 10 * row

        companion object {
            /**
             * Create a Square from the ID.
             */
            fun from(id: Int) = when (id) {
                in 11..18 -> Square(0, id - 11)
                in 21..28 -> Square(1, id - 21)
                in 31..38 -> Square(2, id - 31)
                in 41..48 -> Square(3, id - 41)
                in 51..58 -> Square(4, id - 51)
                in 61..68 -> Square(5, id - 61)
                in 71..78 -> Square(6, id - 71)
                in 81..88 -> Square(7, id - 81)
                else -> throw UnsupportedOperationException("$id is not a Square.")
            }
        }
    }

    /**
     * Enum class for commonly used colors.
     */
    enum class Color(val value: Int) {
        NONE(0), YELLOW(13), BLUE(45), PURPLE(53), RED(72), LIGHT_BLUE(79), LIGHT_GREEN(21), DARK_PURPLE(81)
    }
}