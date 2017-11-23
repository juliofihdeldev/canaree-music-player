package dev.olog.presentation.widgets

import android.content.Context
import android.graphics.drawable.AnimatedVectorDrawable
import android.support.v7.widget.AppCompatImageButton
import android.util.AttributeSet
import dev.olog.presentation.R
import dev.olog.presentation.utils.getAnimatedVectorDrawable

class AnimatedPlayPauseImageView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0

) : AppCompatImageButton(context, attrs, defStyleAttr) {

    private val playAnimation: AnimatedVectorDrawable = context.getAnimatedVectorDrawable(R.drawable.avd_playpause_play_to_pause)
    private val pauseAnimation: AnimatedVectorDrawable = context.getAnimatedVectorDrawable(R.drawable.avd_playpause_pause_to_play)

    init {
        setupBackground(false)
    }

    fun setupBackground(play: Boolean) {
        val drawable = if (play) playAnimation else pauseAnimation
        setImageDrawable(drawable)
        drawable.jumpToCurrentState()
    }

    fun animationPlay(animate: Boolean) {
        setupAndAnimate(animate, playAnimation)
    }

    fun animationPause(animate: Boolean) {
        setupAndAnimate(animate, pauseAnimation)
    }

    private fun setupAndAnimate(animate: Boolean, avd: AnimatedVectorDrawable) {
        setImageDrawable(avd)
        if (animate)
            avd.start()
        else
            avd.jumpToCurrentState()
    }

}
