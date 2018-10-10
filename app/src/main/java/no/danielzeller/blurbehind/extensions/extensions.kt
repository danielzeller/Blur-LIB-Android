package no.danielzeller.blurbehind.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator

inline fun ValueAnimator.onEnd(crossinline func: () -> Unit): ValueAnimator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationEnd(animation: Animator?) {
            super.onAnimationEnd(animation)
            func()
        }
    })
    return this
}

inline fun ValueAnimator.onUpdate(crossinline func: (value: Any) -> Unit): ValueAnimator {
    addUpdateListener { animation ->
        func(animation.animatedValue)
    }
    return this
}

inline fun ValueAnimator.delay(delay: Long): ValueAnimator {
    startDelay = delay
    return this
}

