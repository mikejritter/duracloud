/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.storage.xml.StorageAccountsDocumentBinding;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.impl.StorageAccountImpl;
import org.duracloud.storage.domain.StorageProviderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This class holds the configuration elements for durastore.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class DurastoreConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(DurastoreConfig.class);

    private static final String INIT_RESOURCE = "/stores";
    public static final String QUALIFIER = "durastore";

    protected static final String storageAccountKey = "storage-acct";
    protected static final String storageClassKey = "storage-class";
    protected static final String ownerIdKey = "owner-id";
    protected static final String isPrimaryKey = "is-primary";
    protected static final String idKey = "id";
    protected static final String providerTypeKey = "provider-type";
    protected static final String usernameKey = "username";
    protected static final String passwordKey = "password";

    private Map<String, StorageAccount> storageAccounts =
        new HashMap<String, StorageAccount>();

    private StorageAccountsDocumentBinding documentBinding =
        new StorageAccountsDocumentBinding();

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        if (key.startsWith(storageAccountKey)) {
            String suffix = getSuffix(key);
            loadStorageAcct(suffix, value);

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadStorageAcct(String key, String value) {
        String id = getPrefix(key);
        StorageAccount acct = storageAccounts.get(id);
        if (null == acct) {
            acct = new StorageAccountImpl(null, null, null, null);
        }

        String suffix = getSuffix(key);
        if (suffix.equalsIgnoreCase(idKey)) {
            acct.setId(value);

        } else if (suffix.equalsIgnoreCase(ownerIdKey)) {
            acct.setOwnerId(value);

        } else if (suffix.equalsIgnoreCase(isPrimaryKey)) {
            acct.setPrimary(Boolean.valueOf(value));

        } else if (suffix.equalsIgnoreCase(providerTypeKey)) {
            acct.setType(StorageProviderType.fromString(value));

        } else if (suffix.equalsIgnoreCase(usernameKey)) {
            acct.setUsername(value);

        } else if (suffix.equalsIgnoreCase(passwordKey)) {
            acct.setPassword(value);

        } else if (suffix.equalsIgnoreCase(storageClassKey)) {
            acct.setProperty(StorageAccount.PROPS.STORAGE_CLASS.name(), value);

        } else {
            String msg = "unknown acct key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        storageAccounts.put(id, acct);
    }

    public Collection<StorageAccount> getStorageAccounts() {
        return storageAccounts.values();
    }

    /**
     * Directly sets storage provider accounts. The list of storage accounts
     * is cleared prior to the new set being applied, so no accounts included
     * prior to this call will remain.
     *
     * @param storageAccts storage provider accounts to set
     */
    public void setStorageAccounts(Set<StorageAccount> storageAccts) {
        this.storageAccounts = new HashMap<String, StorageAccount>();
        for(StorageAccount storageAcct : storageAccts) {
            this.storageAccounts.put(storageAcct.getId(), storageAcct);
        }
    }

    public String asXml() {
        boolean includeCredentials = true;
        return documentBinding.createDocumentFrom(getStorageAccounts(),
                                                  includeCredentials);
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }
}
