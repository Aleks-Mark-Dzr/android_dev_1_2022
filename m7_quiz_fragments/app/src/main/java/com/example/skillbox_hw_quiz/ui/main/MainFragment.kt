package com.example.skillbox_hw_quiz.ui.main

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.example.skillbox_hw_quiz.R
import com.example.skillbox_hw_quiz.databinding.MainFragmentBinding
import com.example.skillbox_hw_quiz.quiz.Question
import com.example.skillbox_hw_quiz.quiz.QuizStorage

class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val quiz = QuizStorage.getQuiz(QuizStorage.Locale.Ru) // Или QuizStorage.Locale.Ru в зависимости от локализации
        addQuestions(quiz.questions)
        binding.backButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_welcomeFragment)
        }
        binding.submitButton.setOnClickListener {
            // Сбор выбранных ответов
            val selectedAnswers = mutableListOf<String>()
            for (i in 0 until binding.quizContainer.childCount) {
                val view = binding.quizContainer.getChildAt(i)
                if (view is RadioGroup) {
                    val radioButtonId = view.checkedRadioButtonId
                    val radioButton = view.findViewById<RadioButton>(radioButtonId)
                    selectedAnswers.add(radioButton.text.toString())
                }
            }

//            // Подсчет результатов с помощью QuizStorage.answer() или другой логики
//            val result = QuizStorage.answer(selectedAnswers)
//            // Передача результатов на экран "Результаты"
//            val action = MainFragmentDirections.actionMainFragmentToResultFragment(result)
//            findNavController().navigate(action)
            findNavController().navigate(R.id.action_mainFragment_to_resultFragment)
        }
    }

    private fun addQuestions(questions: List<Question>) {
        questions.forEach { question ->
            val questionTextView = TextView(context).apply {
                text = question.question
                layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
                setPadding(8, 16, 8, 16)
            }
            binding.quizContainer.addView(questionTextView)

            val answersRadioGroup = RadioGroup(context).also { group ->
                question.answers.forEachIndexed { index, answer ->
                    val radioButton = RadioButton(context).apply {
                        text = answer
                        id = View.generateViewId()
                    }
                    group.addView(radioButton)
                }
            }
            binding.quizContainer.addView(answersRadioGroup)
        }
    }
}