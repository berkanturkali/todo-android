package com.example.todo.framework.presentation.view.fragments.homeflow

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.todo.R
import com.example.todo.databinding.FragmentEditTodoLayoutBinding
import com.example.todo.business.domain.model.Todo
import com.example.todo.util.*
import com.example.todo.framework.presentation.view.fragments.BaseFragment
import com.example.todo.framework.presentation.viewmodel.MainTodoFragmentViewModel
import com.example.todo.framework.presentation.viewmodel.fragments.homeflow.EditTodoFragmentViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class EditTodoFragment :
    BaseFragment<FragmentEditTodoLayoutBinding>(
        FragmentEditTodoLayoutBinding::inflate
    ) {
//    private val args: EditTodoFragmentArgs by navArgs()

    private val mViewModel: EditTodoFragmentViewModel by viewModels()
    private val mainTodoViewModel by viewModels<MainTodoFragmentViewModel>(ownerProducer = { requireParentFragment().requireParentFragment() })
    private lateinit var calendar: Calendar
    private lateinit var dateFormat: SimpleDateFormat
    private var alarmOn = false
    private val dateTimeFormat = SimpleDateFormat(
        "${Consts.DATE_PATTERN} ${Consts.TIME_PATTERN}",
        Locale.getDefault()
    )
    private var intentId = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        dateFormat = Consts.DATE_PATTERN.formatter()
        initButtons()
//        mViewModel.getTodo(args.todoId)
        subscribeObservers()
    }

    private fun initButtons() {
        val categories = resources.getStringArray(R.array.category_array)
        binding.categoryPickerIb.setOnClickListener {
            categories.showDialog(requireContext(), "Select a Category", binding.categoryEt)
        }
        binding.datePickerIb.setOnClickListener {
            val datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select dates")
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .build()
            datePicker.show(requireActivity().supportFragmentManager, "tag")
            datePicker.addOnPositiveButtonClickListener {
                calendar.timeInMillis = it
                binding.dateEt.setText(dateFormat.format(calendar.timeInMillis))
            }
        }
        binding.updateTodoBtn.setOnClickListener {
            if (checkFields()) {
                updateTodo()
            } else {
              showSnack(
                    "Fields can not be empty"
                )
            }
        }
        binding.importancePickIb.setOnClickListener {
            resources.getStringArray(R.array.importance_array)
                .showDialog(requireContext(), "Select Importance", binding.importanceTv)
        }
        binding.completedPickIb.setOnClickListener {
            resources.getStringArray(R.array.completed_array)
                .showDialog(requireContext(), "Select Complete Status", binding.completedTv)
        }
        binding.timePickerIb.setOnClickListener {
            showTimePicker()
        }
        binding.notifySwitch.setOnCheckedChangeListener { _, isChecked ->
            alarmOn = isChecked
            if (isChecked) {
                setAlarmTime()
            }
        }
    }

    private fun setAlarmTime() {
        val dateInMillis =
            dateTimeFormat.parse("${binding.dateEt.text}  ${binding.timeEt.text}")!!.time
        mainTodoViewModel.setNotificationTime(dateInMillis)
    }

    private fun showTimePicker() {
        val picker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_24H)
            .setHour(0)
            .setMinute(0)
            .setTitleText("Select Appointment time")
            .build()
        picker.show(childFragmentManager, "tag")

        picker.addOnPositiveButtonClickListener {
            val newHour: Int = picker.hour
            val newMin: Int = picker.minute
            val hourAsText = if (newHour < 10) "0$newHour" else newHour
            val minuteAsText = if (newMin < 10) "0$newMin" else newMin
            binding.timeEt.setText("$hourAsText:$minuteAsText")
        }
    }

    private fun checkFields(): Boolean {
        val todoDesc = binding.todoEt.text.toString().trim()
        return todoDesc.isNotEmpty()
    }

    private fun updateTodo() {
        binding.apply {
            val category = categoryEt.text.toString().trim()
            val importance = when (binding.importanceTv.text.toString()) {
                "Important" -> true
                "Not Important" -> false
                else -> null
            }
            val completed = when (binding.completedTv.text.toString()) {
                "Completed" -> true
                "Not Completed" -> false
                else -> null
            }
            val todoText = binding.todoEt.text.toString().trim()
            val dateInMillis =
                dateTimeFormat.parse("${binding.dateEt.text}  ${binding.timeEt.text}").time
            val notifyMe = alarmOn
            if (alarmOn) {
                intentId = mainTodoViewModel.newId()
                mainTodoViewModel.setNotificationOn(todoText, importance!!)
            } else {
                intentId = -1
            }
            val todo = Todo(
                category,
                dateInMillis,
                todoText,
                isCompleted = completed!!,
                isImportant = importance!!,
                notifyMe = notifyMe,
                notificationId = intentId
            )
//            mViewModel.updateTodo(args.todoId, todo)
        }
    }

    private fun subscribeObservers() {
//        mViewModel.todo.observe(viewLifecycleOwner) { resource ->
//            when (resource) {
//                is Resource.Success -> setFields(resource.data!!)
//                is Resource.Error -> {
//                    if (alarmOn && intentId != -1) mainTodoViewModel.cancelNotification(intentId)
//                    showError(resource.message.toString())
//                }
//            }
//        }
//        mViewModel.updateStatus.observe(viewLifecycleOwner)
//        {
//            it.getContentIfNotHandled()?.let { message ->
//                showSnack(message, R.color.black)
//            }
//        }
        mainTodoViewModel.isValidDate.observe(viewLifecycleOwner) {
            it.getContentIfNotHandled()?.let { isValid ->
                if (!isValid) {
                    showSnack(
                        "Can not set alarm to past time."
                    )
                    binding.notifySwitch.isChecked = false
                }
            }
        }
    }

    private fun setFields(todo: Todo) {
        binding.apply {
            categoryEt.setText(todo.category)
            dateEt.setText(dateFormat.format(todo.date))
            todoEt.setText(todo.todo)
            completedTv.setText(if (todo.isCompleted) "Completed" else "Not Completed")
            importanceTv.setText(if (todo.isImportant) "Important" else "Not Important")
            timeEt.setText(Consts.TIME_PATTERN.formatter().format(todo.date))
            notifySwitch.isChecked = todo.notifyMe && System.currentTimeMillis() < todo.date
            intentId = todo.notificationId
        }
    }

    private fun showError(message: String) {
        showSnack(message)
    }
}