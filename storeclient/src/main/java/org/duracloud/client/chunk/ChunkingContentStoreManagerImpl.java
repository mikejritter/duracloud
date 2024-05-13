/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.client.chunk;

import java.util.Map;

import org.duracloud.client.ContentStore;
import org.duracloud.client.ContentStoreManagerImpl;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.error.ContentStoreException;
import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.domain.StorageAccountManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author shake
 */
public class ChunkingContentStoreManagerImpl extends ContentStoreManagerImpl {

    private static final Logger log = LoggerFactory.getLogger(ChunkingContentStoreManagerImpl.class);

    private Map<String, ContentStore> contentStores;
    private ContentStore primaryContentStore;

    public ChunkingContentStoreManagerImpl(String host, String port) {
        super(host, port);
    }

    public ChunkingContentStoreManagerImpl(String host,
                                          String port,
                                          String context) {
        super(host, port, context);
    }

    /*
    @Override
    public Map<String, ContentStore> getContentStores()
        throws ContentStoreException {
        log.debug("enter: getContentStores()");
        if (null == contentStores) {
            log.debug("populating cache.");
            this.contentStores = super.getContentStores();
        }
        return this.contentStores;
    }

    */
    @Override
    public ContentStore getPrimaryContentStore() throws ContentStoreException {
        return getPrimaryContentStore(-1);
    }

    @Override
    public ContentStore getPrimaryContentStore(int maxRetries) throws ContentStoreException {
        StorageAccountManager acctManager = getStorageAccounts();
        StorageAccount acct = acctManager.getPrimaryStorageAccount();
        return newContentStoreImpl(acct, maxRetries);
    }

    protected ContentStore newContentStoreImpl(StorageAccount acct, int maxRetries) {
        log.info("Creating new ChunkingContentStoreImpl");
        return new ChunkingContentStoreImpl(getBaseURL(),
                                    acct.getType(),
                                    acct.getId(),
                                    isWritable(acct),
                                    getRestHelper(),
                                    maxRetries);
    }

    protected void setRestHelper(RestHttpHelper restHelper) {
        super.setRestHelper(restHelper);
    }
}
