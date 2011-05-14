/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageProviderType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class DurastoreConfigTest {

    private static final int NUM_ACCTS = 3;

    private String[] ownerIds = {"ownerId0", "ownerId1", "ownerId2"};
    private String[] primaries = {"false", "false", "true"};
    private String[] ids = {"id0", "id1", "id2"};
    private String[] types = {StorageProviderType.AMAZON_S3.toString(),
                              StorageProviderType.EMC.toString(),
                              StorageProviderType.RACKSPACE.toString()};
    private String[] usernames = {"username0", "username1", "username2"};
    private String[] passwords = {"password0", "password1", "password2"};
    private String storageClass = "storageclass";

    @Test
    public void testLoad() {
        DurastoreConfig config = new DurastoreConfig();
        config.load(createProps());
        verifyDurastoreConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();

        String dot = ".";
        String prefix = DurastoreConfig.QUALIFIER + dot +
            DurastoreConfig.storageAccountKey + dot;

        for (int i = 0; i < NUM_ACCTS; ++i) {
            String p = prefix + i + dot;
            props.put(p + DurastoreConfig.usernameKey, usernames[i]);
            props.put(p + DurastoreConfig.passwordKey, passwords[i]);
            props.put(p + DurastoreConfig.isPrimaryKey, primaries[i]);
            props.put(p + DurastoreConfig.idKey, ids[i]);
            props.put(p + DurastoreConfig.ownerIdKey, ownerIds[i]);
            props.put(p + DurastoreConfig.providerTypeKey, types[i]);

            if (types[i] == StorageProviderType.AMAZON_S3.toString()) {
                props.put(p + DurastoreConfig.storageClassKey, storageClass);
            }
        }
        return props;
    }

    private void verifyDurastoreConfig(DurastoreConfig config) {

        Collection<StorageAccount> accts = config.getStorageAccounts();
        Assert.assertNotNull(accts);
        Assert.assertEquals(NUM_ACCTS, accts.size());

        boolean[] verified = {false, false, false};
        for (StorageAccount acct : accts) {
            for (int i = 0; i < NUM_ACCTS; ++i) {
                if (usernames[i].equals(acct.getUsername())) {
                    verifyAcct(acct, i);
                    verified[i] = true;
                }
            }
        }

        for (boolean v : verified) {
            Assert.assertTrue(v);
        }
    }

    private void verifyAcct(StorageAccount acct, int i) {
        Assert.assertNotNull(acct);

        String username = acct.getUsername();
        String password = acct.getPassword();
        Assert.assertNotNull(username);
        Assert.assertNotNull(password);
        Assert.assertEquals(usernames[i], username);
        Assert.assertEquals(passwords[i], password);

        Assert.assertEquals(Boolean.valueOf(primaries[i]), acct.isPrimary());
        Assert.assertEquals(ids[i], acct.getId());
        Assert.assertEquals(ownerIds[i], acct.getOwnerId());

        StorageProviderType type = StorageProviderType.fromString(types[i]);
        Assert.assertEquals(type, acct.getType());
        if (type == StorageProviderType.AMAZON_S3) {
            Assert.assertEquals(acct.getProperty(StorageAccount.PROPS
                                                     .STORAGE_CLASS
                                                     .name()), storageClass);
        }
    }

}
