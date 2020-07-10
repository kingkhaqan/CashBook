package com.example.zeesh.cashbook

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import java.lang.Long.getLong

/**
 * Created by zeesh on 4/10/2019.
 */



object CashBookContract {
    // Table contents are grouped together in an anonymous object.
    object UserEntry : BaseColumns {
        const val TABLE_NAME = "user"
        const val COLUMN_NAME_NAME = "name"
        const val COLUMN_NAME_PHONE = "phone"
        const val COLUMN_NAME_EMAIL = "email"
        const val COLUMN_NAME_PASSWORD = "password"
        const val COLUMN_NAME_AMOUNT = "amount"
        const val COLUMN_NAME_CURRENCY = "currency"
        const val COLUMN_NAME_LOGGED_IN = "loggedin"
        const val SQL_CREATE_TABLE_USER =
                "CREATE TABLE ${TABLE_NAME} (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${COLUMN_NAME_NAME} TEXT," +
                        "${COLUMN_NAME_PHONE} TEXT," +
                        "${COLUMN_NAME_EMAIL} TEXT," +
                        "${COLUMN_NAME_PASSWORD} TEXT," +
                        "${COLUMN_NAME_AMOUNT} TEXT," +
                        "${COLUMN_NAME_CURRENCY} TEXT," +
                        "$COLUMN_NAME_LOGGED_IN INT)"
        const val SQL_DROP_TABLE_USER =
                "DROP TABLE IF EXISTS $TABLE_NAME"
    }
    object TransactionEntry: BaseColumns{
        const val TABLE_NAME = "trans"
        const val COLUMN_NAME_AMOUNT = "amount"
        const val COLUMN_NAME_TITLE = "title"
        const val COLUMN_NAME_MONTH = "month"
        const val COLUMN_NAME_DAY = "day"
        const val COLUMN_NAME_YEAR = "year"
        const val COLUMN_NAME_TIME = "time"
        const val COLUMN_NAME_NOTE = "note"
        const val COLUMN_NAME_RECIEPT = "reciept"
        const val COLUMN_NAME_TYPE = "type"
        const val COLUMN_NAME_USER_ID = "user_id"

        const val SQL_CREATE_TABLE =
                "CREATE TABLE $TABLE_NAME (" +
                        "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                        "${COLUMN_NAME_AMOUNT} DOUBLE," +
                        "${COLUMN_NAME_TITLE} TEXT," +
                        "${COLUMN_NAME_MONTH} INTEGER," +
                        "${COLUMN_NAME_YEAR} INTEGER," +
                        "${COLUMN_NAME_DAY} INTEGER," +
                        "${COLUMN_NAME_TIME} LONG," +
                        "${COLUMN_NAME_NOTE} TEXT," +
                        "${COLUMN_NAME_RECIEPT} BLOB," +
                        "${COLUMN_NAME_TYPE} TEXT," +
                        "${COLUMN_NAME_USER_ID} INTEGER)"

        const val SQL_DROP_TABLE =
                "DROP TABLE IF EXISTS $TABLE_NAME"
    }
}

var dbHelper: CashBookDbHelper? = null
class CashBookDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(CashBookContract.UserEntry.SQL_CREATE_TABLE_USER)
        db.execSQL(CashBookContract.TransactionEntry.SQL_CREATE_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(CashBookContract.UserEntry.SQL_DROP_TABLE_USER)
        db.execSQL(CashBookContract.TransactionEntry.SQL_DROP_TABLE)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        // If you change the database schema, you must increment the database version.
        const val DATABASE_VERSION = 4
        const val DATABASE_NAME = "CashBook.db"
    }


    fun insertTransaction(transaction: _Transaction): Long?{
        val db = writableDatabase
        val values = ContentValues().apply {
            put(CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT, transaction.amount)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TITLE, transaction.title)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_MONTH, transaction.month)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_YEAR, transaction.year)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_DAY, transaction.day)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TIME, transaction.timestamp)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_NOTE, transaction.note)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT, transaction.reciept)
            put(CashBookContract.TransactionEntry.COLUMN_NAME_TYPE, transaction.type)
        }

        return db.insert(CashBookContract.TransactionEntry.TABLE_NAME, null, values)
    }

//    fun getAllDebitTransactions(type: String): MutableList<_Transaction>{
//        val transactions = mutableListOf<_Transaction>()
//        val query = "SELECT * FROM ${CashBookContract.TransactionEntry.TABLE_NAME} WHERE ${CashBookContract.TransactionEntry.COLUMN_NAME_TYPE} =? $TRANSACTION_TYPE_DEBIT"
//        val db = readableDatabase
//        val projection = arrayOf(
//                BaseColumns._ID,
//                CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT,
//                CashBookContract.TransactionEntry.COLUMN_NAME_TITLE,
//                CashBookContract.TransactionEntry.COLUMN_NAME_YEAR,
//                CashBookContract.TransactionEntry.COLUMN_NAME_MONTH,
//                CashBookContract.TransactionEntry.COLUMN_NAME_DAY,
//                CashBookContract.TransactionEntry.COLUMN_NAME_TIME,
//                CashBookContract.TransactionEntry.COLUMN_NAME_NOTE,
//                CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT
//                )
//        val selection = "${CashBookContract.TransactionEntry.COLUMN_NAME_TYPE} =?"
//        val selectionArgs = arrayOf(type)
//        val sortOrder = "${CashBookContract.TransactionEntry.COLUMN_NAME_TIME} DESC"
//        val cursor = db.query(
//                CashBookContract.TransactionEntry.TABLE_NAME,
//                projection,
//                selection,
//                selectionArgs,
//                null,
//                null,
//                sortOrder
//        )
//        with(cursor){
//            while (moveToNext()){
//                val id = getLong(BaseColumns._ID)
//                val amount = getDouble(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_AMOUNT))
//                val title = getString(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_TITLE))
//                val month = getInt(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_MONTH))
//                val day = getInt(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_DAY))
//                val year = getInt(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_YEAR))
//                val time = getLong(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_TIME))
//                val note = getString(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_NOTE))
//                val reciept = getBlob(getColumnIndex(CashBookContract.TransactionEntry.COLUMN_NAME_RECIEPT))
//                transactions.add(_Transaction(amount, title, year, month, day, note, reciept, type!!,  time))
//            }
//        }
//
//        return transactions
//    }


}


//private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedEntry.TABLE_NAME}"