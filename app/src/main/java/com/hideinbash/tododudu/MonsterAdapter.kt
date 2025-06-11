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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monster, parent, false)
        return MonsterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonsterAdapter.MonsterViewHolder, position: Int) {
        val item = items[position]

        // 아이템의 우선순위에 따라 몬스터 이미지 glide 사용해서 url 가져와서 설정

        val gifUrl = when(item.priority) {
            1 -> "https://animatedoss.s3.amazonaws.com/59e26adf-ecd7-40e6-b319-1a5510dd42e6/video.gif"
            2 -> "https://animatedoss.s3.amazonaws.com/14563cf1-a95b-4110-a12c-4f0ee8f36fd8/video.gif"
            3 -> "https://animatedoss.s3.amazonaws.com/da32ac1c-a684-49aa-9ef3-0a9ca1259612/video.gif"
            else -> "https://animatedoss.s3.amazonaws.com/59e26adf-ecd7-40e6-b319-1a5510dd42e6/video.gif" // 기본 몬스터 이미지 URL
        }
        Glide.with(holder.itemView.context)
            .asGif()
            .load(gifUrl)
            .into(holder.monsterIv)

        // 아이템 클릭 리스너 설정
        holder.monsterIv.setOnClickListener {
            onItemClick(item)
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
}