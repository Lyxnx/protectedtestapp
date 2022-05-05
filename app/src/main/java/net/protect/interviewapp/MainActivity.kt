package net.protect.interviewapp

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers
import net.protect.interviewapp.api.request.GetWeatherRequestTask

class MainActivity : AppCompatActivity() {

    companion object {
        const val CHANNEL_ID = "InterviewAppNotificationChannel"
        const val NOTIFICATION_ID = 999
    }

    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var cityInputLayout: TextInputLayout
    private lateinit var cityInput: TextInputEditText
    private lateinit var findWeatherButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, getString(R.string.notification_channel_name), NotificationManager.IMPORTANCE_HIGH).apply {
                description = getString(R.string.notification_channel_description)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        cityInputLayout = findViewById(R.id.city_input_layout)
        cityInput = findViewById(R.id.city_input)
        findWeatherButton = findViewById(R.id.find_weather_button)

        cityInput.doOnTextChanged { text, _, _, _ ->
            findWeatherButton.isEnabled = !text.isNullOrBlank()
        }

        findWeatherButton.setOnClickListener {
            showProgress(true)

            GetWeatherRequestTask(cityInput.text.toString()).observable
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    showProgress(false)

                    if (it.success) {
                        val notif = NotificationCompat.Builder(this@MainActivity, CHANNEL_ID)
                            .setLargeIcon(AppCompatResources.getDrawable(this, R.drawable.thermometer)!!.toBitmap())
                            .setSmallIcon(R.drawable.thermometer)
                            .setContentTitle(getString(R.string.notification_title))
                            .setContentText(getString(if (it.temperature!! >= 20) R.string.notification_text_hot else R.string.notification_text_cold))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .build()

                        notificationManager.notify(NOTIFICATION_ID, notif)
                        cityInputLayout.error = null
                    } else {
                        cityInputLayout.error = it.error
                    }
                }, {
                    showProgress(false)

                    AlertDialog.Builder(this)
                        .setTitle(getString(android.R.string.dialog_alert_title))
                        .setMessage(it.message)
                        .setPositiveButton(getString(android.R.string.ok)) { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()
                        .show()
                })
        }
    }

    private fun showProgress(show: Boolean) {
        findViewById<ProgressBar>(R.id.progress_bar).visibility = if (show) View.VISIBLE else View.GONE
    }
}