package com.hideinbash.tododudu

import androidx.room.Dao
import androidx.room.*

@Dao
interface TodoDao {
    @Insert
    suspend fun insertTodo(todo: Todo)

    @Update
    suspend fun updateTodo(todo: Todo)

    @Delete
    suspend fun deleteTodo(todo: Todo)

    @Query("SELECT * FROM todo WHERE date = :date ORDER BY priority ASC")
    suspend fun getTodosByDate(date: String): List<Todo>

    @Query("SELECT * FROM todo WHERE date = :date AND isCompleted = 0 ORDER BY priority DESC")
    suspend fun getYetTodosByDate(date: String): List<Todo>

    @Query("SELECT * FROM todo WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTodosBetweenDates(startDate: String, endDate: String): List<Todo>
}