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
package de.qaware.cloud.nativ.kpad;

import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.api.model.ReplicationController;
import io.fabric8.kubernetes.api.model.ReplicationControllerList;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * Integration test for the Kubernetes Client API.
 */
public class KubernetesClientIT {

    private KubernetesClient client;

    @Before
    public void setUp() throws Exception {
        Config config = new ConfigBuilder().withMasterUrl("https://10.245.1.2").build();
        client = new DefaultKubernetesClient(config);
    }

    @Test
    public void testGetPods() {
        PodList list = client.pods().inNamespace("kube-system").list();
        List<Pod> pods = list.getItems();
        assertThat(pods, hasSize(5));

        list = client.pods().inNamespace("default").list();
        pods = list.getItems();
        for (Pod p : pods) {
            System.out.println(p.toString());
        }
    }

    @Test
    public void testGetReplicationControllers() {
        ReplicationControllerList list = client.replicationControllers().inNamespace("default").list();
        List<ReplicationController> items = list.getItems();
        for (ReplicationController rc : items) {
            System.out.println(rc.toString());

        }
    }

    @Test
    public void testScaleReplicationController() {
        // this works a little strange because it seems to stop all the containers
        // and then start the desired amount of replicas
        ReplicationController rc = client.replicationControllers().inNamespace("default").withName("nginx-rc")
                .edit()
                .editSpec().withReplicas(2).endSpec()
                .done();
    }

    @Test
    public void testScaleDeployments() {
        // this here works as expected when scaling the deployment
        Deployment deployment = client.extensions().deployments()
                .inNamespace("default").withName("nginx")
                .edit()
                .editSpec().withReplicas(2).endSpec()
                .done();
    }
}
