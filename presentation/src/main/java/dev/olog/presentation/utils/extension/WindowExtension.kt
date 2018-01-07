package dev.olog.presentation.utils.extension

import android.graphics.Color
import android.view.View
import android.view.Window
import dev.olog.shared_android.isMarshmallow
import dev.olog.shared_android.isOreo

fun Window.setLightStatusBar(){
    decorView.systemUiVisibility = 0

    statusBarColor = Color.TRANSPARENT

    var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

    if (isMarshmallow()){
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
    }

    if (isOreo()){
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        navigationBarColor = Color.WHITE
    }
    decorView.systemUiVisibility = flags

}

fun Window.removeLightStatusBar(){
    decorView.systemUiVisibility = 0

    statusBarColor = Color.TRANSPARENT

    var flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)

    if (isOreo()){
        flags = flags or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        navigationBarColor = Color.WHITE
    }

    decorView.systemUiVisibility = flags
}