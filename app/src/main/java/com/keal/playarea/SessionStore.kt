package com.keal.playarea

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

object SessionStore {
    private const val LEGACY_PREFS = "play_area"
    private const val LEGACY_KEY = "sessions"
    private const val MIGRATION_PREFS = "play_area_room_import"
    private const val MIGRATION_KEY = "legacy_imported"

    suspend fun initialize(context: Context, clearPendingDeletes: Boolean = true) = withContext(Dispatchers.IO) {
        val dao = SessionDatabase.get(context).sessionDao()
        val marker = context.getSharedPreferences(MIGRATION_PREFS, Context.MODE_PRIVATE)
        if (!marker.getBoolean(MIGRATION_KEY, false)) {
            val raw = context.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                .getString(LEGACY_KEY, "[]") ?: "[]"
            val legacy = runCatching {
                val array = JSONArray(raw)
                (0 until array.length()).mapNotNull { index ->
                    val item = array.optJSONObject(index) ?: return@mapNotNull null
                    val started = item.optLong("startedAt")
                    val finished = if (item.isNull("finishedAt")) null else item.optLong("finishedAt")
                    val isActive = finished == null
                    val played = if (isActive) 0L else
                        ((finished ?: started) - started).coerceIn(0L, SESSION_DURATION_MILLIS)
                    PlaySession(
                        id = item.optLong("id", started),
                        name = item.optString("name", item.optString("childName", "Guest")),
                        colorName = item.optString("colorName", "Unknown"),
                        colorHex = item.optString("colorHex", "#D6A84F"),
                        pattern = item.optString("pattern", "Plain"),
                        startedAt = started,
                        endsAt = item.optLong("endsAt", started + SESSION_DURATION_MILLIS),
                        finishedAt = finished,
                        status = if (isActive) SessionStatus.ACTIVE else SessionStatus.ENDED,
                        playedMillis = played,
                        resumeCount = 0,
                        lastStartedAt = started
                    )
                }
            }.getOrDefault(emptyList())
            if (legacy.isNotEmpty()) dao.insertAll(legacy)
            marker.edit().putBoolean(MIGRATION_KEY, true).commit()
        }
        if (clearPendingDeletes) dao.clearPendingDeletes()
    }

    suspend fun all(context: Context): List<PlaySession> = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().getAll()
    }

    suspend fun getById(context: Context, id: Long): PlaySession? = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().getById(id)
    }

    suspend fun activeByName(context: Context, name: String): PlaySession? = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().findActiveByName(name.trim())
    }

    suspend fun insert(context: Context, session: PlaySession) = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().insert(session)
    }

    suspend fun update(context: Context, session: PlaySession) = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().update(session)
    }

    suspend fun hardDelete(context: Context, id: Long) = withContext(Dispatchers.IO) {
        SessionDatabase.get(context).sessionDao().hardDelete(id)
    }

    fun newId(): Long = System.currentTimeMillis() * 1_000L + (System.nanoTime() % 1_000L)
}
