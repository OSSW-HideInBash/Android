package com.hideinbash.tododudu

data class Todo(
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int, // 우선 순위 (1~3)
    val date: String, // 날짜 (YYYY-MM-DD 형식)
    val isCompleted: Boolean = false
)