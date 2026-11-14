package com.keal.playarea
import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
object SessionStore {
 private const val PREF="play_area"; private const val KEY="sessions"
 @Synchronized fun all(c:Context):List<PlaySession> = try {
  val a=JSONArray(c.getSharedPreferences(PREF,0).getString(KEY,"[]"))
  (0 until a.length()).map { i -> val o=a.getJSONObject(i); PlaySession(o.getLong("id"),o.getString("name"),o.getString("colorName"),o.getString("colorHex"),o.optString("pattern","Plain"),o.getLong("startedAt"),o.getLong("endsAt"),if(o.isNull("finishedAt")) null else o.getLong("finishedAt")) }
 } catch(_:Exception){ emptyList() }
 @Synchronized fun save(c:Context,list:List<PlaySession>){ val a=JSONArray(); list.forEach { s -> a.put(JSONObject().apply { put("id",s.id);put("name",s.name);put("colorName",s.colorName);put("colorHex",s.colorHex);put("pattern",s.pattern);put("startedAt",s.startedAt);put("endsAt",s.endsAt);put("finishedAt",s.finishedAt?:JSONObject.NULL) }) }; c.getSharedPreferences(PREF,0).edit().putString(KEY,a.toString()).commit() }
 fun add(c:Context,s:PlaySession)=save(c,all(c)+s)
 fun finish(c:Context,id:Long){save(c,all(c).map { if(it.id==id && it.finishedAt==null) it.copy(finishedAt=System.currentTimeMillis()) else it })}
}
