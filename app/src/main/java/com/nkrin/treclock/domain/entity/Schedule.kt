package com.nkrin.treclock.domain.entity

data class Schedule(
    var id: Int,
    var name: String,
    var comment: String,
    var steps: MutableList<Step>
)