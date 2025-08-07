package dev.anonymous.hurriya.admin.presentation.utils

object PresenceFormatter {
    fun getLastSeenText(lastSeen: Long?, isOnline: Boolean?): String {
        if (lastSeen == null || isOnline == null) return "آخر ظهور كان قريباً"
        if (isOnline) return "متصل الآن"

        val now = System.currentTimeMillis()
        val diffMillis = now - lastSeen

        val minutes = diffMillis / (60 * 1000)
        val hours = diffMillis / (60 * 60 * 1000)
        val days = diffMillis / (24 * 60 * 60 * 1000)

        return when {
            minutes <= 1L -> "آخر ظهور قبل دقيقة"
            minutes in 2..59 -> "آخر ظهور قبل $minutes دقائق"
            hours == 1L -> "آخر ظهور قبل ساعة"
            hours in 2..23 -> "آخر ظهور قبل $hours ساعات"
            days == 1L -> "آخر ظهور منذ يوم"
            days == 2L -> "آخر ظهور منذ يومين"
            days in 3..6 -> "آخر ظهور منذ $days أيام"
            days in 7..29 -> {
                val weeks = days / 7
                if (weeks == 1L) "آخر ظهور منذ أسبوع" else "آخر ظهور منذ $weeks أسابيع"
            }

            days in 30..364 -> {
                val months = days / 30
                if (months == 1L) "آخر ظهور منذ شهر" else "آخر ظهور منذ $months أشهر"
            }

            else -> {
                val years = days / 365
                if (years == 1L) "آخر ظهور منذ سنة" else "آخر ظهور منذ $years سنوات"
            }
        }
    }
}
