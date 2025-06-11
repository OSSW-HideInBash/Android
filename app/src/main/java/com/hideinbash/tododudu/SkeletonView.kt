package com.hideinbash.tododudu

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.min


class SkeletonView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    data class Joint(
        val name: String,
        var parent: String?,
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

    // 원본 크기(뼈대 세우기 용
    private var originalWidth = 497f
    private var originalHeight = 614f

    // 동적 계산
    private var scale = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    private var jointsInitialized = false

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // View 크기에 맞게 비율 계산
        scale = min(800 / 497f, 800 / 614f)
        offsetX = (800 - 497f * scale) / 2f
        offsetY = (800 - 614f * scale) / 2f

        // joints 한 번만 초기화
        if (!jointsInitialized) {
            val rawJoints = listOf(
                Triple("root", null, floatArrayOf(278f, 446f)),
                Triple("torso", "root", floatArrayOf(278f, 191f)),
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

            joints.clear()
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
            jointsInitialized = true
        }
    }

    fun setOriginalImageSize(width: Int, height: Int) {
        originalWidth = width.toFloat()
        originalHeight = height.toFloat()
        //호출할 필요없이 기존의 노드를 역설
        //jointsInitialized = false // 사이즈 바뀌었으니 다시 초기화 필요
        Log.d("이미지 크기 확인","width : ${width}, height : ${height}")
        //requestLayout() // onSizeChanged 호출 유도
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

    fun getSkeletonJson(): JSONObject {
        val skeletonArray = JSONArray()
        var tempJoint = joints
        tempJoint.add(1, Joint("hip","root",joints.get(0).x,joints.get(0).y,14f))
        //다시 hip으로 바꿔줌
        val torso = tempJoint.find { it.name == "torso" }
        torso?.parent = "hip"
        for (joint in joints) {
            //현재 뷰의 길이가 800px로 잡힘
            //기존 노드 * (이미지 원본 길이/뷰 길이) := scale
            //서버에서 처리해가지구 안하기로함
            val rawX = (joint.x)
            val rawY = (joint.y)

            val jointObject = JSONObject()
            jointObject.put("name", joint.name)
            jointObject.put("parent", joint.parent ?: JSONObject.NULL) // null 그대로 넣어도 됨
            jointObject.put("loc", JSONArray(listOf(rawX, rawY)))

            skeletonArray.put(jointObject)
        }

        val result = JSONObject()
        result.put("skeleton", skeletonArray)
        return result
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
                    Log.d("joint_info", "(${it.x}, ${it.y})")
                }
            }
            MotionEvent.ACTION_UP -> {
                selectedJoint = null
            }
        }
        return true
    }
}
