package dev.olog.msc.presentation.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.support.v4.content.ContextCompat
import android.support.v7.graphics.Palette
import android.view.View
import android.widget.RemoteViews
import dev.olog.msc.R
import dev.olog.msc.constants.FloatingInfoConstants
import dev.olog.msc.presentation.main.MainActivity
import dev.olog.msc.presentation.utils.images.ImageProcessorResult
import dev.olog.shared_android.Constants
import dev.olog.shared_android.ImageUtils
import dev.olog.shared_android.interfaces.MusicServiceClass
import org.jetbrains.anko.dip
import javax.inject.Inject

abstract class BaseWidget : AbsWidgetApp() {

    companion object {
        private var IS_PLAYING = false
        private const val PALETTE_SIZE = 120
    }

    @Inject lateinit var musicServiceClass: MusicServiceClass

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        val remoteViews = RemoteViews(context.packageName, layoutId)
        remoteViews.setImageViewBitmap(R.id.play, ImageUtils.getBitmapFromDrawable(
                ContextCompat.getDrawable(context, R.drawable.vd_play_big)!!))

        remoteViews.setOnClickPendingIntent(R.id.floatingWindow, buildFloatingInfoPendingIntent(context))
        remoteViews.setOnClickPendingIntent(R.id.previous, buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_PREVIOUS))
        remoteViews.setOnClickPendingIntent(R.id.play, buildPendingIntent(context, Constants.WIDGET_ACTION_PLAY_PAUSE))
        remoteViews.setOnClickPendingIntent(R.id.next, buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_NEXT))
        remoteViews.setOnClickPendingIntent(R.id.cover, buildContentIntent(context))

        initializeColors(context, remoteViews, appWidgetIds)

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews)
    }

    override fun onPlaybackStateChanged(context: Context, state: WidgetState, appWidgetIds: IntArray) {
        IS_PLAYING = state.isPlaying

        val remoteViews = RemoteViews(context.packageName, layoutId)

        val playPauseIcon = if (state.isPlaying){
            ContextCompat.getDrawable(context, R.drawable.vd_pause_big)!!
        } else ContextCompat.getDrawable(context, R.drawable.vd_play_big)!!

        remoteViews.setImageViewBitmap(R.id.play, ImageUtils.getBitmapFromDrawable(playPauseIcon))

        remoteViews.setOnClickPendingIntent(R.id.floatingWindow, buildFloatingInfoPendingIntent(context))
        remoteViews.setOnClickPendingIntent(R.id.previous, buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_PREVIOUS))
        remoteViews.setOnClickPendingIntent(R.id.play, buildPendingIntent(context, Constants.WIDGET_ACTION_PLAY_PAUSE))
        remoteViews.setOnClickPendingIntent(R.id.next, buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_NEXT))
        remoteViews.setOnClickPendingIntent(R.id.cover, buildContentIntent(context))

//        remoteViews.setChronometer(R.id.bookmark, SystemClock.elapsedRealtime() - state.bookmark,
//                null, state.isPlaying)

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews)
    }

    override fun onActionVisibilityChanged(context: Context, actions: WidgetActions, appWidgetIds: IntArray) {
        val (showPrevious, showNext) = actions

        val remoteViews = RemoteViews(context.packageName, layoutId)

        val previousVisibility = if (showPrevious) View.VISIBLE else View.INVISIBLE
        val nextVisibility = if (showNext) View.VISIBLE else View.INVISIBLE

        val previousPendingIntent = if (showPrevious) buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_PREVIOUS)
            else null
        val nextPendingIntent = if (showNext) buildPendingIntent(context, Constants.WIDGET_ACTION_SKIP_NEXT)
            else null

        remoteViews.setViewVisibility(R.id.previous, previousVisibility)
        remoteViews.setViewVisibility(R.id.next, nextVisibility)
        remoteViews.setOnClickPendingIntent(R.id.previous, previousPendingIntent)
        remoteViews.setOnClickPendingIntent(R.id.next, nextPendingIntent)

        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetIds, remoteViews)
    }

    private fun buildPendingIntent(context: Context, action: String): PendingIntent? {
        val intent = Intent(context, musicServiceClass.get())
        intent.action = action
        return PendingIntent.getService(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildFloatingInfoPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = FloatingInfoConstants.ACTION_START_SERVICE
        return PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    private fun buildContentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        intent.action = Constants.ACTION_CONTENT_VIEW
        return PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    protected fun generatePalette(context: Context, metadata: WidgetMetadata): Palette {
        val uri = Uri.parse(metadata.image)
        val bitmap = ImageUtils.getBitmapFromUriWithPlaceholder(context, uri, metadata.id, PALETTE_SIZE, PALETTE_SIZE)
        return Palette.from(bitmap).generate()
    }

    protected fun generatePalette(bitmap: Bitmap): Palette {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, PALETTE_SIZE, PALETTE_SIZE, false)
        return Palette.from(scaledBitmap).generate()
    }

    protected fun setMediaButtonColors(remoteViews: RemoteViews, color: Int){
        remoteViews.setInt(R.id.floatingWindow, "setColorFilter", color)
        remoteViews.setInt(R.id.favorite, "setColorFilter", color)
        remoteViews.setInt(R.id.previous, "setColorFilter", color)
        remoteViews.setInt(R.id.play, "setColorFilter", color)
        remoteViews.setInt(R.id.next, "setColorFilter", color)
    }

    protected fun updateTextColor(remoteViews: RemoteViews, palette: ImageProcessorResult){
        remoteViews.setTextColor(R.id.title, palette.primaryTextColor)
        remoteViews.setTextColor(R.id.subtitle, palette.secondaryTextColor)
//        remoteViews.setTextColor(R.id.bookmark, palette.secondaryTextColor)
//        remoteViews.setTextColor(R.id.duration, palette.secondaryTextColor)
    }

    protected abstract val layoutId : Int

    override fun onSizeChanged(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int, size: WidgetSize) {
        val remoteViews = RemoteViews(context.packageName, layoutId)

        if (size.minHeight > 100){
            remoteViews.setInt(R.id.title, "setMaxLines", Int.MAX_VALUE)
            remoteViews.setInt(R.id.subtitle, "setMaxLines", 2)
            remoteViews.setViewPadding(R.id.media_actions, 0, 0, 0, context.dip(8))
        } else {
            remoteViews.setInt(R.id.title, "setMaxLines", 1)
            remoteViews.setInt(R.id.subtitle, "setMaxLines", 1)
            remoteViews.setViewPadding(R.id.media_actions, 0, 0, context.dip(48), context.dip(8))
        }
        AppWidgetManager.getInstance(context).updateAppWidget(appWidgetId, remoteViews)
    }

    protected abstract fun initializeColors(context: Context, remoteViews: RemoteViews, appWidgetIds: IntArray)

}