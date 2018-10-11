package no.danielzeller.blurbehind.extensions

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
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

inline fun ValueAnimator.onStart(crossinline func: () -> Unit): ValueAnimator {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator?) {
            super.onAnimationStart(animation)
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

inline fun ValueAnimator.start(runnigAnims: ArrayList<ValueAnimator>) {
    runnigAnims.add(this)
    start()
}

inline fun ValueAnimator.interpolate(interp: TimeInterpolator): ValueAnimator {
    interpolator = interp
    return this
}
