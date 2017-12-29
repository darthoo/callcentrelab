package com.callcentre.callcentre

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_details.*

class DetailsActivity : AppCompatActivity() {

    companion object {
        const val TOTAL_PROFIT = "totalProfit"
        const val UNUSED_OPERATORS = "UNUSED_OPERATORS"
        const val MISSED_CALLS_COUNT = "missedCallCount"
        const val CALL_PRICE = "callPrice"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        totalProfit.text = "Total profit: ${intent.getIntExtra(TOTAL_PROFIT,0)}"

        unusedOperators.text = "Unused operators ${intent.getIntExtra(UNUSED_OPERATORS,0)}"

        val missedCallsCount = intent.getIntExtra(MISSED_CALLS_COUNT,0)
        missedCalls.text =  "Missed calls: $missedCallsCount"

        val callPrice = intent.getIntExtra(CALL_PRICE,0)
        val missedProfitCount = missedCallsCount*(callPrice)
        missedProfit.text = "Missed profit: $missedProfitCount"

        val totalProfitCount = intent.getIntExtra(TOTAL_PROFIT,0)

        val monthProfitCount = totalProfitCount*(20)

        profitForMonth.text = "Profit for month: $monthProfitCount"

    }
}
