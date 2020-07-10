package com.example.zeesh.cashbook

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.*
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import java.io.Serializable
import java.lang.reflect.Array
import java.text.SimpleDateFormat
import java.util.*










/**
 * Created by zeesh on 4/8/2019.
 */

fun getCurrencySymbol(currency: String?): String?{
    when(currency){
        CURRENCY_USD-> return "$"
        CURRENCY_UKP-> return "£"
        CURRENCY_EUR-> return "€"
        else-> return "Rs"
    }
}
fun getWeekDay(date: _Date): String{

    val d = GregorianCalendar(date.year!!, date.month!!-1, date.day!!)
    val day = SimpleDateFormat("EEEE").format(d.time)
    return day
}
fun getCurrentData(): _Date{


    val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val arr = date.split("-")
    return _Date(arr[2].toInt(), arr[1].toInt(), arr[0].toInt())
}
fun getDate(timestamp: Long): _Date? {
    val cal = Calendar.getInstance()
    cal.timeInMillis = timestamp
    val date = cal.time
    val mDay = date.day
    val mMonth = date.month
    val mYear = date.year
    return _Date(mDay, 3, 2019)
}
fun getDateString(day: Int?, month: Int?, year: Int?): CharSequence? {
    return "$month $day, $year"
}
fun getDateString(date: _Date?): String?{
    return "${getMonthString(date?.month)} ${date?.day}, ${date?.year}"
}
fun getMonthString(month: Int?): String{
    when(month){
        1-> return "January"
        2-> return "Feburary"
        3-> return "March"
        4-> return "April"
        5-> return "May"
        6-> return "June"
        7-> return "July"
        8-> return "August"
        9-> return "September"
        10-> return "October"
        11-> return "November"
        12-> return "December"
    }
    return "None"
}


class MainPagerAdapter(val titles: MutableList<String>, val fragments: MutableList<Fragment>, fm: FragmentManager): FragmentPagerAdapter(fm) {
    override fun getItem(position: Int) = fragments[position]
    override fun getCount() = fragments.size
    override fun getPageTitle(position: Int) = titles[position]
}

data class _Date(var day: Int?=0, var month: Int?=0, var year: Int?=0)
class MString(var str: String?) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(str)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<MString> = object : Parcelable.Creator<MString> {
            override fun createFromParcel(source: Parcel): MString = MString(source)
            override fun newArray(size: Int)= arrayOfNulls<MString?>(size)
        }
    }
}

data class _Transaction(var amount: Double?, var title: String?, var year: Int?, var month: Int?, var day: Int?, var note: String?, var reciept: ByteArray?, var type: String, var timestamp: Long?, var userId: Long?) : Parcelable {
    constructor(source: Parcel) : this(
            source.readValue(Double::class.java.classLoader) as Double?,
            source.readString(),
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readValue(Int::class.java.classLoader) as Int?,
            source.readString(),
            source.createByteArray(),
            source.readString(),
            source.readValue(Long::class.java.classLoader) as Long?,
            source.readValue(Long::class.java.classLoader) as Long?
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeValue(amount)
        writeString(title)
        writeValue(year)
        writeValue(month)
        writeValue(day)
        writeString(note)
        writeByteArray(reciept)
        writeString(type)
        writeValue(timestamp)
        writeValue(userId)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<_Transaction> = object : Parcelable.Creator<_Transaction> {
            override fun createFromParcel(source: Parcel): _Transaction = _Transaction(source)
            override fun newArray(size: Int)= arrayOfNulls<_Transaction?>(size)
        }
    }
}

data class _User(var email: String?, var password: String?, var name: String?, var phone: String?, var amount: Double?, var currency: String?): Serializable

