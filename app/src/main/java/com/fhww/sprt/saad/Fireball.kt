package com.fhww.sprt.saad

import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class Fireball(
    val r: Rectangle,
    parent: ConstraintLayout,
    lifecycleScope: LifecycleCoroutineScope
) {
    val fireball = ImageView(parent.context)

    init {
        fireball.apply {
            layoutParams = ConstraintLayout.LayoutParams(r.width.toInt(), r.height.toInt())
            x = r.x
            y = r.y
            setImageResource(R.drawable.fireball)
            scaleType = ImageView.ScaleType.FIT_XY
        }
        parent.addView(fireball)

        lifecycleScope.launch {
            startThread()
        }
    }

    private suspend fun startThread(){
        withContext(Dispatchers.IO) {
            while (GameFragment.running) {
                delay(16)
                r.y += 5
                update()
            }
        }
    }

    private suspend fun update(){
        withContext(Dispatchers.Main) {
            fireball.y = r.y
        }
    }

//    fun move() {
//        r.y += 5
//        fireball.y = r.y
//    }
}