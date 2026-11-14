package com.keal.playarea
data class PlaySession(val id: Long, val name: String, val colorName: String, val colorHex: String, val pattern: String, val startedAt: Long, val endsAt: Long, val finishedAt: Long? = null)
