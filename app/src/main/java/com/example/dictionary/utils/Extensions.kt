package com.example.dictionary.utils

import android.view.View
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation

fun View.spring(property: DynamicAnimation.ViewProperty): SpringAnimation {
    val key = property.hashCode()
    var spring = getTag(key) as? SpringAnimation?

    if (spring == null) {
        spring = SpringAnimation(this, property)
        setTag(key, spring)
    }

    return spring
}