/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.audit.reader;

import java.io.InputStream;

/**
 * 
 * @author Daniel Bernstein Date: Sept. 17, 2014
 * 
 */
public interface AuditLogReader {
    /**
     * Returns the audit log as a tsv stream of audit events from first to last.
     * 
     * @param account The account identifier - ie usually the subdomain of the original request.
     * @param storeId
     * @param spaceId
     * @return
     * @throws AuditLogEmptyException 
     */
    InputStream gitAuditLog(String account, String storeId, String spaceId)
        throws AuditLogEmptyException;

}