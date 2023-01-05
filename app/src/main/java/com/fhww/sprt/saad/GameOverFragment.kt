package com.fhww.sprt.saad

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.navigation.fragment.findNavController
import com.fhww.sprt.saad.databinding.FragmentGameOverBinding

class GameOverFragment : Fragment(R.layout.fragment_game_over) {
    private lateinit var binding: FragmentGameOverBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentGameOverBinding.bind(view)

        with(binding) {
            againBtn.setOnClickListener {
                findNavController().popBackStack()
            }

            menuBtn.setOnClickListener {
                findNavController().popBackStack(R.id.menuFragment, false)
            }

        }
    }
}