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

import de.qaware.cloud.nativ.kpad.ClusterNodeEvent
import de.qaware.cloud.nativ.kpad.ClusterNodeGrid
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadEvent.Switch
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.Button
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.Color.*
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2.Square
import org.slf4j.Logger
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.event.Event
import javax.enterprise.event.Observes
import javax.enterprise.util.AnnotationLiteral
import javax.inject.Inject

/**
 * The controller for our Launchpad business logic. Knows how to react to certain
 * button events, executes business logic and triggers launchpad actions again.
 */
@ApplicationScoped
open class LaunchpadController @Inject constructor(private val grid: ClusterNodeGrid,
                                                   private val snakeGame: SnakeGame,
                                                   private val launchpad: Event<LaunchpadEvent>,
                                                   private val nodes: Event<ClusterNodeEvent>,
                                                   private val logger: Logger) {

    private var activeRow = -1

    /**
     * Called when we have a button pressed event.
     *
     * @param event the event data
     */
    open fun onButtonPressed(@Observes @SwitchableEvent.Pressed event: SwitchableEvent) {
        if (snakeGame.running())
            return

        logger.debug("{} pressed. {}", event.switchable, snakeGame.running())

        val rows = grid.rows()
        val contained = activeRow in rows

        when (event.switchable) {
        // select next row up
            Button.CURSOR_UP -> {
                up(rows, contained)
            }
        // select next row down
            Button.CURSOR_DOWN -> {
                down(rows, contained)
            }
        // advance row to the right by one square
            Button.CURSOR_RIGHT -> {
                if (activeRow > -1) {
                    light(Switch.ON, event.switchable, PURPLE)

                    var column = grid.next(activeRow)
                    if (column < 8) {
                        val square = Square(activeRow, column)
                        start(square)
                    }
                }
            }
        // reduce row to the left by one square
            Button.CURSOR_LEFT -> {
                if (activeRow > -1) {
                    light(Switch.ON, event.switchable, PURPLE)

                    var column = grid.last(activeRow)
                    if (column > -1) {
                        val square = Square(activeRow, column)
                        stop(square)
                    }
                }
            }

            Button.SESSION -> {
                grid.stopAll()
            }

            Button.USER_1 -> {
                grid.startAll()
            }

            Button.USER_2 -> {
                reset()
            }

            Button.MIXER -> { //handled by snake game
                logger.debug("Start Game!")
                Thread(snakeGame).start()
            }

        // direct selection of active row button
            Button.VOLUME, Button.PAN, Button.SEND_A, Button.SEND_B,
            Button.STOP, Button.MUTE, Button.SOLO, Button.RECORD -> {
                val button = event.switchable

                if (button.row in rows) {
                    light(Switch.ON, event.switchable, BLUE)
                    if (activeRow > -1) {
                        light(Switch.ON, Button.right(activeRow), PURPLE)
                    }
                    activeRow = event.switchable.row
                }
            }
        // then it must be a square button
            else -> {
                if (event.switchable is Square) {
                    val square = event.switchable

                    if (square.row in rows) {
                        val running = grid[square.row][square.column].active.get()
                        if (running) {
                            stop(square)
                        } else {
                            start(square)
                        }
                    }
                }
            }
        }
    }

    private fun up(rows: List<Int>, contained: Boolean) {
        if (activeRow > -1 && rows.size > 1) {
            val nextRow = rows.subList(rows.indexOf(activeRow + 1), rows.size).getOrElse(0) { -1 }

            if (contained && nextRow > -1) {
                light(Switch.ON, Button.right(activeRow), PURPLE)
                light(Switch.ON, Button.right(nextRow), BLUE)
                activeRow = nextRow
            }
        }
    }

    private fun down(rows: List<Int>, contained: Boolean) {
        if (activeRow > -1 && rows.size > 1) {
            val nextRow = rows.subList(0, rows.indexOf(activeRow)).reversed().getOrElse(0) { -1 }

            if (contained && nextRow > -1) {
                light(Switch.ON, Button.right(activeRow), PURPLE)
                light(Switch.ON, Button.right(nextRow), BLUE)
                activeRow = nextRow
            }
        }
    }

    private fun start(square: Square) {
        nodes.select(object : AnnotationLiteral<ClusterNodeEvent.Start>() {})
                .fire(ClusterNodeEvent(square.row, square.column))
    }

    private fun stop(square: Square) {
        nodes.select(object : AnnotationLiteral<ClusterNodeEvent.Stop>() {})
                .fire(ClusterNodeEvent(square.row, square.column))
    }

    open fun reset() {
        resetLaunchpad()
        initActionButtons()
        grid.reset()
    }

    /**
     * Called when we have a button released event.
     *
     * @param event the event data
     */
    open fun onButtonReleased(@Observes @SwitchableEvent.Released event: SwitchableEvent) {
        if (snakeGame.running())
            return

        logger.debug("{} released.", event.switchable)

        when (event.switchable) {
            Button.CURSOR_UP, Button.CURSOR_DOWN,
            Button.CURSOR_LEFT, Button.CURSOR_RIGHT -> {
                light(Switch.ON, event.switchable, BLUE)
            }
        }
    }

    /**
     * Scale the current row by the number of fingers.
     *
     * @param fingers number of fingers
     */
    open fun scale(fingers: Int) {
        if (fingers > -1) {
            grid.scale(activeRow, fingers)
        }
    }

    /**
     * Move the current row one up.
     */
    open fun up() {
        val rows = grid.rows()
        val contained = activeRow in rows
        up(rows, contained)
    }

    /**
     * Move the current row one down.
     */
    open fun down() {
        val rows = grid.rows()
        val contained = activeRow in rows
        down(rows, contained)
    }

    /**
     * A node in the grid has been started. Update Launchpad MK2.
     *
     * @param event the node event data
     */
    open fun starting(@Observes @ClusterNodeEvent.Starting event: ClusterNodeEvent) {
        val square = Square(event.row, event.column)
        pulse(square, grid.color(event.row))
    }

    /**
     * A node in the grid has been started. Update Launchpad MK2.
     *
     * @param event the node event data
     */
    open fun started(@Observes @ClusterNodeEvent.Started event: ClusterNodeEvent) {
        if(event.column == 8) {
            light(Switch.ON, Button.right(event.row), PURPLE)
            return
        }

        val square = Square(event.row, event.column)
        light(Switch.ON, square, grid.color(event.row))
    }

    /**
     * A node in the grid has been stopped. Update Launchpad MK2.
     *
     * @param event the node event data
     */
    open fun stopping(@Observes @ClusterNodeEvent.Stopping event: ClusterNodeEvent) {
        val square = Square(event.row, event.column)
        pulse(square, grid.color(event.row))
    }

    /**
     * A node in the grid has been stopped. Update Launchpad MK2.
     *
     * @param event the node event data
     */
    open fun stopped(@Observes @ClusterNodeEvent.Stopped event: ClusterNodeEvent) {
        if(event.column == 8) {
            light(Switch.OFF, Button.right(event.row))
            if(event.row == activeRow) activeRow = -1
            return
        }

        val square = Square(event.row, event.column)
        light(Switch.OFF, square)
    }

    /**
     * Initialize the action buttons (top row).
     */
    private fun initActionButtons() {
        logger.info("Initializing action buttons.")

        light(Switch.ON, Button.CURSOR_UP, BLUE)
        light(Switch.ON, Button.CURSOR_DOWN, BLUE)
        light(Switch.ON, Button.CURSOR_LEFT, BLUE)
        light(Switch.ON, Button.CURSOR_RIGHT, BLUE)

        light(Switch.ON, Button.SESSION, RED)
        light(Switch.ON, Button.USER_1, LIGHT_GREEN)
        light(Switch.ON, Button.USER_2, YELLOW)
        light(Switch.ON, Button.MIXER, LIGHT_BLUE)
    }

    /**
     * Writes the given message on the Launchpad. Currently this will only work on Windows!
     */
    open fun write(message: String) {
        logger.info(message)
        text(message, BLUE)
    }

    private fun pulse(switchable: LaunchpadMK2.Switchable, color: LaunchpadMK2.Color) {
        val event = LaunchpadEvent.light(Switch.ON, switchable, color)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Pulse>() {}).fire(event)
    }

    private fun blink(switchable: LaunchpadMK2.Switchable, color: LaunchpadMK2.Color) {
        val event = LaunchpadEvent.light(Switch.ON, switchable, color)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Blink>() {}).fire(event)
    }

    private fun light(switch: Switch, switchable: LaunchpadMK2.Switchable, color: LaunchpadMK2.Color = NONE) {
        val event = LaunchpadEvent.light(switch, switchable, color)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Light>() {}).fire(event)
    }

    private fun text(message: String, color: LaunchpadMK2.Color) {
        val event = LaunchpadEvent.text(message, color)
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Text>() {}).fire(event)
    }

    private fun resetLaunchpad() {
        val event = LaunchpadEvent.reset()
        launchpad.select(object : AnnotationLiteral<LaunchpadEvent.Reset>() {}).fire(event)
    }
}