package com.callcentre.callcentre

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import com.callcentre.callcentre.model.Operator
import kotlinx.android.synthetic.main.operator_item.view.*
import java.util.Comparator

/**
 * Created by Nikolay on 12.12.2017.
 */
class OperatorAdapter : RecyclerView.Adapter<OperatorAdapter.ViewHolder>() {

    private val data: MutableList<Operator> = mutableListOf()

    private var totalProfit: Int = 0


    override fun onBindViewHolder(holder: ViewHolder?, position: Int) {
        val operator = data[position]

        holder?.let {
            it.operatorName.text = "Operator № ${operator.id}"
            it.operatorAnsweredCalls.text = "Answered calls: ${operator.answeredCallsCount}"
            it.operatorRating.rating = calculateOperatorProfit(operator)
            it.operatorProfit.text = "Profit : $${operator.profit}"
        }
    }

    class OperatorComparator : Comparator<Operator> {
        override fun compare(p0: Operator, p1: Operator): Int {
            return p1.answeredCallsCount.minus(p0.answeredCallsCount)
        }

    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent!!.context).inflate(R.layout.operator_item, parent, false)
        return ViewHolder(view)
    }

    fun setOperators(operators: MutableList<Operator>) {
        data.clear()
        data.addAll(operators)
        data.sortWith(OperatorComparator())
        notifyDataSetChanged()
    }

    fun setTotalProfit(profit: Int) {
        totalProfit = profit
    }

    fun calculateOperatorProfit(operator: Operator) :Float {
        return (operator.profit*100f)/(totalProfit*10)
    }


    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val operatorName: TextView = view.operatorName
        val operatorProfit: TextView = view.operatorProfit
        val operatorAnsweredCalls: TextView = view.operatorAnsweredCalls
        val operatorRating: RatingBar = view.operatorRating
    }

}