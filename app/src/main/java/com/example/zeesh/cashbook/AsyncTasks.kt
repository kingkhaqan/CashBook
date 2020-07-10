package com.example.zeesh.cashbook

import android.app.Activity
import android.content.Intent
import android.os.AsyncTask
import android.provider.BaseColumns
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView

/**
 * Created by zeesh on 4/18/2019.
 */


class LableTransactionsListTask(val context: Activity, val recyclerview: RecyclerView?, val currency: String?): AsyncTask<String, Unit, MutableList<Any>>() {
    override fun doInBackground(vararg param: String?): MutableList<Any> {
        val userId = param[0]
        val type = param[1]
        val labled = mutableListOf<Any>()
        val db = dbHelper?.readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT,
                CashBookContract.TransactionEntry.COLUMN_NAME_TITLE,
                CashBookContract.TransactionEntry.COLUMN_NAME_YEAR,
                CashBookContract.TransactionEntry.COLUMN_NAME_MONTH,
                CashBookContract.TransactionEntry.COLUMN_NAME_DAY,
                CashBookContract.TransactionEntry.COLUMN_NAME_TIME,
                CashBookContract.TransactionEntry.COLUMN_NAME_NOTE,
                CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT,
                CashBookContract.TransactionEntry.COLUMN_NAME_TYPE,
                CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID
        )
        val sortOrder = "${CashBookContract.TransactionEntry.COLUMN_NAME_YEAR} DESC, ${CashBookContract.TransactionEntry.COLUMN_NAME_MONTH} DESC, ${CashBookContract.TransactionEntry.COLUMN_NAME_DAY} DESC"
        val selection = "${CashBookContract.TransactionEntry.COLUMN_NAME_TYPE} =? AND ${CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID}=?"
        val selectionArgs = arrayOf(type, userId)
        val cursor = db?.query(
                CashBookContract.TransactionEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        )
        var y = -1
        var m = -1
//        var str = "months: "
        if (cursor!=null && cursor.count>0)
            with(cursor){
                while(moveToNext()) {
                    val amount = getDouble(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT))
                    val title = getString(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_TITLE))
                    val month = getInt(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_MONTH))
                    val day = getInt(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_DAY))
                    val year = getInt(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_YEAR))
                    val time = getLong(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_TIME))
                    val note = getString(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_NOTE))
                    val reciept = getBlob(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT))
                    val t = getString(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_TYPE))
                    val uid = getLong(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID))
                    val transaction = _Transaction(amount, title, year, month, day, note, reciept, t, time, uid)


                    if (y != year || m != month) {
                        labled.add("${getMonthString(month)} $year")
                        y = year
                        m = month
//                        labled.add(transaction)
                    }
                        labled.add(transaction)

                }

            }
//        Toast.makeText(context, str, Toast.LENGTH_LONG).show()

        return labled
    }

    override fun onPostExecute(result: MutableList<Any>?) {


        recyclerview?.adapter = RecyclerviewAdapter(context, result!!, currency)

//        var str = "lables(n=${result?.size}): "
//        result?.forEach {
//            if (it is String)
//                str+= "$it, "
//        }
//        Toast.makeText(context, str, Toast.LENGTH_LONG).show()
    }
}

class LoginStatusTask(val context: Activity): AsyncTask<Unit, Unit, Array<String?>>() {
    override fun doInBackground(vararg param: Unit?): Array<String?> {

        val db = dbHelper?.readableDatabase
        val projection = arrayOf("${BaseColumns._ID}", "${CashBookContract.UserEntry.COLUMN_NAME_CURRENCY}" )
        val selection = "${CashBookContract.UserEntry.COLUMN_NAME_LOGGED_IN}=?"
        val selectionArgs = arrayOf("1")
        val cursor = db?.query(
                CashBookContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null)
        if (cursor!=null && cursor.count>0) {
            with(cursor) {
                moveToFirst()
                val id = getLong(getColumnIndex(android.provider.BaseColumns._ID))
                val currency = getString(getColumnIndex(CashBookContract.UserEntry.COLUMN_NAME_CURRENCY))
                if (id != -1L && id != 0L && currency!=null)
                    return arrayOf(id.toString(), currency)
            }
        }

        return arrayOfNulls(2)
    }

    override fun onPostExecute(result: Array<String?>?) {

        val id = result!![0]
        val currency = result[1]
        if (id==null){
            context.startActivity(Intent(context, Login2Activity::class.java))
        }
        else{
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("userId", id.toLong())
            intent.putExtra("currency", currency)
            context.startActivity(intent)
            context.finish()
        }
    }
}


class MarkLoggedInTrue: AsyncTask<Long, Unit, Unit>() {
    override fun doInBackground(vararg param: Long?) {
        val id = param[0]
        val db = dbHelper?.writableDatabase
        if (id!=null){
            val query = "UPDATE ${CashBookContract.UserEntry.TABLE_NAME} SET ${CashBookContract.UserEntry.COLUMN_NAME_LOGGED_IN} = 1 WHERE ${BaseColumns._ID} = $id"
            db?.execSQL(query)
        }
    }

}

class MarkLoggedInFalse: AsyncTask<Long, Unit, Unit>() {
    override fun doInBackground(vararg param: Long?) {
        val id = param[0]
        val db = dbHelper?.writableDatabase
        if (id!=null){
            val query = "UPDATE ${CashBookContract.UserEntry.TABLE_NAME} SET ${CashBookContract.UserEntry.COLUMN_NAME_LOGGED_IN} = 0 WHERE ${BaseColumns._ID} = $id"
            db?.execSQL(query)
        }
    }

}

class UserExistsTask(var context: Activity, var email: String, var password: String): AsyncTask<String, Unit, Boolean>() {
    override fun doInBackground(vararg param: String?): Boolean {
        val email = param[0]
        val db = dbHelper?.readableDatabase
        val projection = arrayOf("${BaseColumns._ID}")
        val selection = "${CashBookContract.UserEntry.COLUMN_NAME_EMAIL}=?"
        val selectionArgs = arrayOf("$email")
        val cursor = db?.query(
                CashBookContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null)
        if (cursor!=null && cursor.count>0) {
            with(cursor) {
                moveToFirst()
                val id = getLong(getColumnIndex(BaseColumns._ID))
                if (id != -1L && id != 0L)
                    return true
            }
        }

        return false
    }

    override fun onPostExecute(result: Boolean?) {

        if (!result!!){

            val intnt = Intent(context, SignupActivity::class.java)
            intnt.putExtra("email", email)
            intnt.putExtra("password", password)
            context.startActivity(intnt)
        }
        else{
            val tv = context.findViewById<TextView>(R.id.textview_warning)
            tv.visibility = View.VISIBLE
            tv.text = "Email already exists"
        }
    }
}