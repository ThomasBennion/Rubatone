package com.example.Heartbeats_by_Dr_Dre

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.simno.kortholt.kortholt
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.roundToInt

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    // Triggers when application tab is navigated off/ tabs switched/ phone screen locked
    override fun onPause() {
        // remove wearable listener when app is paused/navigated off, but not closed
        Wearable.getDataClient(this).removeListener(this)

        // Toggle audio to stop by sending float value of 0.0 to Kortholt/PD patch
        kortholt.sendFloat("appOnOff", 0.0f)

        runBlocking {
            /*
            Allow a 5ms delay between toggling audio playback to stop, and stopping the audio
            stream. When the audio playback is toggled to stop, this triggers the PD patch to
            fade out the audio volume within 4ms. A 5ms delay should allow for the full fade out,
            preventing any pops/clicks in the audio. If these pops/clicks are still apparent, the
            delay time may need to be increased.
             */
            delay(5)
            // After the delay, stop the audio stream
            kortholt.stopStream()
        }

        super.onPause()
    }

    // Triggers when application tab is navigated back to / resumed
    public override fun onResume() {
        // add wearable listener when tab is resumed
        Wearable.getDataClient(this).addListener(this)

        runBlocking {
            // Start the audio stream
            kortholt.startStream()
        }
        // Send a float value of 1.0 to kortholt/PD patch to toggle audio playback on
        kortholt.sendFloat("appOnOff", 1.0f)
        // TODO fix issue with audio automatically playing on startup
        super.onResume()
    }

    /**
     * Handles audio components when the app is closed completely (app instance is destroyed).
     * Toggles the audio off, then stops the audio stream and closes the opened PD patch in
     * Kortholt.
     */
    override fun onDestroy() {
        // Toggle audio to stop by sending float value of 0.0 to Kortholt/PD patch
        kortholt.sendFloat("appOnOff", 0.0f)
        // Stop audio stream and close patch
        runBlocking {
            // Apply a 5ms delay to allow the PD patch to fade audio out (within 4ms)
            delay(5)
            // Stop the audio stream
            kortholt.stopStream()
            // Close the opened PD patch
            kortholt.closePatch()
        }
        super.onDestroy()
    }

    /*
    * Initialize all your state variables related to sensor data here
    * */
    private var localHeartRate by mutableStateOf<Float>( 999.00F) // heart rate
    /*
    This was used to test if the accelerometer inputs functioned appropriately.
    Values can be retrieved from the sensor, but implementing them with the heart
    rate sensor will cause the app to crash on a change in heart rate values.
     */
    // Accelerometer values for x, y, z axes:
    private var localAccelValues by mutableStateOf<FloatArray>(floatArrayOf(999.00F, 999.00F, 999.00F))
    // For temperature testing
    private var localTemp by mutableStateOf<Float>( 999.00F)
    // For light sensor testing
    private var localLight by mutableStateOf<Float>( 999.00F)
    /*
    This was used to test if the gyro inputs functioned appropriately.
    Values can be retrieved from the sensor, but implementing them with the heart
    rate sensor will cause the app to crash on a change in heart rate values.
     */
    // Gyro values for x, y, z axes - testing
    private var localGyroValues by mutableStateOf<FloatArray>(floatArrayOf(999.00F, 999.00F, 999.00F))

    /* onDataChanged()
    * triggers when data is changed in the wearOS app
    * this function will listen to all sensor data
    * */
    override fun onDataChanged(dataEvents: DataEventBuffer) {
        Log.d("DATA_CHANGED", "data is being received")
        dataEvents.forEach {event ->
            if (event.type == DataEvent.TYPE_DELETED) {
                Log.d("DELETED", "DataItem deleted"+event.dataItem.uri)
            } else if (event.type == DataEvent.TYPE_CHANGED) {
                Log.d("CHANGED", "DataItem changed"+event.dataItem.uri)

                val item = event.dataItem
                val dataItem = DataMapItem.fromDataItem(item)
                val dataMap = (dataItem as DataMapItem).dataMap

                // reassign all the state variables here
                // Store the current heart rate value from the wearable within the app
                val tempoHeartRate = dataMap.getFloat("heart_rate")
                if (tempoHeartRate.toInt() != 0)
                    localHeartRate = tempoHeartRate
                /*
                This was used to test if the accelerometer inputs functioned appropriately.
                Values can be retrieved from the sensor, but implementing them with the heart
                rate sensor will cause the app to crash on a change in heart rate values.
                 */
                // Store the current accelerometer values
                val tempoAccelValue: FloatArray? = dataMap.getFloatArray("accelerometer")
                if (tempoAccelValue != null) {

                    localAccelValues = tempoAccelValue
                    Log.d(
                        "CHANGED",
                        "Accel values: [${localAccelValues[0]}, ${localAccelValues[1]}, ${localAccelValues[2]}]"
                    )
                }

                // Testing if the light/temperature sensors work ok
                val tempoTemp = dataMap.getFloat("temperature")
                if (tempoTemp.toInt() != 0) {
                    localTemp = dataMap.getFloat("temperature")
                    Log.d("CHANGED", "Temperature: $localTemp degrees C")
                }
                val tempoLight =dataMap.getFloat("light")
                if (tempoLight.toInt() != 0) {
                    localLight = tempoLight
                    Log.d("CHANGED", "Light sensor: $localLight lux")
                }
                /*
                This was used to test if the gyrometer inputs functioned appropriately.
                Values can be retrieved from the gyro sensor, but implementing them with the heart
                rate sensor will cause the app to crash on a change in heart rate values.
                */
                val tempoGyroValues: FloatArray? = dataMap.getFloatArray("gyrometer")
                if (tempoGyroValues != null) {
                    localGyroValues = tempoGyroValues
                    Log.d("CHANGED", "Gyro values: [${localGyroValues[0]}, ${localGyroValues[1]}, ${localGyroValues[2]}]")
                }
            }
        }
    }

