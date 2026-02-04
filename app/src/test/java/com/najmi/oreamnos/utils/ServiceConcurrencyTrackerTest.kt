package com.najmi.oreamnos.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ServiceConcurrencyTrackerTest {

    @Test
    fun testSequentialExecution() {
        val tracker = ServiceConcurrencyTracker()

        // Request 1 starts
        tracker.onServiceStarted(1)
        assertEquals(1, tracker.getActiveJobs())

        // Request 1 finishes
        val stopId = tracker.onJobFinished()
        assertNotNull("Should return stopId when all jobs done", stopId)
        assertEquals(1, stopId)
        assertEquals(0, tracker.getActiveJobs())
    }

    @Test
    fun testConcurrentExecution_Job2FinishesLast() {
        val tracker = ServiceConcurrencyTracker()

        // Request 1 starts
        tracker.onServiceStarted(1)

        // Request 2 starts (overlapped)
        tracker.onServiceStarted(2)
        assertEquals(2, tracker.getActiveJobs())

        // Request 1 finishes first
        val stopId1 = tracker.onJobFinished()
        assertNull("Should NOT stop service while job 2 is running", stopId1)
        assertEquals(1, tracker.getActiveJobs())

        // Request 2 finishes
        val stopId2 = tracker.onJobFinished()
        assertNotNull("Should return stopId when all jobs done", stopId2)
        assertEquals(2, stopId2)
        assertEquals(0, tracker.getActiveJobs())
    }

    @Test
    fun testConcurrentExecution_Job2FinishesFirst() {
        val tracker = ServiceConcurrencyTracker()

        // Request 1 starts
        tracker.onServiceStarted(1)

        // Request 2 starts (overlapped)
        tracker.onServiceStarted(2)

        // Request 2 finishes first (fast failure maybe)
        val stopId2 = tracker.onJobFinished()
        assertNull("Should NOT stop service while job 1 is running", stopId2)
        assertEquals(1, tracker.getActiveJobs())

        // Request 1 finishes
        val stopId1 = tracker.onJobFinished()
        assertNotNull("Should return stopId when all jobs done", stopId1)
        assertEquals(2, stopId1) // Should use LATEST startId (2) even if job 1 was for startId 1
        assertEquals(0, tracker.getActiveJobs())
    }

    @Test
    fun testUnderflowProtection() {
        val tracker = ServiceConcurrencyTracker()

        tracker.onServiceStarted(1)
        tracker.onJobFinished() // 0 jobs

        // Extra finish call (should be handled safely)
        val stopId = tracker.onJobFinished()
        assertNotNull(stopId)
        assertEquals(1, stopId)
        assertEquals(0, tracker.getActiveJobs())
    }
}
