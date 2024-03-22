package com.example.skillbox_hw_quiz.ui.main

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
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.skillbox_hw_quiz.R
import com.example.skillbox_hw_quiz.databinding.MainFragmentBinding
import com.example.skillbox_hw_quiz.quiz.Question
import com.example.skillbox_hw_quiz.quiz.QuizStorage
import android.animation.ObjectAnimator
import kotlinx.coroutines.delay
import java.util.Locale

class MainFragment : Fragment() {

    private lateinit var binding: MainFragmentBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val currentLocale = Locale.getDefault()
        val quiz = if (currentLocale.language == "ru") {
            // Если текущая локализация Россия, загружаем русские вопросы
            QuizStorage.getQuiz(QuizStorage.Locale.Ru)
        } else {
            // В противном случае загружаем английские вопросы
            QuizStorage.getQuiz(QuizStorage.Locale.En)
        }
//        QuizStorage.getQuiz(QuizStorage.Locale.Ru) // Или QuizStorage.Locale.EN в зависимости от локализации
        addQuestions(quiz.questions)
        binding.backButton.setOnClickListener{
            findNavController().navigate(R.id.action_mainFragment_to_welcomeFragment)
        }

        binding.submitButton.setOnClickListener {
            val selectedAnswers = mutableListOf<String>()
            for (i in 0 until binding.quizContainer.childCount) {
                val view = binding.quizContainer.getChildAt(i)
                if (view is RadioGroup) {
                    val radioButtonId = view.checkedRadioButtonId
                    if (radioButtonId != -1) { // Убедитесь, что ответ был выбран
                        val radioButton = view.findViewById<RadioButton>(radioButtonId)
                        selectedAnswers.add(radioButton.text.toString())
                    } else {
                        // Возможно, показать Toast, что не все вопросы были отвечены
                        Toast.makeText(context, getString(R.string.answer_all_questions), Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                }
            }
            // передача выбранных ответов как строки
            val selectedAnswersString = selectedAnswers.joinToString(",")
            val action = MainFragmentDirections.actionMainFragmentToResultFragment(quizResult = selectedAnswersString)
            findNavController().navigate(action)
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
                        alpha = 0f // начальная прозрачность
                    }
                    group.addView(radioButton)
                    animateView(radioButton)
                }
            }
            binding.quizContainer.addView(answersRadioGroup)
        }
    }
    private fun animateView(view: View) {
        ObjectAnimator.ofFloat(view, "alpha", 0f, 1f).apply {
            duration = 3000 // Продолжительность анимации в миллисекундах
        }.start()
    }
}