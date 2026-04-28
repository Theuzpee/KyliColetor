package br.com.grupokyly.apscoletor.test.fake

import android.content.Context
import br.com.grupokyly.apscoletor.data.sync.SyncScheduler

// We create a mocked version of SyncScheduler that doesn't actually use WorkManager.
// Since SyncScheduler uses context internally for WorkManager.getInstance(), 
// we override its methods in a fake to intercept the calls.
open class FakeSyncScheduler(context: Context) : SyncScheduler(context) {
    var scheduleCount = 0
    
    override fun scheduleSync() {
        scheduleCount++
    }
    
    override fun schedulePeriodic() {
        // no-op
    }
    
    override fun cancelAll() {
        // no-op
    }
}
