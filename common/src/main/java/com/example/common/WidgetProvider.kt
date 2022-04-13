package com.example.common

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.widget.RemoteViews
import android.widget.TextView

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

    fun update(context:Context?, txt:String) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val testWidge = ComponentName(context!!, WidgetProvider::class.java)
        val views = RemoteViews(context.packageName, R.layout.widget)
        views.setTextViewText(R.id.imageView, txt)
        appWidgetManager.updateAppWidget(testWidge, views);
    }
}