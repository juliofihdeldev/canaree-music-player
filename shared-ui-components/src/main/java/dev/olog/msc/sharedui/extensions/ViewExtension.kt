@file:Suppress("NOTHING_TO_INLINE")

package dev.olog.msc.sharedui.extensions

import android.view.View
import android.view.ViewGroup
import androidx.core.view.forEach

fun View.toggleVisibility(visible: Boolean, gone: Boolean){
    if (visible){
        this.visibility = View.VISIBLE
    } else {
        if (gone){
            this.visibility = View.GONE
        } else {
            this.visibility = View.INVISIBLE
        }
    }
}

inline fun View.setGone(){
    this.visibility = View.GONE
}

inline fun View.setVisible(){
    this.visibility = View.VISIBLE
}

inline fun View.setInvisible(){
    this.visibility = View.INVISIBLE
}

fun View.setPaddingTop(padding: Int) {
    setPadding(paddingLeft, padding, paddingRight, paddingBottom)
}

inline fun View.setPaddingBottom(padding: Int) {
    setPadding(paddingLeft, paddingTop, paddingRight, padding)
}

inline fun View.toggleSelected(){
    this.isSelected = !this.isSelected
}

fun View.windowBackground(): Int {
    return context.themeAttributeToColor(android.R.attr.windowBackground)
}

inline fun ViewGroup.forEachRecursively(action: (view: View) -> Unit){
    forEach {
        if (it is ViewGroup){
            it.forEach(action)
        } else {
            action(it)
        }
    }
}