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

        // 선택된 날짜 표시
        calendarView.selectedDate = CalendarDay.from(
            selectedDate.year, selectedDate.monthValue, selectedDate.dayOfMonth
        )

        val redColor = ContextCompat.getColor(requireContext(), android.R.color.holo_red_dark)
        val yellowColor = ContextCompat.getColor(requireContext(), R.color.button_yellow)
        val today = CalendarDay.today()

        // 오늘 날짜 빨간 글씨 decorator
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean = day == today
            override fun decorate(view: DayViewFacade) {
                view.addSpan(android.text.style.ForegroundColorSpan(redColor))
            }
        })

        // 선택된 날짜 노란 배경 decorator
        calendarView.addDecorator(object : DayViewDecorator {
            override fun shouldDecorate(day: CalendarDay): Boolean =
                day.year == selectedDate.year &&
                        day.month == selectedDate.monthValue - 1 &&
                        day.day == selectedDate.dayOfMonth

            override fun decorate(view: DayViewFacade) {
                view.addSpan(android.text.style.BackgroundColorSpan(yellowColor))
            }
        })

        // 날짜 선택 콜백
        calendarView.setOnDateChangedListener { _, date, _ ->
            onDateSelected(date.year, date.month, date.day)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}