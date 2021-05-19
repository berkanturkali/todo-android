package com.example.todo.viewmodel.fragments.homeflow

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.AlarmManagerCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.model.Todo
import com.example.todo.receiver.AlarmReceiver
import com.example.todo.repo.TodoRepo
import com.example.todo.util.Event
import com.example.todo.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "AddTodoFragmentViewMode"

@HiltViewModel
class AddTodoFragmentViewModel @Inject constructor(
    private val todoRepo: TodoRepo,
    @ApplicationContext private val app: Context
) : ViewModel() {

    private val alarmManager = app.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val notifyIntent = Intent(app, AlarmReceiver::class.java)

    private val _timeSelection = MutableLiveData<Long>()
    val timeSelection: LiveData<Long> get() = _timeSelection

    private val _alarmOn = MutableLiveData<Boolean>()
    val isAlarmOn: LiveData<Boolean> get() = _alarmOn

    private val _addedStatus = MutableLiveData<Event<Resource<String>>>()
    val addedStatus: LiveData<Event<Resource<String>>> get() = _addedStatus

    private val _isValidDate = MutableLiveData<Event<Boolean>>()
    val isValidDate: LiveData<Event<Boolean>> get() = _isValidDate

    private var randomPendingId = (0..Int.MAX_VALUE).random()

    fun addTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.Main) {
            _addedStatus.value = Event(todoRepo.addTodo(todo))
        }
    }

    fun setAlarmTime(time: Long) {
        if (System.currentTimeMillis() > time) {
            _isValidDate.value = Event(false)
            return
        }
        _timeSelection.value = time
    }

    fun setAlarmOn(isOn: Boolean, message: String, isImportant: Boolean) {
        _alarmOn.value = isOn
        if (isOn) {
            timeSelection.value?.let {
                Log.i(TAG, "setAlarmOn: $it")
                setAlarm(it, message, isImportant)
            }
        }
    }

    fun newId() {
        randomPendingId = (0..Int.MAX_VALUE).random()
    }

    private fun setAlarm(alarmTime: Long, todoMessage: String, isImportant: Boolean) {
        notifyIntent.putExtra("message", todoMessage)
        notifyIntent.putExtra("isImportant", isImportant)
        val notifyPendingIntent: PendingIntent = PendingIntent.getBroadcast(
            app,
            randomPendingId,
            notifyIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                notifyPendingIntent
            )
        } else {
            AlarmManagerCompat.setExactAndAllowWhileIdle(
                alarmManager,
                AlarmManager.RTC_WAKEUP,
                alarmTime,
                notifyPendingIntent
            )
        }
    }
}