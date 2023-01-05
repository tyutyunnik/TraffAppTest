package com.fhww.sprt.saad

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.navigation.fragment.findNavController
import com.fhww.sprt.saad.databinding.FragmentMenuBinding

class MenuFragment : Fragment(R.layout.fragment_menu) {
    private lateinit var binding: FragmentMenuBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().window.addFlags(ConstraintLayout.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        binding = FragmentMenuBinding.bind(view)
        activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        with(binding) {
            beginBtn.setOnClickListener {
                findNavController().navigate(R.id.action_menuFragment_to_gameFragment)
            }

            endBtn.setOnClickListener {
                end()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(
            requireActivity(), object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    end()
                }
            }
        )
    }

    private fun end() {
        AlertDialog.Builder(requireContext())
            .setTitle("Quit?")
            .setMessage("Sure?")
            .setPositiveButton("Yes") { _, _ ->
                requireActivity().finish()
            }
            .setNegativeButton("No", null)
            .create()
            .show()
    }
}