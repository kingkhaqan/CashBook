package com.example.zeesh.cashbook

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var userId: Long? = -1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        userId = intent.getLongExtra("userId", -1)
//        LableTransactionsDebitListTask(this).execute(userId?.toString())

        val currency = intent.getStringExtra("currency")

//        Toast.makeText(this, "main: $currency", Toast.LENGTH_SHORT).show()

        val titles = mutableListOf<String>()
        val frags = mutableListOf<Fragment>()
        titles.add("home")
        titles.add("Debit")
        titles.add("Credit")
        frags.add(HomeFragment())
        frags.add(DebitFragment())
        frags.add(CreditFragment())


        viewpager.adapter = MainPagerAdapter(titles, frags, supportFragmentManager)

        val rr = object : ResultReceiver(Handler()){
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {

                if (resultCode== RSLT_TRANSACTION_LIST){
                    val transs = resultData?.getParcelableArray("trasactions")
                    var amount = 0.toDouble()
                    transs?.forEach {
                        val t = it as _Transaction
                        if (t.type == TRANSACTION_TYPE_DEBIT)
                            amount += t.amount!!
                        else
                            amount -= t.amount!!
                    }

                    textview_main.text = "${getCurrencySymbol(currency)}$amount"
                }
            }
        }

        startIntentForTransactionList(rr, userId, TRANSACTION_TYPE_ALL)


        viewpager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener{
            override fun onPageScrollStateChanged(state: Int) {}
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
            override fun onPageSelected(position: Int) {

                when(position){
                    0->{
                        startIntentForTransactionList(rr, userId, TRANSACTION_TYPE_ALL)
//                        fab.visibility = View.GONE
                    }
                    1->{
                        startIntentForTransactionList(rr, userId, TRANSACTION_TYPE_DEBIT)
//                        fab.visibility = View.VISIBLE
                    }
                    2->{
                        startIntentForTransactionList(rr, userId, TRANSACTION_TYPE_CREDIT)
//                        fab.visibility = View.VISIBLE
                    }
                }
            }
        })



        setSupportActionBar(toolbar)

        tabs.setupWithViewPager(viewpager)

    }

    fun startIntentForTransactionList(resultReceiver: ResultReceiver?, userId: Long?, type: String?){
        val intent = Intent(this, TransactionListIntentService::class.java)
        intent.putExtra("rr", resultReceiver)
        intent.putExtra("userId", userId)
        intent.putExtra("type", type)
        startService(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.signout -> {

            MarkLoggedInFalse().execute(userId)
            startActivity(Intent(this, Login2Activity::class.java))
            finish()
            true
        }

        R.id.about -> {

            true
        }

        else -> {

            super.onOptionsItemSelected(item)
        }
    }
}


