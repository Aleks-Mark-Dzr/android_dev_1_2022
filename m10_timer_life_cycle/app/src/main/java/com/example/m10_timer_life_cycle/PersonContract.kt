package com.example.m10_timer_life_cycle

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract

class PersonContract : ActivityResultContract<Boolean, Person?>() {

    override fun createIntent(context: Context, input: Boolean): Intent {
        val intent = Intent(context, SecondActivity::class.java)
        intent.putExtra(EXTRA_HAS_MIDDLE_NAME, input)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Person? {
        if (resultCode != Activity.RESULT_OK || intent == null) {
            return null
        }
        return intent.getParcelableExtra(EXTRA_PERSON)
    }

    companion object {
        const val EXTRA_HAS_MIDDLE_NAME = "com.example.hasMiddleName"
        const val EXTRA_PERSON = "com.example.person"
    }
}