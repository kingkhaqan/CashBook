package com.example.zeesh.cashbook

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.BaseColumns
import android.support.v4.os.ResultReceiver
import android.view.View
import android.widget.TextView
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_login2.*

class Login2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
//        setTheme(R.style.MyMaterialTheme_Launcher)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login2)



        sign_in_button.setOnClickListener {
//            login_progress.visibility = View.VISIBLE
            val intnt = Intent(this, LoginIntenService::class.java)
            val rr =object: ResultReceiver(Handler()){
                override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                    login_progress.visibility = View.GONE
                    when(resultCode){
                        RSLT_LOGIN_PASSWORD_CORRECT->{
                            login_progress.visibility = View.GONE
                            textview_warning.visibility = View.GONE
                            val id = resultData?.getLong("id")
                            val currency = resultData?.getString("currency")
                            startActivity(Intent(this@Login2Activity, MainActivity::class.java).also {
                                intent ->
                                intent.putExtra("userId", id)
                                intent.putExtra("currency", currency)
//                                Toast.makeText(this@Login2Activity, "login: $currency", Toast.LENGTH_SHORT).show()
                                MarkLoggedInTrue().execute(id)
                                finish()
                            })
                        }
                        RSLT_LOGIN_PASSWORD_INCORRECT->{

                            login_progress.visibility = View.GONE
                            textview_warning.visibility = View.VISIBLE
                            textview_warning.text = "Password isn't correct"
//                            Toast.makeText(this@Login2Activity, "Password isn' correct", Toast.LENGTH_SHORT).show()
                        }
                        else->{
                            login_progress.visibility = View.GONE
                            textview_warning.visibility = View.VISIBLE
                            textview_warning.text = "Email isn't correct"
                        }
                    }
                }
            }

            val email = edittext_email.text.toString()
            val password = edittext_password.text.toString()

            if (email=="")
                Toast.makeText(this, "Provide email", Toast.LENGTH_SHORT).show()
//            else if (password=="")
//                Toast.makeText(this, "Provide password", Toast.LENGTH_SHORT).show()
            else {
                intnt.putExtra("email", email)
                intnt.putExtra("password", password)
                intnt.putExtra("reciever", rr)
                startService(intnt)

            }
        }
        signup_button.setOnClickListener {
            val email = edittext_email.text.toString()
            val password = edittext_password.text.toString()

            textview_warning.visibility = View.GONE
            if (password!="" && email!=""){

                val task = UserExistsTask(this, email, password).execute(email)



            }
            else
                Toast.makeText(this, "Provide email & password for signup", Toast.LENGTH_SHORT).show()
        }
    }


}

