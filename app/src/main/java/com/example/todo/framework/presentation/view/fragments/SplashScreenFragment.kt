package com.example.todo.framework.presentation.view.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.todo.R
import com.example.todo.util.StorageManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject

@AndroidEntryPoint
class SplashScreenFragment : Fragment(R.layout.fragment_splash_screen) {

    @Inject
    lateinit var storageManager: StorageManager

    private val fragmentScope = CoroutineScope(Dispatchers.Main)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadSplashScreen()
    }

    private fun loadSplashScreen() {
        fragmentScope.launch {
            delay(3000)
            if (!storageManager.getToken().isNullOrBlank()) {
                navigateToHomeFlow()
            } else {
                navigateToAuthFlow()
            }
        }
    }

    private fun navigateToHomeFlow() {

    }

    private fun navigateToAuthFlow() {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        fragmentScope.cancel()
    }
}