package com.callcentre.callcentre

import android.content.Intent
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.callcentre.callcentre.model.Call
import com.callcentre.callcentre.model.Operator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val operators: Stack<Operator> = Stack()
    private val operatorsList:MutableList<Operator> = mutableListOf()
    private val callQueue: Queue<Call> = LinkedBlockingQueue()
    private var callPrice = 300
    private var callDuration = 2L
    private var callPeriod = 1L

    private var callId = 0

    private var workingDayDuration = 20L
    private var operatorsCount = 0
    private var totalProfit = 0

    var isStillWorkingDay = true

    var adapter:Adapter = Adapter()
    var operatorsAdapter = OperatorAdapter()

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener{
            onBtnStartClick()
        }

        recyclerView.adapter = adapter
        adapter.recyclerView = recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        operatorsRecyclerView.adapter = operatorsAdapter
        operatorsRecyclerView.layoutManager = LinearLayoutManager(this)

        fabInfo.setOnClickListener {
            showInfo()
        }

        fabOperators.setOnClickListener {
            showOperators()
        }
    }

    private fun showInfo(){
         val unusedOperatorsCount = operators.count { it.answeredCallsCount == 0 }

           val intent = Intent(this, DetailsActivity::class.java)
           intent.putExtra(DetailsActivity.TOTAL_PROFIT,totalProfit)
           intent.putExtra(DetailsActivity.UNUSED_OPERATORS,unusedOperatorsCount)
           intent.putExtra(DetailsActivity.MISSED_CALLS_COUNT,callQueue.size)
           intent.putExtra(DetailsActivity.CALL_PRICE,callPrice)
           startActivity(intent)
    }

    private fun showOperators(){
        operatorsAdapter.setOperators(operatorsList)
        operatorsAdapter.setTotalProfit(totalProfit)

        btnStart.visibility = View.GONE
        initialRoot.visibility = View.GONE

        recyclerView.visibility = View.GONE
        progressBar.visibility = View.GONE

        operatorsRecyclerView.visibility = View.VISIBLE
        operatorsAdapter.notifyDataSetChanged()

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun getRandomNumber(max:Long) :Long {
        return ThreadLocalRandom.current().nextLong(1,max+1)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onBtnStartClick() {
        this.workingDayDuration = workingDayDurationEt.text.toString().toLong()
        this.callDuration = callDurationEt.text.toString().toLong()
        this.callPeriod = callFrequencyEt.text.toString().toLong()
        this.operatorsCount = operatorsCountEt.text.toString().toInt()
        this.callPrice = callPriceEt.text.toString().toInt()

        btnStart.visibility = View.GONE
        initialRoot.visibility = View.GONE

        recyclerView.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE

        startSimulate()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun startSimulate() {
        generateOperators(operatorsCount)

        val mainTimer = Observable.timer(workingDayDuration, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        mainTimer.subscribe {
            isStillWorkingDay = false

            adapter.add("Working day finished")
            /*showInfo.visibility = View.VISIBLE*/
            fabInfo.visibility = View.VISIBLE
            fabOperators.visibility = View.VISIBLE
            progressBar.visibility = View.GONE
        }

        Observable.interval(callPeriod, TimeUnit.SECONDS)
                .timeInterval()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .takeUntil(mainTimer)
                .subscribe {
                    callId++
                    val newCall = Call(callId, callPrice, callDuration)
                    adapter.add( "New call received with id : ${newCall.id}")
                    onNewCall(newCall)
                }
    }

    private fun generateOperators(count: Int) {
        (0 until count)
                .map { Operator(it + 1) }
                .forEach {
                    operatorsList.add(it)
                    operators.push(it)
                }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onNewCall(call: Call) {
        if (operators.isNotEmpty()) {
            answerOnCall(operators.pop(), call)
        } else {
            adapter.add("Queue: Call ${call.id} moved to queue")
            callQueue.add(call)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun answerOnCall(operator: Operator, call: Call) {
        adapter.add("Answered: Operator ${operator.id} answered on call ${call.id}")
        Observable.timer(getRandomNumber(callDuration), TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    onCallWithOperatorFinished(operator, call)
                }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun onCallWithOperatorFinished(operator: Operator, call: Call) {
        adapter.add( "=== Call ${call.id} finished! ===")
        totalProfit += call.price
        operator.profit+=call.price
        operator.answeredCallsCount++
        if (callQueue.isNotEmpty() && isStillWorkingDay) {
            val newCall = callQueue.poll()
            answerOnCall(operator, newCall)
        } else {
            operators.push(operator)
        }
    }

    private fun log(message: String) {
        Log.e("Tag", message)
    }

}