//    On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load PD patch via Kortholt after extracting from zip file - patch is used to generate audio
        lifecycleScope.launch { kortholt.openPatch(R.raw.pulse_mockup_one_file, "pulse_mockup_one_file.pd", extractZip = true) }
        // TODO fix issue with audio automatically playing on startup
        setContent {
            MainCompanionAppLayout(localHeartRate) // Would also need to add accelValues as an input param
        }
    }
}

/** MainCompanionAppLayout(localHeartRate)
 * Contains the main structure of the App
 * the structure is extremely likely to change after MVP is completed
 *
 * @param localHeartRate (float) the heart rate value currently stored by the app (in BPM)
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompanionAppLayout(localHeartRate: Float) { // Would also need to add accelValues as an input param
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.mediumTopAppBarColors  (
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Heartbeats by Dr.Dre")
                }
                
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.primary,
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = "Legion ft. Tony Robbins",
                )
            }
        },

        floatingActionButton = {
            // turn sounds on/off
            PlayControl(modifier = Modifier)

            // change music tempo
            TempoControl(localHeartRate)

            // Changes the music synth dynamics/volume
            //DynamicsControl(localAccelValues)
        }

    ) { innerPadding ->
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            HeartRateMonitor(localHeartRate)
        }
    }
}

/** HeartRateMonitor(localHeartRate)
 *
 * displays heart rate passed from parents
 * */
@Composable
fun HeartRateMonitor(localHeartRate: Float) {
    // state variable that represents the current heart rate, initialized to 0 for ease of testing
        androidx.compose.material3.Icon(imageVector = Icons.Default.Favorite, contentDescription ="", Modifier.size(120.dp) )
        Text(text = "${localHeartRate.roundToInt()} BPM", fontSize = 35.sp)

}


