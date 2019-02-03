package com.nkrin.treclock.view.alarm

object AlarmPlayer {

    private val alarms: MutableMap<String, MutableList<Alarm>> = mutableMapOf()

    fun setUp(type: String, alarm: Alarm) {
        if (alarms[type] == null) {
            alarms[type] = mutableListOf()
        }
        alarms[type]?.add(alarm)
        alarm.setUp()
    }

    fun cancel(type: String) {
        alarms[type]?.forEach { it.cancel() }
    }

    fun createNewRequestCode() : Int {
        val max = alarms.values.flatten().maxBy { it.requestCode }?.requestCode ?: 0
        return max + 1
    }
}