package com.example.zeesh.cashbook

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.*
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.provider.MediaStore
import android.widget.*
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import java.io.ByteArrayOutputStream
import com.jjoe64.graphview.series.LineGraphSeries




//  adb -d shell "run-as com.example.zeesh.cashbook cat /data/data/com.example.zeesh.cashbook/databases/CashBook.sqlite > /sdcard/CashBook.sqlite"


/**
 * Created by zeesh on 4/8/2019.
 */


fun getLabledTransactions(transactions: Array<Parcelable>?): MutableList<Any>{

    val labled = mutableListOf<Any>()
    transactions?.forEach {
        val t = it as _Transaction
        labled.add(t)
    }
    return labled
}

class HomeFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val userId = activity?.intent?.getLongExtra("userId", -1)
        val itemView = inflater!!.inflate(R.layout.fragment_home, container, false)
        val graphView = itemView.findViewById<GraphView>(R.id.graphview)


        val reciever = object: ResultReceiver(Handler()){
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                when(resultCode){
                    RSLT_TRANSACTION_LIST->{
                        val debitSeriesDPs = mutableListOf<DataPoint>()
                        val creditSeriesDPs = mutableListOf<DataPoint>()
                        val amountSeriesDPs = mutableListOf<DataPoint>()
                        val transs = resultData?.getParcelableArray("trasactions")
                        Toast.makeText(activity, "This month transactions ${transs?.size}", Toast.LENGTH_SHORT).show()
                        if (transs!=null && transs.size>0){

                            var preTrans = transs[0] as _Transaction
                            var date = preTrans.day
                            var amount = 0.0
                            var dr = 0.0
                            var cr = 0.0
                            when(preTrans.type){
                                TRANSACTION_TYPE_DEBIT-> dr+=preTrans.amount!!
                                TRANSACTION_TYPE_CREDIT-> cr-=preTrans.amount!!
                            }
                            for (i in 1..transs.size-1){
                                val currTrans = transs[i] as _Transaction
                                if (preTrans.day!=currTrans.day){
                                    debitSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, dr))
                                    creditSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, cr))
                                    amount += dr+cr
                                    amountSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, amount))

                                    preTrans = currTrans
                                    dr = 0.0
                                    cr = 0.0
                                }
                                when(preTrans.type){
                                    TRANSACTION_TYPE_DEBIT-> dr+=preTrans.amount!!
                                    TRANSACTION_TYPE_CREDIT-> cr-=preTrans.amount!!
                                }


                            }

                            debitSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, dr))
                            creditSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, cr))
                            amount += dr+cr
                            amountSeriesDPs.add(DataPoint(preTrans.day?.toDouble()!!, amount))


//                            Toast.makeText(activity, "Date $str", Toast.LENGTH_SHORT).show()

                            graphView.addSeries(LineGraphSeries<DataPoint>(debitSeriesDPs.toTypedArray()).also {
                                it.color = Color.GREEN
                                it.thickness = 2
                            })
                            graphView.addSeries(LineGraphSeries<DataPoint>(creditSeriesDPs.toTypedArray()).also {
                                it.color = Color.RED
                                it.thickness = 2
                            })
                            graphView.addSeries(LineGraphSeries<DataPoint>(amountSeriesDPs.toTypedArray()).also {
                                it.thickness = 2
                            })
                            graphView.viewport.setMaxX(31.0)
                            graphView.viewport.setMinX(1.0)
                            graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter(){
                                override fun formatLabel(value: Double, isValueX: Boolean): String {
                                    if (isValueX){
                                        val v = (value*10).toInt()
                                        if (v%10==0)
                                            return value.toInt().toString()
                                        else return ""
                                    }
                                    return super.formatLabel(value, isValueX)
                                }
                            }

                        }


                    }
                }
            }
        }

        val date = getCurrentData()
        val mtIntent = Intent(activity, MonthTransactionList::class.java)
        mtIntent.putExtra("rr", reciever)
        mtIntent.putExtra("year", date.year.toString())
        mtIntent.putExtra("month", date.month.toString())
        mtIntent.putExtra("userId", userId?.toString())
        activity?.startService(mtIntent)

