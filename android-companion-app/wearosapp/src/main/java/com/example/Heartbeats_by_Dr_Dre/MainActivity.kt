/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.Heartbeats_by_Dr_Dre

import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.Heartbeats_by_Dr_Dre.presentation.theme.Companion_AppTheme
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.isActive
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    /** onCreate()
     * Requests permission, handles startup stuff, then starts the program
     * Note that this isn't a composable function, so don't try making it do anything else
     * If it does have to handle something, it should be passed to the WearApp() function
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        if (checkSelfPermission("android.permission.BODY_SENSORS")
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission("android.permission.ACTIVITY_RECOGNITION")
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                (arrayOf(
                    "android.permission.BODY_SENSORS",
                    "android.permission.ACTIVITY_RECOGNITION"
                )), 1);
        }
        super.onCreate(savedInstanceState)
        setContent {
            WearApp("Android", Sensor.TYPE_HEART_RATE)
        }
    }

        companion object {
        private const val TAG = "MainActivity"
        private const val HEART_RATE_PATH = "/heart_rate"
        private const val HEART_RATE_KEY = "heart_rate"

    }


}

/** WearApp()
 * Main Function, handles top level styling
 * Note this calls functions IT SHOULD NOT HANDLE INFORMATION DISPLAY INTERNALLY
 * If it does display information, it might cause A LOT of problems for us later on
 *
 * @greetingName and @greetingNum are just placeholders for now, don't have to worry about them
 */
@Composable
fun WearApp(greetingName: String, greetingNum: Int) {

        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
    Companion_AppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center
        ) {
            HeartRateSensor()
        }
    }


}

/** HeartRateSensor()
 * Handles the heart rate sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live heart rate
 * @return Data for
 */
@Composable
fun HeartRateSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val heartRatePath = "/heart_rate"
    val heartRateKey = "heart_rate"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val heartRateSensor: Sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    val default: Float = 21.00F
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }

    val heartRateSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        fun toggleSensor() {
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values[0]
                    sendHeartRateToPhone(sensorStatus.value)
                } else {
                    sensorStatus.value = 999.00F
                }
            }
        }

        private fun sendHeartRateToPhone(heartRateValue: Float) {
            try {
            val putDataMapRequest = PutDataMapRequest.create(heartRatePath)
            val dataMap = putDataMapRequest.dataMap
            dataMap.putFloat(heartRateKey, heartRateValue)
            val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()


            val task = dataClient.putDataItem(putDataRequest)

            task.addOnCompleteListener { task ->
                Log.e("HANDHELD", "Transfer data to handheld")
                if (task.result != null) {
                    val dataMapItem = DataMapItem.fromDataItem(task.result!!).dataMap
                    Log.d("item", dataMapItem.toString())
                }
            }

        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (exception: Exception) {
            Log.d("error", "Saving DataItem failed: $exception")
        }
        }
    }

    mSensorManager.registerListener(
        heartRateSensorListener,
        heartRateSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    )

    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "Testing This is important \n ${sensorStatus.value}"
    )

    Button(
        onClick = {
            heartRateSensorListener.toggleSensor()
        },
        enabled = true,

        ) {
        Text("Toggle")
    }
}

/** DefaultPreview()
 * It's the apps preview
 * Don't Worry about this, we'll change it when we need to
 */
//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp("Preview Android", 22)
//}