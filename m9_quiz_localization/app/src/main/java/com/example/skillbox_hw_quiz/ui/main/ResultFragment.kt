package com.example.skillbox_hw_quiz.ui.main

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.example.skillbox_hw_quiz.R
import com.example.skillbox_hw_quiz.databinding.FragmentResultBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_QUIZ_RESULT = "quizResult"

/**
 * A simple [Fragment] subclass.
 * Use the [ResultFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class ResultFragment : androidx.fragment.app.Fragment() {

    private var _binding: FragmentResultBinding? = null
    private val binding get() = _binding!!
    private var resultQuiz: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentResultBinding.inflate(inflater, container, false)
        arguments?.let {
            resultQuiz = it.getString(ARG_QUIZ_RESULT)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.resultTextView.text = resultQuiz

        // Добавляем анимацию для resultTextView
        ObjectAnimator.ofFloat(binding.resultTextView, "alpha", 0f, 1f).apply {
            duration = 2000
        }.start()

        // Добавляем анимацию для startAgainButton
        binding.startAgainButton.translationY = 100f // начальное положение
        ObjectAnimator.ofFloat(binding.startAgainButton, "translationY", 100f, 0f).apply {
            duration = 2000
        }.start()

        binding.startAgainButton.setOnClickListener{
            findNavController().navigate(R.id.action_resultFragment_to_mainFragment)
        }
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Выполняем навигацию обратно к WelcomeFragment
                findNavController().navigate(R.id.action_resultFragment_to_welcomeFragment)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ResultFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_QUIZ_RESULT, param1)
                }
            }
    }
}