//        val rr = object : ResultReceiver(Handler()){
//            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
//
//                if (resultCode == RSLT_TRANSACTION_LIST){
//                    val transs = resultData?.getParcelableArray("trasactions")
//                    val dataset = getLabledTransactions(transs)
//
//                    val debitSeriesDPs = mutableListOf<DataPoint>()
//                    val creditSeriesDPs = mutableListOf<DataPoint>()
//                    if (dataset.size>0 && dataset[0] is String){
//                        var n = dataset.size
//                        for (i in 1..dataset.size-1){
//                            if (dataset[i] is String)
//                                n=i
//                        }
//                        val t = dataset[1] as _Transaction
//                        var date = t.day
//                        var dr = 0.0
//                        var cr = 0.0
//                        for (i in 1..n-1){
//                            val transaction = dataset[i] as _Transaction
//                            if (date != transaction.day){
//
//                                debitSeriesDPs.add(DataPoint(date?.toDouble()!!, dr))
//                                creditSeriesDPs.add(DataPoint(date?.toDouble()!!, cr))
//                                dr = 0.0
//                                cr = 0.0
//                                date = transaction.day
//
//                            }
//
//                            when(transaction.type){
//                                TRANSACTION_TYPE_DEBIT-> dr+=transaction.amount!!
//                                TRANSACTION_TYPE_DEBIT-> dr+=transaction.amount!!
//                            }
//                        }
//
//                        when(t.type){
//                            TRANSACTION_TYPE_DEBIT-> dr+=t.amount!!
//                            TRANSACTION_TYPE_DEBIT-> dr+=t.amount!!
//                        }
//                    }
//
//                    graphView.addSeries(LineGraphSeries<DataPoint>(debitSeriesDPs.toTypedArray()))
//                    graphView.addSeries(LineGraphSeries<DataPoint>(creditSeriesDPs.toTypedArray()))
//                }
//            }
//        }




//        val series = LineGraphSeries<DataPoint>(arrayOf<DataPoint>(
//                DataPoint(0.0, 1.0),
//                DataPoint(1.0, 5.0),
//                DataPoint(2.0, 3.0),
//                DataPoint(3.0, 2.0),
//                DataPoint(4.0, 6.0)))
//        graphView.addSeries(series)

        return itemView
    }

}// Required empty public constructor

class CreditFragment : Fragment() {

    var recieptImage: ByteArray? = null
    var recieptImageView: ImageView? = null

    fun startIntentForTransactionList(resultReceiver: ResultReceiver?, userId: Long?, type: String?){
        val intent = Intent(activity, TransactionListIntentService::class.java)
        intent.putExtra("rr", resultReceiver)
        intent.putExtra("userId", userId)
        intent.putExtra("type", type)
        activity?.startService(intent)
    }

    private fun updateTransactionList(resultReciever: ResultReceiver?, userId: Long?, type: String?){
        val transactionListIntent = Intent(activity?.baseContext, TransactionListIntentService::class.java)
        transactionListIntent.putExtra("rr", resultReciever)
        transactionListIntent.putExtra("userId", userId.toString())
        transactionListIntent.putExtra("type", type)
        activity?.baseContext?.startService(transactionListIntent)
    }
    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {

            val bitmap = data?.getExtras()?.get("data") as Bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            recieptImage = baos.toByteArray()

            if (recieptImageView!=null){
//                val iv = viewHolder!!.findViewById<ImageView>(R.id.imageview_reciept)
                recieptImageView!!.visibility= View.VISIBLE
                recieptImageView!!.setImageBitmap(bitmap)
            }

        }


    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val itemView = inflater!!.inflate(R.layout.fragment_credit, container, false)
        //-------------------------------------------------start------------------------------------------------------
        val recyclerview = itemView?.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview?.layoutManager = LinearLayoutManager(activity)

        val userId = activity?.intent?.getLongExtra("userId", -1)
        val currency = activity?.intent?.getStringExtra("currency")

        val resultReciever = object : ResultReceiver(Handler()){
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                when(resultCode){
                    RSLT_TRANSACTION_LIST->{
//                        val arr = resultData?.getParcelableArray("trasactions")
//                        if (arr!=null)
//                            recyclerview?.adapter = RecyclerviewAdapter(requireContext(), arr.toMutableList<Any>(), currency)
//                        else
//                            Toast.makeText(activity, "error in frag", Toast.LENGTH_SHORT).show()

                    }
                    RSLT_TRANSACTION_LIST_FAIL->{

//                        Toast.makeText(activity, "ERROR LIST", Toast.LENGTH_SHORT).show()

                    }
                    RSLT_TRANSACTION_INSERT_SUCCESS->{
                        LableTransactionsListTask(requireActivity(), recyclerview, currency).execute(userId?.toString(), TRANSACTION_TYPE_CREDIT)
//                        Toast.makeText(activity, resultData?.getLong("id", -1).toString(), Toast.LENGTH_SHORT).show()
//                        startIntentForTransactionList(this, userId, TRANSACTION_TYPE_CREDIT)
                    }
                    RSLT_TRANSACTION_INSERT_FAIL->{}
                    RSLT_LABLED_TRANSACTIONS->{
//                        Toast.makeText(activity, "labled", Toast.LENGTH_SHORT).show()
//
//                        val transs = resultData?.getParcelableArray("labled")
//
//                        if (transs!=null)
//                            recyclerview?.adapter = RecyclerviewAdapter(activity?.baseContext!!, transs.toMutableList<Any>())

                    }
                }
            }
        }
        LableTransactionsListTask(requireActivity(), recyclerview, currency).execute(userId?.toString(), TRANSACTION_TYPE_CREDIT)
