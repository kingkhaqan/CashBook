package com.example.zeesh.cashbook

import android.annotation.SuppressLint
import android.app.IntentService
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.os.ResultReceiver
import android.provider.BaseColumns
import android.widget.Toast

/**
 * Created by zeesh on 4/14/2019.
 */

val RSLT_LOGIN_PASSWORD_CORRECT = 1001
val RSLT_LOGIN_PASSWORD_INCORRECT = 1002
val RSLT_SIGNUP_SUCCESS = 1003
val RSLT_SIGNUP_FAIL = 1004
val RSLT_TRANSACTION_LIST = 1005
val RSLT_TRANSACTION_INSERT_SUCCESS = 1006
val RSLT_TRANSACTION_INSERT_FAIL = 1007
val RSLT_LABLED_TRANSACTIONS = 1008
val RSLT_TRANSACTION_LIST_FAIL = 1009
val RSLT_DECODED_BMP = 1010

data class MBitmap(var bitmap: Bitmap?) : Parcelable {
    constructor(source: Parcel) : this(
            source.readParcelable<Bitmap>(Bitmap::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(bitmap, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MBitmap> = object : Parcelable.Creator<MBitmap> {
            override fun createFromParcel(source: Parcel): MBitmap = MBitmap(source)
            override fun newArray(size: Int): Array<MBitmap?> = arrayOfNulls(size)
        }
    }
}

class DecodeBmpIntent: IntentService("DecodeBmp") {
    override fun onHandleIntent(intent: Intent?) {
        val rr = intent?.getParcelableExtra<ResultReceiver>("rr")
        val bytes = intent?.getByteArrayExtra("bytes")

        val bundle = Bundle()
        if (bytes!=null){
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            if (bmp!=null){
                bundle.putParcelable("bmp", MBitmap(bmp))
                rr?.send(RSLT_DECODED_BMP, bundle)
            }
        }
    }
}

class MonthTransactionList: IntentService("MonthTransaction") {
    override fun onHandleIntent(intent: Intent?) {
        val reciever = intent?.getParcelableExtra<ResultReceiver>("rr")
        val userId = intent?.getStringExtra("userId")
        val year = intent?.getStringExtra("year")
        val month = intent?.getStringExtra("month")



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
            val sortOrder = "${CashBookContract.TransactionEntry.COLUMN_NAME_DAY} ASC"

                val selection = "${CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID}=? AND ${CashBookContract.TransactionEntry.COLUMN_NAME_YEAR} =? AND ${CashBookContract.TransactionEntry.COLUMN_NAME_MONTH}=?"
                val selectionArgs = arrayOf(userId, year, month)

        if (userId!=null && year!=null && month!=null)
            recieveAllTransactions(this, reciever, projection, selection, selectionArgs, sortOrder)


    }
}

class LabledTransactionsList: IntentService("LabledTransactions") {
    override fun onHandleIntent(intent: Intent?) {
        val rr = intent?.getParcelableExtra<ResultReceiver>("rr")
        val transactions = intent?.getParcelableArrayExtra("transactions")



        data class _Month(val year: Int, val month: Int)
        val monthsList = mutableListOf<_Month>()
        val isContain = {month: _Month, months: MutableList<_Month> ->
            months.forEach {
                if (it==month)
                    true
            }
            false
        }
        val sortMonths= { months: MutableList<_Month> ->
            val sorted = mutableListOf<_Month>()
            val n= months.size
            for (i in 0..n-2) {
                var max = i
                for (j in i+1..n-1){

                    val mw = months[max].year*100+months[max].month
                    val jw = months[j].year*100+months[j].month
                    if (jw>mw)
                        max = j
                }

                if (max!=i){
                    val temp = months[i]
                    months[i] = months[max]
                    months[max] = temp
                }
            }
            sorted
        }

        transactions?.forEach {
            val t = it as _Transaction
            val m = _Month(t.year!!, t.month!!)
            if (!isContain(m, monthsList))
                monthsList.add(m)
        }

        if (monthsList.size!=0) {

            val labled = mutableListOf<Parcelable>()
            sortMonths(monthsList).forEach {
                val sm = it
                val lable = "${it.month} ${it.year}"
                labled.add(MString(lable))
                transactions?.forEach {
                    val t = it as _Transaction
                    val m = _Month(t.year!!, t.month!!)
                    if (sm == m)
                        labled.add(t)
                }
            }
            val bundle = Bundle()
            if (labled.size!=0){
                bundle.putParcelableArray("labled", labled.toTypedArray())
                rr?.send(RSLT_LABLED_TRANSACTIONS, bundle)
            }
            else
                Toast.makeText(this, "labled error", Toast.LENGTH_SHORT).show()

        }



    }

}

class TransactionListIntentService: IntentService("TransactionList") {


//    var reciever: ResultReceiver? = null
//    var type: String? = null
//    var userId: String? = null
    override fun onHandleIntent(intent: Intent?) {
        val reciever = intent?.getParcelableExtra<ResultReceiver>("rr")
        val type = intent?.getStringExtra("type")
        val userId = intent?.getLongExtra("userId", -1)


//        val c = db?.execSQL(query)




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
        val sortOrder = "${CashBookContract.TransactionEntry.COLUMN_NAME_TIME} DESC"

        if (type!=TRANSACTION_TYPE_ALL) {
            val selection = "${CashBookContract.TransactionEntry.COLUMN_NAME_TYPE} =? AND ${CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID}=?"
            val selectionArgs = arrayOf(type, userId?.toString())
            if (userId!=null)
            recieveAllTransactions(this, reciever, projection, selection, selectionArgs, sortOrder)
        }
        else{
            val selection = "${CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID}=?"
            val selectionArgs = arrayOf(userId?.toString())
            if (userId!=null)
            recieveAllTransactions(this, reciever, projection, selection, selectionArgs, sortOrder)
        }






    }


}

fun recieveAllTransactions(context: Context?, reciever: ResultReceiver?, projection: Array<String>, selection: String, selectionArgs: Array<String?>, sortOrder: String) {
    val transactions = mutableListOf<_Transaction>()
    val db = dbHelper?.readableDatabase

    val cursor = db?.query(
            CashBookContract.TransactionEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
    )
    val bundle = Bundle()
    if (cursor!=null)
        with(cursor) {
            while (moveToNext()) {
                val id = getLong(getColumnIndex(android.provider.BaseColumns._ID))
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
                transactions.add(_Transaction(amount, title, year, month, day, note, reciept, t, time, uid))

//                if (reciept!=null)
//                    Toast.makeText(baseContext, "image from db", Toast.LENGTH_SHORT).show()
            }



//            if (transactions.size > 0) {
                bundle.putParcelableArray("trasactions", transactions.toTypedArray())
                reciever?.send(RSLT_TRANSACTION_LIST, bundle)

//            }
//            else
//                reciever?.send(RSLT_TRANSACTION_LIST_FAIL, bundle)

        }
    else
        Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()

}


class InsertTransactionIntentService: IntentService("InserTransaction") {
    override fun onHandleIntent(intent: Intent?) {

        val reciever = intent?.getParcelableExtra<ResultReceiver>("rr")
        val transaction = intent?.getParcelableExtra<_Transaction>("transaction")

        val db = dbHelper?.writableDatabase
        val values = ContentValues().apply {
            put(CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT, transaction?.amount)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TITLE, transaction?.title)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_MONTH, transaction?.month)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_YEAR, transaction?.year)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_DAY, transaction?.day)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TIME, transaction?.timestamp)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_NOTE, transaction?.note)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT, transaction?.reciept)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TYPE, transaction?.type)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_USER_ID, transaction?.userId)
        }

        val id =  db?.insert(CashBookContract.TransactionEntry.TABLE_NAME, null, values)
        val bundle = Bundle()
        if (id!=null && id!=-1L){
            bundle.putLong("id", id)
            reciever?.send(RSLT_TRANSACTION_INSERT_SUCCESS, bundle)
        }
        else
            reciever?.send(RSLT_TRANSACTION_INSERT_FAIL, bundle)

    }
}


