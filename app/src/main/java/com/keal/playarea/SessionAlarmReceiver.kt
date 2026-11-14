package com.keal.playarea
import android.app.*
import android.content.*
import android.media.AudioAttributes
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
class SessionAlarmReceiver:BroadcastReceiver(){
 override fun onReceive(context:Context,intent:Intent){
  val id=intent.getLongExtra(EXTRA_ID,-1L); val s=SessionStore.all(context).firstOrNull{it.id==id}?:return
  if(s.finishedAt!=null)return
  ensureChannel(context)
  val n=NotificationCompat.Builder(context,CHANNEL).setSmallIcon(R.drawable.ic_launcher).setContentTitle("Play time is up").setContentText("${s.name}'s 30-minute play session has ended.").setPriority(NotificationCompat.PRIORITY_MAX).setCategory(NotificationCompat.CATEGORY_ALARM).setAutoCancel(true).build()
  try{NotificationManagerCompat.from(context).notify(id.hashCode(),n)}catch(_:SecurityException){}
 }
 companion object{
  const val CHANNEL="play_session_alerts";const val EXTRA_ID="session_id"
  fun ensureChannel(c:Context){if(Build.VERSION.SDK_INT>=26){val ch=NotificationChannel(CHANNEL,c.getString(R.string.notification_channel_name),NotificationManager.IMPORTANCE_HIGH).apply{description=c.getString(R.string.notification_channel_description);enableVibration(true);vibrationPattern= longArrayOf(0,450,180,450,180,700);setSound(android.provider.Settings.System.DEFAULT_NOTIFICATION_URI,AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_NOTIFICATION).build())};c.getSystemService(NotificationManager::class.java).createNotificationChannel(ch)}}
  fun schedule(c:Context,s:PlaySession){val am=c.getSystemService(Context.ALARM_SERVICE) as AlarmManager;val pi=PendingIntent.getBroadcast(c,s.id.hashCode(),Intent(c,SessionAlarmReceiver::class.java).putExtra(EXTRA_ID,s.id),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);if(Build.VERSION.SDK_INT>=31 && am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,s.endsAt,pi) else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,s.endsAt,pi)}
  fun cancel(c:Context,id:Long){val pi=PendingIntent.getBroadcast(c,id.hashCode(),Intent(c,SessionAlarmReceiver::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);(c.getSystemService(Context.ALARM_SERVICE) as AlarmManager).cancel(pi)}
 }
}
