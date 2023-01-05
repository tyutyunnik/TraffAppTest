package com.fhww.sprt.saad

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.fhww.sprt.saad.databinding.FragmentGameBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class GameFragment : Fragment(R.layout.fragment_game) {
    private lateinit var binding: FragmentGameBinding

    companion object {
        var running = true
    }

    private var width = 0f
    private var height = 0f
    private lateinit var dinoRect: Rectangle

    private var time = 0
    private var fireballs = listOf<Fireball>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGameBinding.bind(view)

        width = resources.displayMetrics.widthPixels.toFloat()
        height = resources.displayMetrics.heightPixels.toFloat()

        dinoRect = Rectangle(
            width * 0.4f,
            height * 0.7f,
            width * 0.15f,
            height * 0.25f
        )

        with(binding) {
            dino.apply {
                layoutParams = ConstraintLayout.LayoutParams(
                    dinoRect.width.toInt(),
                    dinoRect.height.toInt()
                )
                x = dinoRect.x
                y = dinoRect.y
                scaleType = ImageView.ScaleType.FIT_XY
                setOnTouchListener { _, motionEvent ->
                    when (motionEvent.action) {
                        MotionEvent.ACTION_MOVE -> {
                            if (motionEvent.rawX > dinoRect.width / 2 && motionEvent.rawX < this@GameFragment.width - dinoRect.width / 5) {
                                dinoRect.x =
                                    (motionEvent.rawX - dinoRect.width / 1.5).toFloat()
                                this.x = dinoRect.x
                            }
                        }
                    }
                    true
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        running = true

        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            while (running) {
                delay(16)
                time++
                if (time > 60) {
                    time = 0
                    addFireball()
                }
                fireballs.forEach { fireball ->
                    if (fireball.r.y > height) {
                        removeFireball(fireball)
                    }
                    if (fireball.r.overlaps(dinoRect)) {
                        removeFireball(fireball)
                        gameOver()
                    }
                }
            }
//            fireballs.forEach { fireball ->
//                requireActivity().runOnUiThread { fireball.move() }
//                if (fireball.r.y > height) {
//                    removeFireball(fireball)
//                }
//                if (dinoRect.overlaps(fireball.r)) {
//                    removeFireball(fireball)
//                    requireActivity().runOnUiThread {
//                        gameOver()
//                    }
//                }
//            }

        }
    }

//    override fun onResume() {
//        super.onResume()
//        fireballs.forEach { fireball ->
//            removeFireball(fireball)
//        }
//    }

    override fun onStop() {
        super.onStop()
        running = false
    }

    private suspend fun gameOver() = withContext(Dispatchers.Main) {
        running = false
        findNavController().navigate(R.id.action_gameFragment_to_gameOverFragment)
    }

    private suspend fun removeFireball(fireball: Fireball) = withContext(Dispatchers.Main) {
        binding.root.removeView(fireball.fireball)
        val fireballsMutable = fireballs.toMutableList()
        fireballsMutable.remove(fireball)
        fireballs = fireballsMutable.toList()
    }

    private suspend fun addFireball() = withContext(Dispatchers.Main) {
        val fireball = Fireball(
            Rectangle(
                Random.nextInt(0, (width * 0.75f).toInt()).toFloat(),
                -height * 0.1f,
                width * 0.07f,
                height * 0.2f
            ),
            binding.root,
            viewLifecycleOwner.lifecycleScope
        )
        val fireballsMutable = fireballs.toMutableList()
        fireballsMutable.add(fireball)
        fireballs = fireballsMutable.toList()
    }
}