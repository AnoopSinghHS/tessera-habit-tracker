package app.tessera.habit

import android.app.Application
import app.tessera.habit.data.backup.BackupManager
import app.tessera.habit.data.local.TesseraDatabase
import app.tessera.habit.data.repository.TesseraRepository

class TesseraApp : Application() {

    val database: TesseraDatabase by lazy {
        TesseraDatabase.getInstance(this)
    }

    val repository: TesseraRepository by lazy {
        TesseraRepository(this, database.habitDao())
    }

    val backupManager: BackupManager by lazy {
        BackupManager(this, repository)
    }
}
