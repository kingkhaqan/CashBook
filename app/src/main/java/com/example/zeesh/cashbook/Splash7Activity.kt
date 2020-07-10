package com.example.zeesh.cashbook

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

class Splash7Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash7)

        dbHelper = CashBookDbHelper(this)

        LoginStatusTask(this).execute()
    }
}
