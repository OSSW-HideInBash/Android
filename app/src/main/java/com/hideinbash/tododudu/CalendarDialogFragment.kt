package com.hideinbash.tododudu

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.hideinbash.tododudu.databinding.FragmentCalendarDialogBinding
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate

class CalendarDialogFragment(
    private val selectedDate: LocalDate,
    private val onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) : DialogFragment() {

    private var _binding: FragmentCalendarDialogBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCalendarDialogBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val calendarView = binding.calendarView

        // 캘린더 초기 설정
        calendarView.currentDate = CalendarDay.from(
            selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth
        )
        val initialMonth = binding.calendarView.currentDate
        loadAndDecorateMonth(initialMonth.year, initialMonth.month)

        // 선택된 날짜 표시
        calendarView.selectedDate = CalendarDay.from(
            selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth
        )

        // 날짜 선택 콜백
        calendarView.setOnDateChangedListener { _, date, _ ->
            onDateSelected(date.year, date.month, date.day)
            dismiss()
        }

        // 월 변경 리스너
        binding.calendarView.setOnMonthChangedListener { _, date ->
            val year = date.year
            val month = date.month
            loadAndDecorateMonth(year, month)
        }
    }

    private fun loadAndDecorateMonth(year: Int, month: Int) {
        val firstDay = LocalDate.of(year, month, 1)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())

        val today = CalendarDay.today()
        val redColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.button_yellow)


        CoroutineScope(Dispatchers.IO).launch {
            val db = TodoDatabase.getInstance(requireContext())
            val todos = db.todoDao().getTodosBetweenDates(
                firstDay.toString(), lastDay.toString()
            )
            val daysWithYetTodos = todos.filter { !it.isCompleted }
                .map { it.date }
                .toSet()

            withContext(Dispatchers.Main) {
                binding.calendarView.removeDecorators()     // 기존 decorator 제거

                // 오늘 날짜 빨간 글씨 decorator
                binding.calendarView.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean = day == today
                    override fun decorate(view: DayViewFacade) {
                        view.addSpan(android.text.style.ForegroundColorSpan(redColor))
                    }
                })

                // 선택된 날짜 노란 배경 decorator
                binding.calendarView.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean =
                        day.year == selectedDate.year &&
                                day.month == selectedDate.monthValue &&
                                day.day == selectedDate.dayOfMonth

                    override fun decorate(view: DayViewFacade) {
                        view.addSpan(android.text.style.BackgroundColorSpan(yellowColor))
                    }
                })

                // 할 일이 있는 날짜에 노란 점 decorator
                binding.calendarView.addDecorator(object : DayViewDecorator {
                    override fun shouldDecorate(day: CalendarDay): Boolean {
                        val dateStr = "%04d-%02d-%02d".format(day.year, day.month, day.day)
                        return daysWithYetTodos.contains(dateStr)
                    }
                    override fun decorate(view: DayViewFacade) {
                        view.addSpan(
                            com.prolificinteractive.materialcalendarview.spans.DotSpan(
                                8f,
                                ContextCompat.getColor(requireContext(), R.color.button_yellow)
                            )
                        )
                    }
                })
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}