//        startIntentForTransactionList(resultReciever, userId, TRANSACTION_TYPE_CREDIT)
        // Inflate the layout for this fragment

        val fab = itemView?.findViewById<FloatingActionButton>(R.id.fab)
//        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {

            recieptImage = null
            val builder = AlertDialog.Builder(activity)
            val view = LayoutInflater.from(activity).inflate(R.layout.popup_view_add_trans, null, false)
            val amountET = view.findViewById<EditText>(R.id.edittext_amount)
            val titleET = view.findViewById<EditText>(R.id.edittext_title)
            val datepickBtn = view.findViewById<Button>(R.id.btn_datepick)
            val recieptBtn = view.findViewById<Button>(R.id.btn_reciept)
            val noteET = view.findViewById<EditText>(R.id.edittext_note)
            recieptImageView = view.findViewById(R.id.imageview_reciept)
            var date = getCurrentData()
            datepickBtn.text = getDateString(date)
            recieptBtn.setOnClickListener {
                dispatchTakePictureIntent()
            }
            datepickBtn.setOnClickListener {
                val v = LayoutInflater.from(activity).inflate(R.layout.popup_view_datepicker, null, false)
                val picker = v?.findViewById<DatePicker>(R.id.picker)
                val ab = AlertDialog.Builder(activity)
                ab.setView(v)
                ab.setPositiveButton("ok", {di, id->

                    var month = picker?.month
                    date.day = picker?.dayOfMonth
                    date.month = month!!+1
                    date.year = picker?.year
                    datepickBtn.text = getDateString(date)

                })

                ab.show()
            }
            builder.setView(view)
            builder.setPositiveButton("Add to Debit", {di, id->

                val amount = amountET?.text?.toString()
                if (amount!=null && amount!="") {
                    val transaction = _Transaction(
                            amount.toDouble(),
                            titleET?.text?.toString(),
                            date?.year,
                            date?.month,
                            date?.day,
                            noteET?.text?.toString(),
                            recieptImage,
                            TRANSACTION_TYPE_CREDIT,
                            System.currentTimeMillis(),
                            userId
                    )

                    val inserTransactionIntent = Intent(activity, InsertTransactionIntentService::class.java)
                    inserTransactionIntent.putExtra("rr", resultReciever)
                    inserTransactionIntent.putExtra("transaction", transaction)
                    activity?.startService(inserTransactionIntent)
                }

            })

            builder.show()
        }

        //-------------------------------------------------end-------------------------------------------------------------
        return itemView
    }
}// Required empty public constructor

val TRANSACTION_TYPE_DEBIT = "DR"
val TRANSACTION_TYPE_CREDIT = "CR"
val TRANSACTION_TYPE_ALL = "all"
val REQUEST_IMAGE_CAPTURE = 9348
class DebitFragment : Fragment() {

    var recieptImage: ByteArray? = null
    var recieptImageView: ImageView? = null

    fun startIntentForTransactionList(resultReceiver: ResultReceiver?, userId: Long?, type: String?){
        val intent = Intent(activity, TransactionListIntentService::class.java)
        intent.putExtra("rr", resultReceiver)
        intent.putExtra("userId", userId)
        intent.putExtra("type", type)
        activity?.startService(intent)
    }

