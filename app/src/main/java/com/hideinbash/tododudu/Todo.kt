package com.hideinbash.tododudu

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "todo")
data class Todo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String = "",
    val priority: Int, // 우선 순위 (1~3)
    val date: String, // 날짜 (YYYY-MM-DD 형식)
    val isCompleted: Boolean = false
) : Serializable