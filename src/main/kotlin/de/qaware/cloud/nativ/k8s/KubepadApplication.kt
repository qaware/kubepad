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

package de.qaware.cloud.nativ.k8s

import de.qaware.cloud.nativ.k8s.launchpad.LaunchpadController
import de.qaware.cloud.nativ.k8s.launchpad.LaunchpadMK2
import org.apache.deltaspike.cdise.api.CdiContainerLoader.getCdiContainer
import org.apache.deltaspike.core.api.provider.BeanProvider.getContextualReference
import org.slf4j.bridge.SLF4JBridgeHandler
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.context.Dependent

/**
 * The main application class for the K8S Cloud Launchpad.
 *
 * -Djava.library.path=.:./lib
 */
fun main(args: Array<String>) {
    SLF4JBridgeHandler.removeHandlersForRootLogger()
    SLF4JBridgeHandler.install()

    // get the current CDI container
    val cdiContainer = getCdiContainer()
    cdiContainer.boot();

    // and initialize the CDI container control
    val contextControl = cdiContainer.contextControl;
    contextControl.startContext(Dependent::class.java)
    contextControl.startContext(ApplicationScoped::class.java)

    // reset any buttons of the Launchpad
    val launchpad = getContextualReference(LaunchpadMK2::class.java)
    launchpad.reset()

    val controller = getContextualReference(LaunchpadController::class.java)
    controller.write("K8S Cloud Launchpad")
    controller.init()

    /*
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
    */

    // ensure we shutdown nicely on exit
    Runtime.getRuntime().addShutdownHook(Thread() { cdiContainer.shutdown() })
}
