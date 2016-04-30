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

import com.leapmotion.leap.Controller
import com.leapmotion.leap.Gesture
import org.slf4j.Logger
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject

/**
 * The leap Motion controller instance. Creates the actual controller to interact
 * with the device, registers a listener and translates frames to Launchpad events.
 * The API controller and listener instances where for some reason not CDI compatible,
 * wiring resulted in strange NullPointerExceptions.
 */
@ApplicationScoped
open class LeapMotionController @Inject constructor(private val launchpad: LaunchpadController,
                                                    private val logger: Logger) {

    private lateinit var controller: Controller
    private val listener = LeapMotionListener(launchpad, logger)

    open var enabled: Boolean = false
    open var connected: Boolean by listener

    @PostConstruct
    open fun connect() {
        try {
            controller = Controller(listener)
            controller.setPolicy(Controller.PolicyFlag.POLICY_BACKGROUND_FRAMES)

            controller.enableGesture(Gesture.Type.TYPE_SWIPE)
            controller.enableGesture(Gesture.Type.TYPE_SCREEN_TAP)
            controller.enableGesture(Gesture.Type.TYPE_KEY_TAP)
            enabled = true
        } catch (e: Exception) {
            enabled = false
        } catch (e: UnsatisfiedLinkError) {
            logger.warn("You need to set -Djava.library.path to point to your Leap motion libs.")
            enabled = false
        }
    }

    @PreDestroy
    open fun disconnect() {
        if (enabled) {
            controller.removeListener(listener)
            connected = false
        }
    }
}