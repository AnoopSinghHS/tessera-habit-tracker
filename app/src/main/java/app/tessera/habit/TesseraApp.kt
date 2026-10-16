package app.tessera.habit

import android.app.Application
import app.tessera.habit.data.backup.BackupManager
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.repository.TesseraRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TesseraApp : Application() {

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database: TesseraDatabase by lazy {
        TesseraDatabase.getInstance(this)
    }

    val repository: TesseraRepository by lazy {
        TesseraRepository(this, database.habitDao())
    }

    val backupManager: BackupManager by lazy {
        BackupManager(this, repository)
    }

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            repository.ensureSeeded()
        }
    }
}
