package com.hideinbash.tododudu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(private val items: List<Todo>) :
    RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.desc.text = item.description
        // 우선순위 표시 (예시)
        holder.orderText.text = "${item.priority}순위"
        // 필요 시 날짜, 완료 여부 등 추가 표시 가능
    }

    override fun getItemCount() = items.size

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderIcon: ImageView = view.findViewById(R.id.todo_item_order_iv)
        val orderText: TextView = view.findViewById(R.id.todo_item_order_tv)
        val editIcon: ImageView = view.findViewById(R.id.todo_item_fix_iv)
        val title: TextView = view.findViewById(R.id.todo_item_title_tv)
        val desc: TextView = view.findViewById(R.id.todo_item_content_tv)
    }
}