/**
 * Creates a button that can be used to turn the audio synthesis component on or off.
 * When starting/stopping audio, a float value is sent to the PD patch via Kortholt to toggle the
 * change. The audio stream must also be started/stopped by Kortholt.
 * When turning audio off, a fade out is applied to the audio to mute it over a period of 4ms.
 * This is applied so that there are no clicks/pops in the audio. Adding a delay of 5ms between
 * toggling the audio off in the PD patch and closing the Kortholt audio stream should allow for
 * the fade out to finish completely. If there are still pops/clicks occurring when the audio
 * is toggled on/off, this delay amount may need to be increased slightly.
 */
@Composable
fun PlayControl(modifier: Modifier) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    val scope = rememberCoroutineScope()

    // Store state for audio playback (initialise to false - audio not playing on app startup)
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    // Play audio
    if (isPlaying) {
        runBlocking {
            // Start audio stream
            scope.launch { kortholt.startStream() }
            Log.d("K START", "Kortholt stream is starting")
        }
        // Toggle playback in PD patch by sending float value of 1.0
        kortholt.sendFloat("appOnOff", 1.0f)
        // Change playback status after playback has been started (within start/stop button)
    }

    // Stop audio (isPlaying = false)
    else {
        // Toggle audio off in PD patch by sending float value of 0
        kortholt.sendFloat("appOnOff", 0.0f)
        runBlocking {
            scope.launch {
                // Apply a 5ms delay to allow the PD patch to fade audio out (within 4ms)
                delay(5)
                // After the delay, stop the audio stream
                kortholt.stopStream()
            }
            Log.d("K STOP", "Kortholt stream is stopping")
        }
        // Change playback status after playback has started (within start/stop button)
    }

    // Create a play/stop button
    ExtendedFloatingActionButton(modifier = modifier,
        /*
        If isPlaying is currently true, then set it to false (as the button has been clicked,
        and audio will stop playing).
        If isPlaying is currently false, then set it to true (as the button has been clicked,
        and audio will start playing).
         */
        onClick = {
            isPlaying = !isPlaying
            }) {
        // Change the button label once clicked
        if (!isPlaying) {
            // If stopped, set the button label to 'Play'
            Text(text = "play")
        } else {
            // If playing, set the button label to 'Stop'
            Text(text = "stop")
        }
    }
}

/**
 * Sends the heart rate value currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The heart rate value is periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the tempo of a
 * pulsing sine tone synth - faster heart rate corresponds to
 * a faster tempo value.
 *
 * @param heartRate (float) user's heart rate (in Beats Per Minute).
 *                  Valid wearable values are bounded to the range
 *                  [50, 200] BPM. A value of 999 BPM is sent if the heart
 *                  rate sensor is offline/disabled.
 */
@Composable
fun TempoControl(heartRate: Float) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    Log.d("HEART_RATE", "Heart rate value sent to PD: $heartRate BPM")
    // Call function to modify heart rate value/tempo in PD patch
    kortholt.sendFloat("appHeartRate", heartRate)
}

/*
These methods were used to test if the accelerometer inputs functioned appropriately.
Values can be retrieved from the sensor, but implementing them with the heart
rate sensor will cause the app to crash on a change in heart rate values.
 */

/**
 * Sends the accelerometer values currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The accelerometer raw values are periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the volume/dynamics of a
 * pulsing sine tone synth - 'stronger' hand movements will correspond to a louder sound/stronger
 * dynamics.
 *
 * @param accelValues (float array) accelerometer's acceleration values in the x, y, z axes (m/s^2).
 *                    Values of 999.00 are sent if the sensor is offline/disabled.
 */
