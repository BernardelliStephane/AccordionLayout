package fr.steph.accordionlayout

import android.view.View
import android.view.animation.Animation
import android.view.animation.Transformation
import android.widget.LinearLayout
import androidx.core.view.isVisible

/**
 * Created by riyagayasen on 16/10/16.
 * Updated by Stéphane Bernardelli on 12/27/22
 */

class AccordionTransitionAnimation(view: View, duration: Int, type: Int) : Animation() {
    private val view: View
    private var endHeight: Int
    var endBottomMargin: Int
    var endTopMargin: Int
    private val type: Int
    private val layoutParams: LinearLayout.LayoutParams

    init {
        setDuration(duration.toLong())
        this.view = view
        endHeight = this.view.measuredHeight
        layoutParams = view.layoutParams as LinearLayout.LayoutParams
        endBottomMargin = layoutParams.bottomMargin
        endTopMargin = layoutParams.topMargin
        this.type = type
        if (this.type == EXPAND) {
            layoutParams.height = 0
            layoutParams.topMargin = 0
            layoutParams.bottomMargin = 0
        }
        else layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
        view.isVisible = true
    }

    var height: Int
        get() = view.height
        set(height) { endHeight = height }

    override fun applyTransformation(interpolatedTime: Float, t: Transformation) {
        super.applyTransformation(interpolatedTime, t)
        if (interpolatedTime < 1.0f) {
            if (type == EXPAND) {
                layoutParams.height = (endHeight * interpolatedTime).toInt()
                layoutParams.topMargin = (endTopMargin * interpolatedTime).toInt()
                layoutParams.bottomMargin = (endBottomMargin * interpolatedTime).toInt()
                view.invalidate()
            }
            else {
                layoutParams.height = (endHeight * (1 - interpolatedTime)).toInt()
                layoutParams.topMargin = (endTopMargin * (1 - interpolatedTime)).toInt()
                layoutParams.bottomMargin = (endBottomMargin * (1 - interpolatedTime)).toInt()
            }
            view.requestLayout()
        }
        else {
            if (type == EXPAND) {
                layoutParams.height = LinearLayout.LayoutParams.WRAP_CONTENT
                view.requestLayout()
            }
            else view.isVisible = false
        }
    }

    companion object {
        const val COLLAPSE = 1
        const val EXPAND = 0
    }
}