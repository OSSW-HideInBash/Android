package com.hideinbash.tododudu

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM todo ORDER BY date ASC, priority ASC")
    fun getAllTodos(): LiveData<List<Todo>>     // LiveData로 변경하여 UI에서 자동으로 업데이트 가능

    @Query("SELECT * FROM todo ORDER BY date DESC, priority ASC")
    suspend fun getAllTodosRaw(): List<Todo>
    // LiveData가 아니라 List<Todo>를 반환하는 함수

    @Query("SELECT * FROM todo WHERE isCompleted = 0 ORDER BY date DESC, priority ASC")
    suspend fun getIncompleteTodos(): List<Todo>

    @Query("SELECT * FROM todo WHERE isCompleted = 1 ORDER BY date DESC, priority ASC")
    suspend fun getCompletedTodos(): List<Todo>
}