@Composable
fun DynamicsControl(accelValues: FloatArray) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }

    Log.d("ACCEL", "Accelerometer values: [${accelValues[0]}, ${accelValues[1]}, ${accelValues[2]}]")
    var accelMagnitude = 999.00f

    // If the sensor is offline/disabled, send a value of 999.00 directly to PD patch
    if (!accelValues.contentEquals(floatArrayOf(999.00f, 999.00f, 999.00f))) {
        // If the sensor is online:
        // Get the linear acceleration from the accelerometer data
        var linearAccelValues = getLinearAcceleration(accelValues)
        // Calculate the magnitude of the linear acceleration vectors
        accelMagnitude = getLinearAccelMagnitude(linearAccelValues)
    }
    Log.d("ACCEL", "PD input value: $accelMagnitude")
    // Send the magnitude input value to the PD patch
    kortholt.sendFloat("appAccelerometer", accelMagnitude)
}


/**
 * Takes the accelerometer values for each positional axis (x, y, z), then attempts to isolate
 * and remove the force of gravity from the measured accelerometer values. Gives a sense of the
 * linear acceleration in each positional axis such that the force of gravity is ignored.
 *
 * For more information about this, see here:
 * https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-accel
 *
 * Another potential implementation for this would involve taking data directly from
 * Sensors.TYPE_LINEAR_ACCELERATION from the wearable. This may be more useful/accurate data that
 * potentially utilises sensor fusion to give more accurate readings/values. For more info,
 * see here: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
 *
 * @param accelValues (float array) accelerometer's acceleration values in the x, y, z axes (m/s^2).
 *                                  Values of 999.0 are sent if the sensor is offline/disabled;
 *                                  however, implementation of this method assumes that it won't be
 *                                  called if this is the case.
 * @return (float array) the linear acceleration values in the x, y, z axes (m/s^2)
 */
fun getLinearAcceleration(accelValues: FloatArray): FloatArray {
    /*
    "The code sample uses an alpha value of 0.8 for demonstration purposes.
    If you use this filtering method you may need to choose a different alpha value.
     In this example, alpha is calculated as t / (t + dT),
     where t is the low-pass filter's time-constant and
     dT is the event delivery rate."
     */
    val alpha: Float = 0.8f
    var gravity = floatArrayOf(0f, 0f, 0f)
    var linearAcceleration = floatArrayOf(0f, 0f, 0f)

    // Isolate the force of gravity with a low-pass filter
    gravity[0] = alpha * gravity[0] + (1 - alpha) * accelValues[0]
    gravity[1] = alpha * gravity[1] + (1 - alpha) * accelValues[1]
    gravity[2] = alpha * gravity[2] + (1 - alpha) * accelValues[2]

    // Remove the gravity contribution with a high-pass filter
    linearAcceleration[0] = accelValues[0] - gravity[0]
    linearAcceleration[1] = accelValues[1] - gravity[1]
    linearAcceleration[2] = accelValues[2] - gravity[2]

    Log.d("ACCEL", "Linear acceleration values: [${linearAcceleration[0]}, ${linearAcceleration[1]}, ${linearAcceleration[2]}]")
    return linearAcceleration
}


/**
 * Calculates the magnitude of the linear acceleration vectors in the x, y, z axes.
 * The magnitude is calculated as sqrt(a_x^2 + a_y^2 + a_z^2), where a_x, a_y, a_z are the linear
 * acceleration values.
 *
 * For more info, see here:
 * https://www.calctool.org/kinetics/magnitude-of-acceleration#how-to-calculate-the-magnitude-of-the-acceleration-from-its-component
 *
 * @param linearAccelValues (float array) the linear acceleration values in the x, y, z axes (m/s^2)
 * @return (float) the magnitude of the given linear acceleration values
 */
fun getLinearAccelMagnitude(linearAccelValues: FloatArray): Float {
    var accelMagnitude = sqrt(linearAccelValues[0].pow(2) + linearAccelValues[1].pow(2) +
                                                                    linearAccelValues[2].pow(2))
    Log.d("ACCEL", "Linear acceleration magnitude: $accelMagnitude")
    return accelMagnitude

}