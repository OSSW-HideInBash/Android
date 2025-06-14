package com.hideinbash.tododudu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class MonsterAdapter(
    private var items: List<Todo>,
    private val onItemClick: (Todo) -> Unit
) : RecyclerView.Adapter<MonsterAdapter.MonsterViewHolder>() {

    // 완료 예정 상태인 아이템들의 ID를 저장
    private val completingItems = mutableSetOf<Long>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monster, parent, false)
        return MonsterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonsterAdapter.MonsterViewHolder, position: Int) {
        val item = items[position]

        // 완료 예정 상태인지 확인
        val isCompleting = completingItems.contains(item.id)

        val gifUrl = if (isCompleting) {
            // 완료 상태 이미지 (priority별로 구분)
            when(item.priority) {

                //로봇사진 점프 2
                1 -> "https://animatedoss.s3.amazonaws.com/14563cf1-a95b-4110-a12c-4f0ee8f36fd8/video.gif"
                //늑대 점프
                2 -> "https://animatedoss.s3.amazonaws.com/83d582af-9ba4-459c-85c6-d7548962b026/video.gif"
                //고블린 사진 점프 2
                3 -> "https://animatedoss.s3.amazonaws.com/0e035a8a-17b1-4908-a5a7-23f9ec03dfd7/video.gif"
                else -> "https://animatedoss.s3.amazonaws.com/fad01384-aeea-4539-946e-025387d43e81/video.gif"
            }
        } else {
            // 일반 상태 이미지 (priority별로 구분)
            when(item.priority) {
                //로봇 ㅎㅇ idx 4
                1 -> "https://animatedoss.s3.amazonaws.com/59e26adf-ecd7-40e6-b319-1a5510dd42e6/video.gif"
                //늑대 하이
                2 -> "https://animatedoss.s3.amazonaws.com/4f1b8958-b8c1-4726-b7b5-f907fb72e748/video.gif"
                //고블린 ㅎㅇ 4
                3 -> "https://animatedoss.s3.amazonaws.com/19d34b1f-4bfc-47b3-aee0-6fc78e7fb968/video.gif"
                else -> "https://animatedoss.s3.amazonaws.com/59e26adf-ecd7-40e6-b319-1a5510dd42e6/video.gif"
            }
        }

        Glide.with(holder.itemView.context)
            .asGif()
            .load(gifUrl)
            .into(holder.monsterIv)

        // 아이템 클릭 리스너 설정 (완료 예정 상태가 아닐 때만)
        holder.monsterIv.setOnClickListener {
            if (!isCompleting) {
                onItemClick(item)
            }
        }
    }

    override fun getItemCount() = items.size

    class MonsterViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monsterIv: ImageView = view.findViewById(R.id.monster_iv)
    }

    fun updateList(newItems: List<Todo>) {
        items = newItems
        notifyDataSetChanged()
    }

    // 특정 아이템을 완료 예정 상태로 설정
    fun setItemCompleting(todoId: Long) {
        completingItems.add(todoId)
        // 해당 아이템의 위치를 찾아서 업데이트
        val position = items.indexOfFirst { it.id == todoId }
        if (position != -1) {
            notifyItemChanged(position)
        }
    }

    // 완료 예정 상태 해제 (실제 완료 후 목록에서 제거될 때)
    fun removeCompletingItem(todoId: Long) {
        completingItems.remove(todoId)
    }
}