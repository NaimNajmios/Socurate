package com.najmi.oreamnos.utils

/**
 * Tracks concurrent service jobs and determines when it is safe to stop the service.
 *
 * This class ensures that the service is only stopped when all active jobs are finished,
 * preventing race conditions where a newer request's job is killed by an older request's completion.
 */
class ServiceConcurrencyTracker {
    private val lock = Any()
    private var activeJobs = 0
    private var lastStartId = 0

    /**
     * Called when the service receives a new start command.
     *
     * @param startId The startId provided by the system.
     */
    fun onServiceStarted(startId: Int) {
        synchronized(lock) {
            activeJobs++
            lastStartId = startId
        }
    }

    /**
     * Called when a job finishes.
     *
     * @return The startId to stop the service with if all jobs are finished, or null if the service should continue running.
     */
    fun onJobFinished(): Int? {
        synchronized(lock) {
            activeJobs--
            if (activeJobs <= 0) {
                activeJobs = 0 // Safety clamp
                return lastStartId
            }
            return null
        }
    }

    /**
     * For testing purposes only.
     */
    fun getActiveJobs(): Int {
        synchronized(lock) {
            return activeJobs
        }
    }
}