class SignupIntentService: IntentService("SignupIntentService") {
    override fun onHandleIntent(intent: Intent?) {
        val reciever = intent?.getParcelableExtra<ResultReceiver?>("rr")
        val user = intent?.getSerializableExtra("user") as _User

        val db = dbHelper?.writableDatabase
        val values = ContentValues().apply {
            put(CashBookContract.UserEntry.COLUMN_NAME_NAME, user.name)
            put(CashBookContract.UserEntry.COLUMN_NAME_PHONE, user.phone)
            put(CashBookContract.UserEntry.COLUMN_NAME_EMAIL, user.email)
            put(CashBookContract.UserEntry.COLUMN_NAME_PASSWORD, user.password)
            put(CashBookContract.UserEntry.COLUMN_NAME_AMOUNT, user.amount)
            put(CashBookContract.UserEntry.COLUMN_NAME_CURRENCY, user.currency)
        }

        val id = db?.insert(CashBookContract.UserEntry.TABLE_NAME, null, values)
        val bundle = Bundle()
        if (id!=null && id!=-1L){
            bundle.putLong("id", id)
            bundle.putString("currency", user.currency)
            reciever?.send(RSLT_SIGNUP_SUCCESS, bundle)
//            MarkLoggedInTrue().execute(id)

        }
        else
            reciever?.send(RSLT_SIGNUP_FAIL, bundle)
    }
}


class LoginIntenService: IntentService("LoginService") {
    @SuppressLint("RestrictedApi")
    override fun onHandleIntent(intent: Intent?) {
        val reciever: android.support.v4.os.ResultReceiver? = intent?.getParcelableExtra("reciever")
        val email = intent?.getStringExtra("email")
        val password = intent?.getStringExtra("password")

        val bundle = Bundle()


        val db = dbHelper?.readableDatabase
        val projection = arrayOf(
                BaseColumns._ID,
                CashBookContract.UserEntry.COLUMN_NAME_PASSWORD,
                CashBookContract.UserEntry.COLUMN_NAME_CURRENCY
        )
        val selection = "${CashBookContract.UserEntry.COLUMN_NAME_EMAIL} =?"
        val selectionArgs = arrayOf(email)
        val sortOrder = "${BaseColumns._ID} DESC"
        val cursor = db?.query(
                CashBookContract.UserEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        )
        if (cursor!=null && cursor.count>0) {
            with(cursor) {
                while (moveToNext()) {
                    val id = getLong(getColumnIndex(BaseColumns._ID))
                    val pw = getString(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.UserEntry.COLUMN_NAME_PASSWORD))
                    val currency = getString(getColumnIndex(com.example.zeesh.cashbook.CashBookContract.UserEntry.COLUMN_NAME_CURRENCY))
                    if (pw != null && pw != "" && pw == password) {
                        bundle.putLong("id", id)
                        bundle.putString("currency", currency)
                        reciever?.send(RSLT_LOGIN_PASSWORD_CORRECT, bundle)
//                        Toast.makeText(, "login: $currency", Toast.LENGTH_SHORT).show()
//                    MarkLoggedInTrue().execute(id)
                    } else
                        reciever?.send(RSLT_LOGIN_PASSWORD_INCORRECT, bundle)


                }
            }
        }
        else
            Toast.makeText(this, "Email isn't correct", Toast.LENGTH_SHORT).show()


    }
}
