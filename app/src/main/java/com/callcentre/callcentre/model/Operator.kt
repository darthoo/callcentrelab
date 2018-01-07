package com.callcentre.callcentre.model


/**
 * Created by Nikolay on 12.12.2017.
 */
data class Operator (
        val id:Int,
        var answeredCallsCount :Int = 0,
        var profit:Int = 0
)