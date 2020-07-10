package com.example.zeesh.cashbook

import android.app.IntentService
import android.content.ContentValues
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_signup.*

val CURRENCY_USD = "USD"
val CURRENCY_PKR = "PKR"
val CURRENCY_UKP = "UKP"
val CURRENCY_EUR = "EUR"


class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val currencies = arrayOf(CURRENCY_PKR, CURRENCY_USD, CURRENCY_UKP, CURRENCY_EUR)
        ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, currencies).also {
            arrayAdapter ->

            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner_currency.adapter = arrayAdapter

        }

        signup_button.setOnClickListener {
            login_progress.visibility = View.VISIBLE
            val name = edittext_name.text.toString()
            val phone = edittext_phone.text.toString()
            val currency = spinner_currency.selectedItem as String
            val email = intent.getStringExtra("email")
            val password = intent.getStringExtra("password")

            val user = _User(email, password, name, phone, 0.toDouble(), currency)
            val resultReciever = object : ResultReceiver(Handler()){
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    login_progress.visibility = View.GONE
                    when(resultCode){
                        RSLT_SIGNUP_SUCCESS->{
                            val id = resultData?.getLong("id")
//                            val currency = resultData?.getLong("currency")
                            Toast.makeText(this@SignupActivity, "${resultData?.get("id")}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SignupActivity, MainActivity::class.java).also {
                                intent ->
                                intent.putExtra("userId", id)
                                intent.putExtra("currency", currency)
                                MarkLoggedInTrue().execute(id)
                                finish()
                            })
                        }
                        RSLT_SIGNUP_FAIL->{
                            Toast.makeText(this@SignupActivity, "error occured", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
            val intnt = Intent(this, SignupIntentService::class.java)
            intnt.putExtra("rr", resultReciever)
            intnt.putExtra("user", user)
            startService(intnt)

//            val id = dbHelper?.insertUser(_User(email, password, name, phone, 0.toDouble(), currency))
//            Toast.makeText(this, id?.toString(), Toast.LENGTH_SHORT).show()
        }
    }
}

