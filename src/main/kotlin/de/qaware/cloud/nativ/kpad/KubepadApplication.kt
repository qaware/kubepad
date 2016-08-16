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
@file:JvmName("KubepadApplication")

package de.qaware.cloud.nativ.kpad

import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadController
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadEvent
import de.qaware.cloud.nativ.kpad.launchpad.LaunchpadMK2
import de.qaware.cloud.nativ.kpad.leapmotion.LeapMotionController
import org.apache.deltaspike.cdise.api.CdiContainerLoader.getCdiContainer
import org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference
import org.slf4j.bridge.SLF4JBridgeHandler
import java.util.logging.Logger
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent

/**
 * The main application class for the Cloud Launchpad.
 * K8S and Marathon (DC/OS) are supported.
 *
 * To switch between them set -Dcluster.service to either kubernetes or marathon.
 * For additional configuration have a look at the cluster.properties file
 *
 * -Djava.library.path=.:./lib
 */
fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // this seems to be required at least under Windows
    System.setProperty("javax.sound.midi.Transmitter", "com.sun.media.sound.MidiInDeviceProvider#Launchpad MK2")
    System.setProperty("javax.sound.midi.Receiver", "com.sun.media.sound.MidiOutDeviceProvider#Launchpad MK2")

    // get cluster service property or initialize default
    var clusterService = System.getProperty("cluster.service")
    if (clusterService == null) {
        clusterService = "kubernetes"
        System.setProperty("cluster.service", clusterService)
    }

    // get the current CDI container
    val cdiContainer = getCdiContainer()
    cdiContainer.boot()

    // and initialize the CDI container control
    val contextControl = cdiContainer.contextControl;
    contextControl.startContext(Dependent::class.java)
    contextControl.startContext(ApplicationScoped::class.java)

    // reset any buttons of the Launchpad
    val launchpad = getContextualReference(LaunchpadMK2::class.java)
    launchpad.reset(LaunchpadEvent.reset())

    val controller = getContextualReference(LaunchpadController::class.java)
    controller.write(clusterService)
    controller.reset()

    val logger = Logger.getLogger(LeapMotionController::class.java.name)
    val leap = getContextualReference(LeapMotionController::class.java)
    if (leap.enabled) {
        logger.info("Leap Motion support enabled.")
        if (leap.connected) {
            logger.info("Leap Motion connected. Use gestures to control Kubernetes.")
        }
    } else {
        logger.warning("No Leap Motion support.")
    }

    // ensure we shutdown nicely on exit
    Runtime.getRuntime().addShutdownHook(Thread() { cdiContainer.shutdown() })
}
