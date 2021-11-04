package com.example.todo.framework.presentation.viewmodel.fragments.homeflow

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.todo.business.domain.model.Todo
import com.example.todo.business.repo.abstraction.TodoRepo
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

    private val _addedInfo = MutableLiveData<Event<Resource<String>>>()
    val addedInfo: LiveData<Event<Resource<String>>> get() = _addedInfo

    fun addTodo(todo: Todo) {
        viewModelScope.launch(Dispatchers.Main) {
            _addedInfo.value = Event(todoRepo.add(todo))
        }
    }
}