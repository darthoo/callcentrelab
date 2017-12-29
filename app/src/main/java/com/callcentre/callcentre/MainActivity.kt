package com.callcentre.callcentre

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
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
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private val operators: Stack<Operator> = Stack()
    private val callQueue: Queue<Call> = LinkedBlockingQueue()
    private var callPrice = 300
    private var callDuration = 5L
    private var callPeriod = 1L

    private var callId = 0

    private var workingDayDuration = 20L
    private var operatorsCount = 0
    private var totalProfit = 0

    var isStillWorkingDay = true

    var adapter:Adapter = Adapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btnStart.setOnClickListener{
            onBtnStartClick()
        }

        recyclerView.adapter = adapter
        adapter.recyclerView = recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)

        showInfo.setOnClickListener{
            val unusedOperatorsCount = operators.count { it.answeredCallsCount == 0 }

            val intent = Intent(this, DetailsActivity::class.java)
            intent.putExtra(DetailsActivity.TOTAL_PROFIT,totalProfit)
            intent.putExtra(DetailsActivity.UNUSED_OPERATORS,unusedOperatorsCount)
            intent.putExtra(DetailsActivity.MISSED_CALLS_COUNT,callQueue.size)
            intent.putExtra(DetailsActivity.CALL_PRICE,callPrice)
            startActivity(intent)
        }
    }

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

    private fun startSimulate() {
        generateOperators(operatorsCount)

        val mainTimer = Observable.timer(workingDayDuration, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        mainTimer.subscribe {
            isStillWorkingDay = false

            adapter.add("Working day finished")
            showInfo.visibility = View.VISIBLE
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
                .forEach { operators.push(it) }
    }

    private fun onNewCall(call: Call) {
        if (operators.isNotEmpty()) {
            answerOnCall(operators.pop(), call)
        } else {
            adapter.add("Queue: Call ${call.id} moved to queue")
            callQueue.add(call)
        }
    }

    private fun answerOnCall(operator: Operator, call: Call) {
        adapter.add("Answered: Operator ${operator.id} answered on call ${call.id}")
        Observable.timer(call.duration, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    operator.answeredCallsCount+=1
                    onCallWithOperatorFinished(operator, call)
                }
    }

    private fun onCallWithOperatorFinished(operator: Operator, call: Call) {
        adapter.add( "=== Call ${call.id} finished! ===")
        totalProfit += call.price
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
