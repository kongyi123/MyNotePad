package com.example.common

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import com.example.common.data.Schedule
import java.util.*
import kotlin.properties.Delegates

class WidgetProvider : AppWidgetProvider() {
    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
//        appWidgetIds?.forEach {
//            val views: RemoteViews = addViews(context)
//            appWidgetManager?.updateAppWidget(it, views)
//        }
    }

//    private fun addViews(context: Context?): RemoteViews {
//        val views = RemoteViews(context?.packageName, R.layout.widget)
//        // make bitmap file to background of view.
//        val bitmapFromFile = BitmapFactory.decodeResource(context?.resources, R.drawable.aaa)
//        val bd = BitmapDrawable(context?.resources, bitmapFromFile)
//        views.setImageViewBitmap(R.id.imageView, bd.bitmap)
//        return views
//    }

//    fun update(context:Context?, bm: Bitmap) {
//        val appWidgetManager = AppWidgetManager.getInstance(context)
//        val testWidge = ComponentName(context!!, WidgetProvider::class.java)
//        val views = RemoteViews(context.packageName, R.layout.widget)
//        views.setImageViewBitmap(R.id.imageView, bm)
//        appWidgetManager.updateAppWidget(testWidge, views);
//    }

    object ViewSetter {
        private val dayString = Array<String>(8) { "" }

        init {
            dayString[1]= "일"
            dayString[2]= "월"
            dayString[3]= "화"
            dayString[4]= "수"
            dayString[5]= "목"
            dayString[6]= "금"
            dayString[7]= "토"
        }
        var i = 0
        var j = 0
        var str = ""

        fun init() {
            i = 0
            j = 0
            str = ""
        }

        fun setTextViewDayTextView(views: RemoteViews, list: ArrayList<Schedule>, id:Int) {
            if (list.size == 0) {
                str = ""
            } else {
                if (i < list.size) {
                    var currentDayCal = Utils.getDateFromStringToCal(list[i].date)
                    if (currentDayCal!!.calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY && (
                                id == R.id.today_sunday ||
                                        id == R.id.week1_sunday ||
                                        id == R.id.week2_sunday)
                    ) {
                        j = i
                        str = ""
                        while (i < list.size && list[i].date == list[j].date) {
                            var currentDayCal = Utils.getDateFromStringToCal(list[i].date)
                            str += Utils.convDBdateToShown(list[i].date) + "(${
                                dayString[currentDayCal!!.calendar.get(
                                    Calendar.DAY_OF_WEEK
                                )]
                            }) " + list[i].title + " " + list[i++].content
                            if (!(i < list.size && list[i].date == list[j].date)) {
                                break
                            }
                            str += "\n"
                        }
                    } else if (currentDayCal!!.calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY && (
                                id == R.id.today_saturday ||
                                        id == R.id.week1_saturday ||
                                        id == R.id.week2_saturday)
                    ) {
                        j = i
                        str = ""
                        while (i < list.size && list[i].date == list[j].date) {
                            var currentDayCal = Utils.getDateFromStringToCal(list[i].date)
                            str += Utils.convDBdateToShown(list[i].date) + "(${
                                dayString[currentDayCal!!.calendar.get(
                                    Calendar.DAY_OF_WEEK
                                )]
                            }) " + list[i].title + " " + list[i++].content
                            if (!(i < list.size && list[i].date == list[j].date)) {
                                break
                            }
                            str += "\n"
                        }
                    } else {
                        str = ""
                        val cal = Calendar.getInstance()
                        cal.timeInMillis = System.currentTimeMillis()

                        if (i < list.size) {
                            if (currentDayCal != null) {
                                while (i < list.size &&
                                    currentDayCal!!.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                                    currentDayCal.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY
                                ) {
                                    str += Utils.convDBdateToShown(list[i].date) + "(${
                                        dayString[currentDayCal.calendar.get(
                                            Calendar.DAY_OF_WEEK
                                        )]
                                    }) " + list[i].title + " " + list[i++].content
                                    if (i < list.size) {
                                        currentDayCal = Utils.getDateFromStringToCal(list[i].date)
                                        if (!(i < list.size && currentDayCal != null &&
                                                    currentDayCal.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY &&
                                                    currentDayCal.calendar.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
                                        )
                                            break
                                        str += "\n"
                                    } else break
                                }
                            }
                        }
                        //     Log.i("kongyi1220BB", "day = ${day}, currentDay = $currentDay")

                    }
                }
            }
            views.setTextViewText(id, str)
            if (str == "") {
                views.setViewVisibility(id, View.GONE)
            } else {
                views.setViewVisibility(id, View.VISIBLE)
            }
        }
    }

    fun update(context: Context?, list: ArrayList<Schedule>, intent: Intent) {
        val cal = Calendar.getInstance()
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val testWidget = ComponentName(context!!, WidgetProvider::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget)

        cal.timeInMillis = System.currentTimeMillis()
        val day = cal.get(Calendar.DAY_OF_WEEK)

        Log.i("kongyi1220BB", "day - ${day}")
        clearWidget(views)
        ViewSetter.init()

        when (day) {
            Calendar.SATURDAY -> {
                Log.i("kongyi1220BB", "SATURDAY")
                ViewSetter.setTextViewDayTextView(views, list, R.id.today_saturday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_sunday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_weekdays)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_saturday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_sunday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_weekdays)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_saturday)
            }
            Calendar.SUNDAY -> {
                Log.i("kongyi1220BB", "SUNDAY")
                ViewSetter.setTextViewDayTextView(views, list, R.id.today_sunday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_weekdays)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_saturday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_sunday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_weekdays)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_saturday)
            }
            else -> {
                Log.i("kongyi1220BB", "weekday")
                ViewSetter.setTextViewDayTextView(views, list, R.id.today_weekday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week1_saturday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_sunday)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_weekdays)
                ViewSetter.setTextViewDayTextView(views, list, R.id.week2_saturday)
            }
        }

        views.setOnClickPendingIntent(R.id.clickable_view,
            PendingIntent.getActivity(context, 0, intent, 0))
        appWidgetManager.updateAppWidget(testWidget, views);
    }

    private fun clearWidget(views: RemoteViews) {
        val blank = ArrayList<Schedule>()
        ViewSetter.setTextViewDayTextView(views, blank, R.id.today_sunday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.today_weekday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.today_saturday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week1_sunday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week1_weekdays)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week1_saturday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week2_sunday)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week2_weekdays)
        ViewSetter.setTextViewDayTextView(views, blank, R.id.week2_saturday)
    }
}