val ITEM_VIEW_TYPE_TRANSACTION = 1001
val ITEM_VIEW_TYPE_HEADING = 1002
 class RecyclerviewAdapter(val context: Context, val dataset: MutableList<Any>, val currency: String? = CURRENCY_PKR): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
     override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {


         when{
             holder is HeadingViewHolder->{holder.bind(dataset[position] as String)}
             holder is TransactionViewHolder->{
                 val transaction = dataset[position] as _Transaction
                 holder.bind(transaction, currency)

                 holder.itemView.setOnClickListener {


                     val view = LayoutInflater.from(context).inflate(R.layout.popup_view_view_trans, null , false)
                     val amountTV = view?.findViewById<TextView>(R.id.textview_amount_value)
                     val dateTV = view?.findViewById<TextView>(R.id.textview_date_value)
                     val noteTV = view?.findViewById<TextView>(R.id.textview_note_value)
                     val titleTV = view?.findViewById<TextView>(R.id.textview_title_value)
                     val recieptIV = view?.findViewById<ImageView>(R.id.imageview_reciept)

                     amountTV?.text = transaction.amount.toString()
                     dateTV?.text = getDateString(_Date(transaction.day, transaction.month, transaction.year))
                     noteTV?.text = transaction.note
                     titleTV?.text = transaction.title

                     val rr = object: ResultReceiver(Handler()){
                         override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                             when(resultCode){
                                 RSLT_DECODED_BMP->{

                                     val mbmp = resultData?.getParcelable<MBitmap>("bmp")
                                     if (mbmp!=null){

                                         recieptIV?.visibility = View.VISIBLE
                                        recieptIV?.setImageBitmap(mbmp.bitmap)
                                         recieptIV?.setOnClickListener {
                                             val intent = Intent(context, FullscreenActivity::class.java)
                                             intent.putExtra("bytes", transaction.reciept)
                                             context.startActivity(intent)
                                         }
//

                                     }

                                 }
                             }
                         }
                     }

//                     var bmp: Bitmap? = null
                     if (transaction.reciept!=null){

                         val intent = Intent(context, DecodeBmpIntent::class.java)
                         intent.putExtra("rr", rr)
                         intent.putExtra("bytes", transaction.reciept)
                         context.startService(intent)
//                         BitmapFactory.decodeByteArray(transaction.reciept, 0, transaction.reciept?.size!!)
                     }
                     else
                         Toast.makeText(context, "adapter image", Toast.LENGTH_SHORT).show()
//                     if (bmp!=null) {
//                         recieptIV?.visibility = View.VISIBLE
//                         recieptIV?.setImageBitmap(bmp)
//                     }
//                     else
//                         Toast.makeText(context, "bmp conversion error", Toast.LENGTH_SHORT).show()

                     val builder = AlertDialog.Builder(context)
                     builder.setView(view)
                     builder.show()
                 }
             }
         }
     }

     override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
         val inflator = LayoutInflater.from(context)
         when(viewType){
             ITEM_VIEW_TYPE_HEADING->{return HeadingViewHolder(inflator.inflate(R.layout.item_view_heading, parent, false))}
             ITEM_VIEW_TYPE_TRANSACTION->{return TransactionViewHolder(inflator.inflate(R.layout.item_view_transaction, parent, false))}
         }

         return ErrorViewHolder(null)
     }



     override fun getItemCount() = dataset.size


    override fun getItemViewType(position: Int): Int {
        val dataObj = dataset[position]
        when{
            dataObj==null->{}
            dataObj is String->return ITEM_VIEW_TYPE_HEADING
            dataObj is _Transaction->return ITEM_VIEW_TYPE_TRANSACTION
        }
        return super.getItemViewType(position)
    }
}

class TransactionViewHolder(v: View?): RecyclerView.ViewHolder(v){
    val title: TextView?
    val subtitle: TextView?
    val amount: TextView?
    init {
        title = v?.findViewById(R.id.textview_title)
        subtitle = v?.findViewById(R.id.textview_subtitle)
        amount = v?.findViewById(R.id.textview_amount)
    }

    fun bind(transactio: _Transaction, currency: String?){
        title?.text = transactio.title
        var receipt = ""
        if (transactio.reciept!=null)
            receipt = "-Reciept"
        subtitle?.text = "${getWeekDay(_Date(transactio.day, transactio.month, transactio.year))}, ${transactio.day}$receipt"
        amount?.text = "${getCurrencySymbol(currency)}${transactio.amount}"
    }

    private fun getDayString(day: Int?): String {
        return "Monday"
    }
}

class HeadingViewHolder(v: View?): RecyclerView.ViewHolder(v){
    val heading: TextView?
    init {
        heading = v?.findViewById(R.id.textview_heading)
    }
    fun bind(text: String){
        heading?.text = text
    }
}

class ErrorViewHolder(v:View?): RecyclerView.ViewHolder(v)


