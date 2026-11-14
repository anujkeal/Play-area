package com.keal.playarea

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "play_sessions")
data class PlaySession(
    @PrimaryKey val id: Long,
    val name: String,
    val colorName: String,
    val colorHex: String,
    val pattern: String,
    val startedAt: Long,
    val endsAt: Long,
    val finishedAt: Long? = null,
    @ColumnInfo(defaultValue = "'ENDED'") val status: String = SessionStatus.ACTIVE,
    @ColumnInfo(defaultValue = "0") val playedMillis: Long = 0L,
    @ColumnInfo(defaultValue = "0") val resumeCount: Int = 0,
    @ColumnInfo(defaultValue = "0") val lastStartedAt: Long = startedAt,
    @ColumnInfo(defaultValue = "NULL") val pendingDeleteAt: Long? = null
)

object SessionStatus {
    const val ACTIVE = "ACTIVE"
    const val ENDED = "ENDED"
    const val DELETED_PENDING = "DELETED_PENDING"
}

const val SESSION_DURATION_MILLIS = 30L * 60L * 1000L
