package cow.management.cowmanagementservice.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import cow.management.cowmanagementservice.database.converters.LocalDateConverter
import cow.management.cowmanagementservice.database.dao.ArtificialInseminationDao
import cow.management.cowmanagementservice.database.dao.BirthDao
import cow.management.cowmanagementservice.database.dao.CowDao
import cow.management.cowmanagementservice.model.ArtificialInsemination
import cow.management.cowmanagementservice.model.Birth
import cow.management.cowmanagementservice.model.Cow

@Database(
    entities = [Cow::class, Birth::class, ArtificialInsemination::class],
    version = 3
)
@TypeConverters(LocalDateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cowDao(): CowDao
    abstract fun birthDao(): BirthDao
    abstract fun artificialInseminationDao(): ArtificialInseminationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from 2 to 3: Add birthId to cows table
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE cows ADD COLUMN birthId INTEGER")
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cow_database"
                )
                .addMigrations(MIGRATION_2_3)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
