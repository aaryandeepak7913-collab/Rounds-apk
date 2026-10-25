package com.aaryan.rounds

import android.app.*
import android.content.*
import android.os.Build
import java.text.SimpleDateFormat
import java.util.*

/** Daily 9:00 PM reminder to log the workout. Skipped on days that already have an entry. */
object Reminder {
    const val HOUR = 21
    private fun pending(c: Context) = PendingIntent.getBroadcast(
        c, 1, Intent(c, ReminderReceiver::class.java), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    fun schedule(c: Context) {
        val at = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, HOUR); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }.timeInMillis
        val am = c.getSystemService(AlarmManager::class.java)
        if (Build.VERSION.SDK_INT < 31 || am.canScheduleExactAlarms()) am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(c))
        else am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pending(c)) // exact alarms not allowed: Android picks a time close to 9 PM
    }

    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
}

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        Reminder.schedule(c) // line up tomorrow's reminder first
        if (c.getSharedPreferences("r", 0).getString("logged", "") == Reminder.today()) return
        val nm = c.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("daily", "Daily reminder", NotificationManager.IMPORTANCE_DEFAULT))
        val open = PendingIntent.getActivity(c, 2, Intent(c, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        nm.notify(1, Notification.Builder(c, "daily").setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("Log today's workout").setContentText("Add today's rounds before you call it a day.")
            .setContentIntent(open).setAutoCancel(true).build())
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) { Reminder.schedule(c) }
}
