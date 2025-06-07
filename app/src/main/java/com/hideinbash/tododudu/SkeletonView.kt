package com.hideinbash.tododudu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.min


class SkeletonView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    data class Joint(
        val name: String,
        val parent: String?,
        var x: Float,
        var y: Float,
        val radius: Float = 14f
    ) {
        fun containsPoint(px: Float, py: Float): Boolean {
            val dx = px - x
            val dy = py - y
            return dx * dx + dy * dy <= radius * radius
        }
    }

    private val nodePaint = Paint().apply {
        color = Color.BLUE
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val edgePaint = Paint().apply {
        color = Color.GRAY
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var selectedJoint: Joint? = null
    private val joints = mutableListOf<Joint>()

    init {
        // 원본 이미지 크기
        val originalWidth = 497f
        val originalHeight = 614f

        // View 크기 기준 (400dp x 400dp)
        val targetSize = 400f
        val scale = min(targetSize / originalWidth, targetSize / originalHeight)
        val offsetX = (targetSize - originalWidth * scale) / 2f
        val offsetY = (targetSize - originalHeight * scale) / 2f

        val rawJoints = listOf(
            Triple("root", null, floatArrayOf(278f, 446f)),
            Triple("hip", "root", floatArrayOf(278f, 446f)),
            Triple("torso", "hip", floatArrayOf(278f, 191f)),
            Triple("neck", "torso", floatArrayOf(278f, 258f)),
            Triple("right_shoulder", "torso", floatArrayOf(142f, 200f)),
            Triple("right_elbow", "right_shoulder", floatArrayOf(96f, 161f)),
            Triple("right_hand", "right_elbow", floatArrayOf(58f, 123f)),
            Triple("left_shoulder", "torso", floatArrayOf(414f, 181f)),
            Triple("left_elbow", "left_shoulder", floatArrayOf(439f, 136f)),
            Triple("left_hand", "left_elbow", floatArrayOf(459f, 84f)),
            Triple("right_hip", "root", floatArrayOf(193f, 446f)),
            Triple("right_knee", "right_hip", floatArrayOf(181f, 517f)),
            Triple("right_foot", "right_knee", floatArrayOf(168f, 582f)),
            Triple("left_hip", "root", floatArrayOf(362f, 446f)),
            Triple("left_knee", "left_hip", floatArrayOf(375f, 511f)),
            Triple("left_foot", "left_knee", floatArrayOf(394f, 569f)),
        )

        joints.addAll(
            rawJoints.map { (name, parent, loc) ->
                Joint(
                    name = name,
                    parent = parent,
                    x = loc[0] * scale + offsetX,
                    y = loc[1] * scale + offsetY
                )
            }
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // 선 그리기 (부모와 연결)
        for (joint in joints) {
            joint.parent?.let { parentName ->
                val parent = joints.find { it.name == parentName }
                parent?.let {
                    canvas.drawLine(joint.x, joint.y, it.x, it.y, edgePaint)
                }
            }
        }

        // 노드 원 그리기
        for (joint in joints) {
            canvas.drawCircle(joint.x, joint.y, joint.radius, nodePaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                selectedJoint = joints.findLast { it.containsPoint(event.x, event.y) }
            }
            MotionEvent.ACTION_MOVE -> {
                selectedJoint?.let {
                    it.x = event.x
                    it.y = event.y
                    invalidate()
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedJoint = null
            }
        }
        return true
    }
}
