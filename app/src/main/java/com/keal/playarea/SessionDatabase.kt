package com.keal.playarea

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Dao
interface SessionDao {
    @Query("SELECT * FROM play_sessions ORDER BY startedAt DESC")
    suspend fun getAll(): List<PlaySession>

    @Query("SELECT * FROM play_sessions WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): PlaySession?

    @Query("SELECT * FROM play_sessions WHERE LOWER(name) = LOWER(:name) AND status = 'ACTIVE' LIMIT 1")
    suspend fun findActiveByName(name: String): PlaySession?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: PlaySession)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<PlaySession>)

    @Update
    suspend fun update(session: PlaySession)

    @Query("DELETE FROM play_sessions WHERE id = :id")
    suspend fun hardDelete(id: Long)

    @Query("SELECT * FROM play_sessions WHERE status = 'DELETED_PENDING' ORDER BY pendingDeleteAt ASC")
    suspend fun getPendingDeletes(): List<PlaySession>
}

/*
 * Version 1 is the initial Room schema. Version 2 adds lifecycle and resume
 * metadata. The previously shipped app used SharedPreferences, so SessionStore
 * also imports that legacy JSON data once on first launch.
 */
private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE play_sessions ADD COLUMN status TEXT NOT NULL DEFAULT 'ENDED'")
        database.execSQL("ALTER TABLE play_sessions ADD COLUMN playedMillis INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE play_sessions ADD COLUMN resumeCount INTEGER NOT NULL DEFAULT 0")
        database.execSQL("ALTER TABLE play_sessions ADD COLUMN lastStartedAt INTEGER NOT NULL DEFAULT 0")
        database.execSQL(
            "UPDATE play_sessions SET status = CASE WHEN finishedAt IS NULL THEN 'ACTIVE' ELSE 'ENDED' END, " +
                "lastStartedAt = startedAt, " +
                "playedMillis = CASE WHEN finishedAt IS NULL THEN 0 ELSE MIN(1800000, MAX(0, finishedAt - startedAt)) END"
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE play_sessions ADD COLUMN pendingDeleteAt INTEGER DEFAULT NULL")
    }
}

@Database(entities = [PlaySession::class], version = 3, exportSchema = false)
abstract class SessionDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao

    companion object {
        @Volatile private var instance: SessionDatabase? = null

        fun get(context: Context): SessionDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                SessionDatabase::class.java,
                "play_area_sessions.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
        }
    }
}