    private fun updateTransactionList(resultReciever: ResultReceiver?, userId: Long?, type: String?){
        val transactionListIntent = Intent(activity?.baseContext, TransactionListIntentService::class.java)
        transactionListIntent.putExtra("rr", resultReciever)
        transactionListIntent.putExtra("userId", userId.toString())
        transactionListIntent.putExtra("type", type)
        activity?.baseContext?.startService(transactionListIntent)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val itemView = inflater.inflate(R.layout.fragment_debit, container, false)
        val recyclerview = itemView?.findViewById<RecyclerView>(R.id.recyclerview)
        recyclerview?.layoutManager = LinearLayoutManager(activity)

        val userId = activity?.intent?.getLongExtra("userId", -1)
        val currency = activity?.intent?.getStringExtra("currency")

        val resultReciever = object : ResultReceiver(Handler()){
            override fun onReceiveResult(resultCode: Int, resultData: Bundle?) {
                when(resultCode){
                    RSLT_TRANSACTION_LIST->{
//                        val arr = resultData?.getParcelableArray("trasactions")
//                      if (arr!=null)
//                        recyclerview?.adapter = RecyclerviewAdapter(requireContext(), arr.toMutableList<Any>(), currency)
//                        else
//                            Toast.makeText(activity, "error in frag", Toast.LENGTH_SHORT).show()

                    }
                    RSLT_TRANSACTION_LIST_FAIL->{

                        Toast.makeText(activity, "ERROR LIST", Toast.LENGTH_SHORT).show()

                    }
                    RSLT_TRANSACTION_INSERT_SUCCESS->{
                        LableTransactionsListTask(requireActivity(), recyclerview, currency).execute(userId?.toString(), TRANSACTION_TYPE_DEBIT)

//                        Toast.makeText(activity, resultData?.getLong("id", -1).toString(), Toast.LENGTH_SHORT).show()
//                        startIntentForTransactionList(this, userId, TRANSACTION_TYPE_DEBIT)
                    }
                    RSLT_TRANSACTION_INSERT_FAIL->{}
                    RSLT_LABLED_TRANSACTIONS->{
//                        Toast.makeText(activity, "labled", Toast.LENGTH_SHORT).show()
//
//                        val transs = resultData?.getParcelableArray("labled")
//
//                        if (transs!=null)
//                            recyclerview?.adapter = RecyclerviewAdapter(activity?.baseContext!!, transs.toMutableList<Any>())

                    }
                }
            }
        }
        LableTransactionsListTask(requireActivity(), recyclerview, currency).execute(userId?.toString(), TRANSACTION_TYPE_DEBIT)
//        startIntentForTransactionList(resultReciever, userId, TRANSACTION_TYPE_DEBIT)
        // Inflate the layout for this fragment

        val fab = itemView?.findViewById<FloatingActionButton>(R.id.fab)
//        val fab = activity?.findViewById<FloatingActionButton>(R.id.fab)
        fab?.setOnClickListener {

            recieptImage = null
            val builder = AlertDialog.Builder(activity)
            val view = LayoutInflater.from(activity).inflate(R.layout.popup_view_add_trans, null, false)
            val amountET = view.findViewById<EditText>(R.id.edittext_amount)
            val titleET = view.findViewById<EditText>(R.id.edittext_title)
            val datepickBtn = view.findViewById<Button>(R.id.btn_datepick)
            val recieptBtn = view.findViewById<Button>(R.id.btn_reciept)
            val noteET = view.findViewById<EditText>(R.id.edittext_note)
            recieptImageView = view.findViewById(R.id.imageview_reciept)
            var date = getCurrentData()
            datepickBtn.text = getDateString(date)
            recieptBtn.setOnClickListener {
                dispatchTakePictureIntent()
            }
            datepickBtn.setOnClickListener {
                val v = LayoutInflater.from(activity).inflate(R.layout.popup_view_datepicker, null, false)
                val picker = v?.findViewById<DatePicker>(R.id.picker)
                val ab = AlertDialog.Builder(activity)
                ab.setView(v)
                ab.setPositiveButton("ok", {di, id->

                    var month = picker?.month
                    date.day = picker?.dayOfMonth
                    date.month = month!!+1
                    date.year = picker?.year
                    datepickBtn.text = getDateString(date)

                })

                ab.show()
            }
            builder.setView(view)
            builder.setPositiveButton("Add to Debit", {di, id->

                val amount = amountET?.text?.toString()
                if (amount!=null && amount!="") {
                    val transaction = _Transaction(
                            amount.toDouble(),
                            titleET?.text?.toString(),
                            date?.year,
                            date?.month,
                            date?.day,
                            noteET?.text?.toString(),
                            recieptImage,
                            TRANSACTION_TYPE_DEBIT,
                            System.currentTimeMillis(),
                            userId
                    )

                    val inserTransactionIntent = Intent(activity, InsertTransactionIntentService::class.java)
                    inserTransactionIntent.putExtra("rr", resultReciever)
                    inserTransactionIntent.putExtra("transaction", transaction)
                    activity?.startService(inserTransactionIntent)
                }

            })

            builder.show()
        }
        return itemView
    }



    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(activity?.packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_IMAGE_CAPTURE) {

             val bitmap = data?.getExtras()?.get("data") as Bitmap
            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
            recieptImage = baos.toByteArray()

            if (recieptImageView!=null){
//                val iv = viewHolder!!.findViewById<ImageView>(R.id.imageview_reciept)
                recieptImageView!!.visibility= View.VISIBLE
                recieptImageView!!.setImageBitmap(bitmap)
            }

        }


    }

}// Required empty public constructor
