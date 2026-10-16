package app.tessera.habit.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [HabitEntity::class, HabitLogEntity::class],
    version = 1,
    exportSchema = false
)
abstract class TesseraDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        private const val DATABASE_NAME = "tessera_habits.db"

        @Volatile
        private var INSTANCE: TesseraDatabase? = null

        fun getInstance(context: Context): TesseraDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    TesseraDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
