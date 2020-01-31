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
package de.qaware.cloud.nativ.kpad.openshift

import io.fabric8.openshift.client.DefaultOpenShiftClient
import io.fabric8.openshift.client.OpenShiftClient
import io.fabric8.openshift.client.OpenShiftConfig
import io.fabric8.openshift.client.OpenShiftConfigBuilder
import org.apache.deltaspike.core.api.config.ConfigProperty
import org.apache.deltaspike.core.api.exclude.Exclude
import javax.enterprise.context.ApplicationScoped
import javax.enterprise.inject.Default
import javax.enterprise.inject.Produces
import javax.inject.Inject

/**
 * The CDI producer for the OpenShift Java API.
 */
@Exclude(onExpression = "cluster.service!=openshift")
@ApplicationScoped
open class OpenShiftProducer @Inject constructor(@ConfigProperty(name = "openshift.url")
                                                 private val master: String?) {

    @Produces
    @Default
    open fun openShiftClient(): OpenShiftClient {
        System.setProperty(OpenShiftConfig.KUBERNETES_TRUST_CERT_SYSTEM_PROPERTY, "true")
        if (!master.isNullOrBlank()) {
            // only set the master if specified
            System.setProperty(OpenShiftConfig.KUBERNETES_MASTER_SYSTEM_PROPERTY, master)
            System.setProperty(OpenShiftConfig.OPENSHIFT_URL_SYSTEM_PROPERTY, master)
        }

        val config = OpenShiftConfigBuilder(false).build()
        return DefaultOpenShiftClient(config)
    }
}