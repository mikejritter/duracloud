/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duraboss.rest.report;

import org.duracloud.client.ContentStoreManager;
import org.duracloud.common.util.CalendarUtil;
import org.duracloud.reporter.error.InvalidScheduleException;
import org.duracloud.reporter.storage.StorageReportBuilder;
import org.duracloud.reporter.storage.StorageReportHandler;
import org.duracloud.reporter.storage.StorageReportScheduler;
import org.duracloud.error.ContentStoreException;
import org.duracloud.reportdata.storage.StorageReportInfo;
import org.duracloud.reportdata.storage.StorageReportList;
import org.duracloud.reportdata.storage.serialize.StorageReportInfoSerializer;
import org.duracloud.reportdata.storage.serialize.StorageReportListSerializer;

import java.io.InputStream;
import java.util.Date;

import static org.duracloud.common.util.CalendarUtil.DAY_OF_WEEK.SAT;

/**
 * First line of business logic to handle the requests coming in via the
 * Storage Report REST API.
 *
 * @author: Bill Branan
 * Date: 5/12/11
 */
public class StorageReportResource {

    private String reportPrefix = null;
    private String errorLogName = null;
    private ContentStoreManager storeMgr = null;
    private StorageReportBuilder reportBuilder;
    private StorageReportHandler reportHandler;
    private StorageReportScheduler reportScheduler;

    public StorageReportResource(String reportPrefix, String errorLogName) {
        this.reportPrefix = reportPrefix;
        this.errorLogName = errorLogName;
    }

    public void initialize(ContentStoreManager storeMgr, String reportSpaceId) {
        this.storeMgr = storeMgr;
        this.reportHandler = new StorageReportHandler(storeMgr,
                                                      reportSpaceId,
                                                      reportPrefix,
                                                      errorLogName);
        this.reportBuilder = new StorageReportBuilder(storeMgr, reportHandler);

        // if a scheduler already exists, shut it down before making a new one
        if(null != this.reportScheduler) {
            this.reportScheduler.cancelStorageReportSchedule();
            this.reportScheduler.cancelStorageReport();
        }
        this.reportScheduler = new StorageReportScheduler(reportBuilder);

        // adds default report schedule: weekly at 1 AM on Saturday
        scheduleStorageReport(getDefaultScheduleStartDate().getTime(),
                              CalendarUtil.ONE_WEEK_MILLIS);
        // start a report now
        startStorageReport();
    }

    protected Date getDefaultScheduleStartDate() {
        return new CalendarUtil().getDateAtOneAmNext(SAT);
    }

    /**
     * Initialization option for tests only.
     */
    protected void initialize(ContentStoreManager storeMgr,
                              StorageReportHandler reportHander,
                              StorageReportBuilder reportBuilder,
                              StorageReportScheduler reportScheduler) {
        this.storeMgr = storeMgr;
        this.reportHandler = reportHander;
        this.reportBuilder = reportBuilder;
        this.reportScheduler = reportScheduler;
    }

    /**
     * Indicates whether or not initialization has occurred.
     */
    public boolean isInitialized() {
        try {
            checkInitialized();
            return true;
        } catch(RuntimeException e) {
            return false;
        }
    }

    /**
     * Provides the xml stream of the lastest storage report or null if no
     * reports exist.
     */
    public InputStream getLatestStorageReport() throws ContentStoreException {
        checkInitialized();
        return reportHandler.getLatestStorageReportStream();
    }

    /**
     * Provides the xml stream of the specified storage report or null if the
     * report does not exist.
     */
    public InputStream getStorageReport(String reportId)
        throws ContentStoreException {
        checkInitialized();
        return reportHandler.getStorageReportStream(reportId);
    }

    /**
     * Provides the xml stream of the list of storage reports, the list may
     * be empty
     */
    public String getStorageReportList() throws ContentStoreException {
        checkInitialized();
        StorageReportList reportList = reportHandler.getStorageReportList();
        StorageReportListSerializer serializer =
            new StorageReportListSerializer();
        return serializer.serialize(reportList);
    }

    /**
     * Provides information about the storage report. This will include
     * status of any running reports as well data about the last successful
     * report run.
     *
     * @return XML serialized set of information about the storage report system
     */
    public String getStorageReportInfo() {
        checkInitialized();
        StorageReportInfo reportInfo = new StorageReportInfo();
        StorageReportBuilder.Status status = reportBuilder.getStatus();
        reportInfo.setStatus(status.name());
        if(status.equals(StorageReportBuilder.Status.ERROR)) {
            reportInfo.setError(reportBuilder.getError());
        }

        long startTime = reportBuilder.getStartTime();
        long stopTime = reportBuilder.getStopTime();
        long elapsedTime = reportBuilder.getElapsedTime();
        long count = reportBuilder.getCurrentCount();

        reportInfo.setStartTime(startTime);
        if(stopTime < startTime) { // A new run has started since the last stop
            if(elapsedTime > 0) { // A previous run has completed
                long estCompletionTime = startTime + elapsedTime;
                reportInfo.setEstimatedCompletionTime(estCompletionTime);
            }

            reportInfo.setCurrentCount(count);
        } else { // No new runs since the last stop
            reportInfo.setCompletionTime(stopTime);
            reportInfo.setFinalCount(count);
        }

        Date nextScheduledDate = reportScheduler.getNextScheduledStartDate();
        if(null != nextScheduledDate) {
            reportInfo.setNextScheduledStartTime(nextScheduledDate.getTime());
        }

        StorageReportInfoSerializer serializer =
            new StorageReportInfoSerializer();
        return serializer.serialize(reportInfo);
    }

    /**
     * Starts a new storage report if one is not currently running.
     *
     * @return String indicating a successful start or that a report is running
     */
    public String startStorageReport() {
        checkInitialized();
        return reportScheduler.startStorageReport();
    }

    /**
     * Schedules a series of storage reports to run. The first such report
     * will begin at the indicated start time, followed by reports at the
     * given frequency.
     *
     * @param startTime time to start the next storage report
     * @param frequency time in milliseconds to wait between reports
     * @return String indicating the successful creation of a report schedule
     * @throws InvalidScheduleException if the parameters do not create a
     *                                  valid schedule
     */
    public String scheduleStorageReport(long startTime, long frequency)
        throws InvalidScheduleException {
        checkInitialized();

        if(new Date(startTime).before(new Date())) {
            throw new InvalidScheduleException("Cannot set report schedule" +
                                               " which starts in the past");
        }

        if(frequency < 600000) {
            throw new InvalidScheduleException("Minimum frequency for report " +
                                               "schedule is 10 minutes.");
        }
        return reportScheduler.scheduleStorageReport(new Date(startTime),
                                                     frequency);
    }

    /**
     * Cancels any existing storage report schedule.
     * @return String indicating the successful cancellation of a report schedule
     */
    public String cancelStorageReportSchedule() {
        checkInitialized();
        return reportScheduler.cancelStorageReportSchedule();
    }

    /**
     * Cancels a storage report that is currently in process
     * @return String indicating the cancellation of the current storage report
     */
    public String cancelStorageReport() {
        return reportScheduler.cancelStorageReport();
    }

    /**
     * Shuts down this resource
     */
    public void dispose() {
        cancelStorageReport();
    }

    private void checkInitialized() {
        if(null == storeMgr) {
            throw new RuntimeException("DuraBoss must be initialized.");
        }
    }

}