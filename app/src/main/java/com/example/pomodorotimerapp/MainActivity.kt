package com.example.pomodorotimerapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Continue the action or workflow in your app.
        } else {
            // Explain to the user that the feature is unavailable because the
            // features requires a permission that the user has denied.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        createNotificationChannel(this)
        checkNotificationPermission(this, requestPermissionLauncher)
        setContent {
            PomodoroScreen()
        }
    }

    private fun createNotificationChannel(context: Context) {
        val channelId = "pomodoro_channel"
        val channelName = "Pomodoro Notifications"
        val channelDescription = "Notifications for Pomodoro Timer"

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.description = channelDescription
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkNotificationPermission(context: Context, requestPermissionLauncher: ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS) -> {
                // In an educational UI, explain to the user why your app requires this
                // permission for a specific feature to behave as expected. In this UI,
                // include a "cancel" or "no thanks" button that allows the user to
                // continue using your app without granting the permission.
            }
            else -> {
                // You can directly ask for the permission.
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}

@Composable
fun PomodoroScreen() {
    var timeLeft by remember { mutableStateOf(25 * 60 * 1000L) }
    var isRunning by remember { mutableStateOf(false) }
    var timer: CountDownTimer? by remember { mutableStateOf(null) }
    var isBreak by remember { mutableStateOf(false) }
    var workDuration by remember { mutableStateOf(25 * 60 * 1000L) }
    var breakDuration by remember { mutableStateOf(5 * 60 * 1000L) }
    val context = LocalContext.current

    fun showNotification(context: Context, title: String, message: String) {
        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            val builder = NotificationCompat.Builder(context, "pomodoro_channel")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

            with(NotificationManagerCompat.from(context)) {
                notify(System.currentTimeMillis().toInt(), builder.build())
            }
        }
    }

    fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(timeLeft, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
            }

            override fun onFinish() {
                isBreak = !isBreak
                timeLeft = if (isBreak) breakDuration else workDuration
                isRunning = false
                startTimer()
                showNotification(
                    context = context,
                    title = if (isBreak) "Break Time!" else "Work Time!",
                    message = if (isBreak) "Break is over. Time to work!" else "Work session is over. Take a break!"
                )
            }
        }.start()
        isRunning = true
    }

    fun stopTimer() {
        timer?.cancel()
        isRunning = false
    }

    fun resetTimer() {
        timer?.cancel()
        isBreak = false
        timeLeft = workDuration
        isRunning = false
    }

    fun increaseWorkDuration() {
        workDuration += 5 * 60 * 1000L
        if (!isBreak) timeLeft = workDuration
    }

    fun decreaseWorkDuration() {
        if (workDuration > 5 * 60 * 1000L) {
            workDuration -= 5 * 60 * 1000L
            if (!isBreak) timeLeft = workDuration
        }
    }

    fun increaseBreakDuration() {
        breakDuration += 5 * 60 * 1000L
        if (isBreak) timeLeft = breakDuration
    }

    fun decreaseBreakDuration() {
        if (breakDuration > 5 * 60 * 1000L) {
            breakDuration -= 5 * 60 * 1000L
            if (isBreak) timeLeft = breakDuration
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (isBreak) Color(0xFF3F51B5) else Color(0xFF4CAF50)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isBreak) "Break Time 🏖" else "Work! 🚀",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = String.format("%02d:%02d", (timeLeft / 1000) / 60, (timeLeft / 1000) % 60),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Work Duration", color = Color.White, fontSize = 20.sp)
            Row {
                Button(
                    onClick = { decreaseWorkDuration() },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(text = "-", fontSize = 24.sp)
                }

                Button(
                    onClick = { increaseWorkDuration() },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                ) {
                    Text(text = "+", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Break Duration", color = Color.White, fontSize = 20.sp)
            Row {
                Button(
                    onClick = { decreaseBreakDuration() },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text(text = "-", fontSize = 24.sp)
                }

                Button(
                    onClick = { increaseBreakDuration() },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3F51B5))
                ) {
                    Text(text = "+", fontSize = 24.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = { if (isRunning) stopTimer() else startTimer() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) Color(0xFF607D8B) else Color(0xFF2196F3)
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = if (isRunning) "Stop" else "Start", fontSize = 20.sp)
            }

            Button(
                onClick = { resetTimer() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.padding(8.dp)
            ) {
                Text(text = "Reset", fontSize = 20.sp)
            }
        }
    }
}