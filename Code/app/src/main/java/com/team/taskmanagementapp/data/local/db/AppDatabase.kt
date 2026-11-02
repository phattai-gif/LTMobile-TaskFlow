package com.team.taskmanagementapp.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.team.taskmanagementapp.data.local.dao.PomodoroDao
import com.team.taskmanagementapp.data.local.dao.TaskDao
import com.team.taskmanagementapp.data.local.entity.PomodoroSession
import com.team.taskmanagementapp.data.local.entity.Task
import com.team.taskmanagementapp.util.Constants

@Database(
    entities = [Task::class, PomodoroSession::class],
    version = Constants.DATABASE_VERSION,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): TaskDao

    abstract fun pomodoroDao(): PomodoroDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    Constants.DATABASE_NAME
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()

                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "ALTER TABLE tasks ADD COLUMN reminderMinutes INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        internal val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.addColumnIfMissing("tasks", "repeatEndDate", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "repeatLimitCount", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "currentOccurrence", "INTEGER NOT NULL DEFAULT 1")
                db.addColumnIfMissing("tasks", "isPaused", "INTEGER NOT NULL DEFAULT 0")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `pomodoro_sessions` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`taskId` INTEGER NOT NULL, " +
                            "`startTime` INTEGER NOT NULL, " +
                            "`endTime` INTEGER NOT NULL, " +
                            "`durationInMinutes` INTEGER NOT NULL, " +
                            "`sessionType` TEXT NOT NULL, " +
                            "`isCompleted` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) " +
                            "ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pomodoro_sessions_taskId` " +
                            "ON `pomodoro_sessions` (`taskId`)"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_pomodoro_sessions_startTime` " +
                            "ON `pomodoro_sessions` (`startTime`)"
                )

                db.addColumnIfMissing("tasks", "estimatedPomodoros", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "completedPomodoros", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "totalFocusTimeMinutes", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "completedAt", "INTEGER")

                db.addColumnIfMissing("tasks", "repeatEndDate", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "repeatLimitCount", "INTEGER NOT NULL DEFAULT 0")
                db.addColumnIfMissing("tasks", "currentOccurrence", "INTEGER NOT NULL DEFAULT 1")
                db.addColumnIfMissing("tasks", "isPaused", "INTEGER NOT NULL DEFAULT 0")
            }
        }

        private fun SupportSQLiteDatabase.addColumnIfMissing(
            table: String,
            column: String,
            definition: String
        ) {
            val alreadyExists = query("PRAGMA table_info(`$table`)").use { cursor ->
                val nameIndex = cursor.getColumnIndex("name")
                var found = false
                while (cursor.moveToNext()) {
                    if (nameIndex >= 0 && cursor.getString(nameIndex) == column) {
                        found = true
                        break
                    }
                }
                found
            }

            if (!alreadyExists) {
                execSQL("ALTER TABLE `$table` ADD COLUMN `$column` $definition")
            }
        }
    }
}