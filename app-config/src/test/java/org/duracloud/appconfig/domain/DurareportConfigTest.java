/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class DurareportConfigTest {

    private String durastoreHost = "durastoreHost";
    private String durastorePort = "durastorePort";
    private String durastoreContext = "durastoreContext";
    private String duraserviceHost = "duraserviceHost";
    private String duraservicePort = "duraservicePort";
    private String duraserviceContext = "duraserviceContext";

    @Test
    public void testLoad() {
        DurareportConfig config = new DurareportConfig();
        config.load(createProps());
        verifyDurareportConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String p = DurareportConfig.QUALIFIER + ".";

        props.put(p + DurareportConfig.duraStoreHostKey, durastoreHost);
        props.put(p + DurareportConfig.duraStorePortKey, durastorePort);
        props.put(p + DurareportConfig.duraStoreContextKey, durastoreContext);
        props.put(p + DurareportConfig.duraServiceHostKey, duraserviceHost);
        props.put(p + DurareportConfig.duraServicePortKey, duraservicePort);
        props.put(p + DurareportConfig.duraServiceContextKey, duraserviceContext);

        return props;
    }

    private void verifyDurareportConfig(DurareportConfig config) {

        Assert.assertNotNull(config.getDurastoreHost());
        Assert.assertNotNull(config.getDurastorePort());
        Assert.assertNotNull(config.getDurastoreContext());
        Assert.assertNotNull(config.getDuraserviceHost());
        Assert.assertNotNull(config.getDuraservicePort());
        Assert.assertNotNull(config.getDuraserviceContext());

        Assert.assertEquals(durastoreHost, config.getDurastoreHost());
        Assert.assertEquals(durastorePort, config.getDurastorePort());
        Assert.assertEquals(durastoreContext, config.getDurastoreContext());
        Assert.assertEquals(duraserviceHost, config.getDuraserviceHost());
        Assert.assertEquals(duraservicePort, config.getDuraservicePort());
        Assert.assertEquals(duraserviceContext, config.getDuraserviceContext());
    }

}