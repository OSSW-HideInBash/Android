package com.hideinbash.tododudu

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class MonsterAdapter(private var items: List<Todo>) :
    RecyclerView.Adapter<MonsterAdapter.MonsterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonsterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_monster, parent, false)
        return MonsterViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonsterAdapter.MonsterViewHolder, position: Int) {
        val item = items[position]

        val monsterRes = when(item.priority) {
            1 -> R.drawable.monster_1
            2 -> R.drawable.monster_2
            3 -> R.drawable.monster_3
            else -> R.drawable.monster_3 // 기본 몬스터 이미지
        }
        holder.monsterIv.setImageResource(monsterRes)

        // 좌우 랜덤 위치 지정
        val layoutParams = holder.monsterIv.layoutParams as ConstraintLayout.LayoutParams
        layoutParams.horizontalBias = (0..100).random() / 100f
        holder.monsterIv.layoutParams = layoutParams
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