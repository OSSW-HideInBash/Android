package com.hideinbash.tododudu

import android.util.Log
import androidx.recyclerview.widget.RecyclerView

/**
 * 몬스터를 자연스럽게 배치하는 커스텀 LayoutManager.
 * - pattern: (x, y) 비율 패턴 리스트 (0~1 권장)
 * - itemWidth, itemHeight: 각 아이템의 크기(px)
 * - verticalSpacing: 패턴 그룹 간 세로 간격(px)
 */
class MonsterPatternLayoutManager(
    private val pattern: List<Pair<Float, Float>>,
    private val itemWidth: Int,
    private val itemHeight: Int,
    private val verticalSpacing: Int = 20
) : RecyclerView.LayoutManager() {

    private var verticalOffset = 0

    override fun generateDefaultLayoutParams(): RecyclerView.LayoutParams =
        RecyclerView.LayoutParams(itemWidth, itemHeight)

    override fun canScrollVertically(): Boolean = true

    override fun onLayoutChildren(recycler: RecyclerView.Recycler, state: RecyclerView.State) {
        detachAndScrapAttachedViews(recycler)
        if (itemCount == 0) return

        val parentWidth = width
        val patternSize = pattern.size
        val groupHeight = height / 2 + verticalSpacing

        // 전체 그룹(패턴 반복 묶음) 수와 전체 높이 계산
        val totalGroups = (itemCount + patternSize - 1) / patternSize
        val totalHeight = totalGroups * groupHeight

        // 아래(리스트 끝)부터 아이템을 배치
        for (i in 0 until itemCount) {
            val reverseIndex = itemCount - 1 - i
            val patternIndex = reverseIndex % patternSize
            val groupIndex = reverseIndex / patternSize

            val (xRatio, yRatio) = pattern[patternIndex]
            val left = (parentWidth * xRatio).toInt() - itemWidth / 2
            val top = totalHeight - groupHeight * (groupIndex + 1) + (groupHeight * yRatio).toInt() - verticalOffset - 200

            val view = recycler.getViewForPosition(i)
            addView(view)
            measureChildWithMargins(view, 0, 0)
            layoutDecorated(view, left, top, left + itemWidth, top + itemHeight)
        }
    }

    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler, state: RecyclerView.State): Int {
        val parentHeight = height
        val patternSize = pattern.size
        val groupHeight = itemHeight * 2 + verticalSpacing
        val totalGroups = (itemCount + patternSize - 1) / patternSize
        val totalHeight = totalGroups * groupHeight

        val maxOffset = totalHeight - parentHeight
        val newOffset = (verticalOffset + dy).coerceIn(0, maxOffset.coerceAtLeast(0))
        val delta = newOffset - verticalOffset
        verticalOffset = newOffset
        offsetChildrenVertical(-delta)
        requestLayout()
        return delta
    }
}
