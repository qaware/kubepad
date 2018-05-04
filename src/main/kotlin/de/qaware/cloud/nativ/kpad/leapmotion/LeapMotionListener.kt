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
package de.qaware.cloud.nativ.kpad.leapmotion

import com.leapmotion.leap.*
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadController
import org.slf4j.Logger
import kotlin.reflect.KProperty

/**
 * The Leap motion listener implementation to handle frames and gestures.
 */
class LeapMotionListener constructor(private val launchpad: LaunchpadController,
                                     private val logger: Logger) : Listener() {

    var connected = false

    override fun onConnect(controller: Controller?) {
        logger.info("Leap Motion device connected.")
        connected = true
    }

    override fun onFrame(controller: Controller?) {
        val frame = controller?.frame()
        val gestures = frame?.gestures()

        for (gesture in gestures!!.iterator()) {
            when (gesture.type()) {
                Gesture.Type.TYPE_SWIPE -> {
                    val swipe = SwipeGesture(gesture)
                    if (swipe.state() == Gesture.State.STATE_STOP) {
                        // scale cluster by number of fingers
                        val fingers = frame.fingers()?.count() ?: -1
                        logger.debug("Detected swipe gesture with {} fingers.", fingers)
                        launchpad.scale(fingers)
                    }
                }
                Gesture.Type.TYPE_SCREEN_TAP -> {
                    val screenTap = ScreenTapGesture(gesture)
                    if (screenTap.state() == Gesture.State.STATE_STOP) {
                        // one row up
                        logger.debug("Detected screen tap. One row up.")
                        launchpad.up()
                    }
                }
                Gesture.Type.TYPE_KEY_TAP -> {
                    val keyTap = KeyTapGesture(gesture)
                    if (keyTap.state() == Gesture.State.STATE_STOP) {
                        // one row down
                        logger.debug("Detected key tap. One row down.")
                        launchpad.down()
                    }
                }
                else -> {
                    // nothing to do
                }
            }
        }

    }

    override fun onDisconnect(controller: Controller?) {
        logger.debug("Leap Motion device disconnected.")
        connected = false
    }

    operator fun getValue(controller: LeapMotionController, property: KProperty<*>): Boolean = connected

    operator fun setValue(controller: LeapMotionController, property: KProperty<*>, b: Boolean) {
        this.connected = b
    }
}