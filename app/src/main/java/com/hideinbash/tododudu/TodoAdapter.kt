package com.hideinbash.tododudu

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TodoAdapter(
    private var items: List<Todo>,
    private val onEdit: (Todo) -> Unit,
    private val onToggleComplete: (Todo) -> Unit,
    private val onDelete: (Todo) -> Unit
) : RecyclerView.Adapter<TodoAdapter.TodoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TodoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_todo, parent, false)
        return TodoViewHolder(view)
    }

    override fun onBindViewHolder(holder: TodoViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.desc.text = item.description
        holder.orderText.text = "${item.priority}순위"

        // priority에 따라 색 변경
        when (item.priority) {
            1 -> {
                holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.first_red_bg))
                holder.orderIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.first_red))
            }
            2 -> {
                holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.second_blue_bg))
                holder.orderIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.second_blue))
            }
            3 -> {
                holder.itemView.setBackgroundColor(holder.itemView.context.getColor(R.color.third_yellow_bg))
                holder.orderIcon.setColorFilter(ContextCompat.getColor(holder.itemView.context, R.color.third_yellow))
            }
        }

        // 아이콘 클릭 리스너 설정 (완료처리)
        holder.orderIcon.setOnClickListener {
            onToggleComplete(item)
        }

        // 수정 아이콘 클릭 리스너 설정
        holder.editIcon.setOnClickListener {
            onEdit(item)
        }

        // 삭제 아이콘 클릭 리스너 설정
        holder.deleteIcon.setOnClickListener {
            onDelete(item)
        }
    }

    override fun getItemCount() = items.size

    class TodoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val orderIcon: ImageView = view.findViewById(R.id.todo_item_order_iv)
        val orderText: TextView = view.findViewById(R.id.todo_item_order_tv)
        val editIcon: ImageView = view.findViewById(R.id.todo_item_fix_iv)
        val deleteIcon: ImageView = view.findViewById(R.id.todo_item_delete_iv)
        val title: TextView = view.findViewById(R.id.todo_item_title_tv)
        val desc: TextView = view.findViewById(R.id.todo_item_content_tv)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateList(newItems: List<Todo>) {
        items = newItems
        notifyDataSetChanged()
    }
}