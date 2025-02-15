package com.example.Heartbeats_by_Dr_Dre

import android.hardware.SensorManager
import android.health.connect.datatypes.units.Temperature
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * The ethics of the phone app:
 *
 * The primary ethical considerations were:
 * 1. Our responsibility for handling the users data in a way that ensures transparency and
 * privacy for the user
 * 2. Our duty of care in ensuring our users are not encouraged to hurt themselves when
 * using our application
 *
 * Handling User Data:
 * When handling user data, we need to ensure the user is aware of what data is being used
 * and how is it being used. This should be done before the user interacts with the application.
 *
 * - We must make sure the data is not being used in ways that might conflict with what
 * the user expects of us. To do this, we shouldn’t send any data to a “middleman”,
 * instead we should have the data only be sent between the watch app and the phone app,
 * therefore ensuring the data can’t be used for anything else.
 * - The user expects us to use the data only for the functions they can perceive,
 * (i.e. to create music). Therefore we shouldn’t require any sensor or private
 * information for any other function of the app.
 * - Ideally, the data stored by the phone app should not be stored on the device in the
 * long term, and in large quantities. To prevent this, only a single reading from each sensor is stored
 * in the mobile app at any given time. When a new value is received from the watch, the previously
 * received value is overwritten.
 * - An exception to this is the gyrometer sensor, in which a rotation vector
 * is calculated from the current gyrometer values, and the previous rotation vector is stored within the
 * app (as well as the current rotation vector). This must be done to calculate relative angular rotation
 * changes, with respect to a previous value. As the process of creating the rotation vector introduces
 * a linear drift/error term among other inaccuracies, the previously stored rotation vector data is at
 * least partially transformed such that it does not directly identify a user.
 * - Within the wearable application, the user has the ability to enable or disable sensors, effectively
 * being able to revoke consent for their data being transferred/logged within the apps. If sensors are
 * disabled, a value of 999.0 is sent from the wearable app to the phone app. When the music synthesis
 * component receives this value of 999.0, any musical elements associated with the sensor are disabled.
 * The music synthesis component then resets these to an arbitrary default start-up value.
 * - When the instance of the app is destroyed (eg. the app is closed), listeners that interface with the
 * wearable app (and receive values from it) are removed. This means that the mobile device will not
 * receive any data from the wearable app, if the mobile app has quit. Currently, the mobile app is
 * also destroyed when the app's process is moved to the background (eg. when the user's screen is locked,
 * or when a user attempts to switch applications without closing). Values are only received
 * from the wearable app when the mobile app is actively running as a foreground process.
 *
 * Duty of Care:
 * When it comes to our duty of care, we have to decide on the line between what is
 * inhibiting the users ability to use the application, and what is encouraging undue
 * risk to our users. This is also difficult as health is very dependent on a person's
 * age and overall health status.
 * Fortunately for us, the “Guiness Book of World Records” was faced with a very similar issue,
 * so we can apply their reasoning to our own sensors.
 *
 * We shouldn’t encourage users to perform activities that are:
 * - Risky to a user's long term health
 * - That an average, unqualified person couldn't reasonably try and accomplish
 *
 * Heart Rate:
 * - While a person could artificially increase their heart rate to risky places via drugs,
 * this is far more difficult to do than trying to lower your heart rate, which you can do by
 * holding your breath for long periods of time
 * (natural response of the body to preserve resources). Therefore, we should put much more
 * stringent checks in place to ensure the user doesn’t lower their heart rate too low.
 * A heart rate in the range of 50 - 200 bpm is acceptable. Notably, it is possible to
 * have a heart rate lower than 50 and still be healthy (if you are an athlete), but the
 * risk of our users reaching this heart rate by unhealthy means was considered too great.
 * - As the heart rate sensor corresponds to the music tempo, it must be enabled for any
 * other passive inputs (temperature and light intensity) to be used/processed within the
 * music synthesis component.
 *
 * Gyrometer + Accelerometer:
 * - These values were not bounded when receiving them from the wearable app. However,
 * the values are bounded within the music synthesis component itself. This will result in
 * extreme values producing identical outputs to the end points of the value ranges.
 * We hope that this will discourage users from performing dangerous or extreme movements,
 * as no unique musical content will be generated for dangerous gestures, resulting in
 * users not bothering to seek out any unheard/unique musical content.
 * - Both accelerometer and gyrometer input data is transformed in a way which translates
 * certain user gestures into appropriate numeric values, which can then be used to create
 * music appropriately. A result of these transformations is that a user's accelerometer
 * and gyrometer inputs are partially abstracted when sending them to the music synthesis
 * component.
 * - For accelerometer values, this involves extracting the force of
 * linear acceleration, then calculating the magnitude of this linear acceleration. This
 * process inevitably introduces some error/noise into the calculated values, and abstracts
 * the original data in a way that makes it difficult (if not impossible) to recover.
 * - The gyrometer performs an integration/quarternion rotation operation, which results in
 * a linear drift/error term being added, which abstracts the original input
 * data.
 * - If the gyrometer inputs are disabled in the wearable app, then the pitch values for the
 * melody instrument (in the music synthesis component) will no longer be changed. The pitch of
 * the melody will stay at a static value.
 * - If the accelerometer inputs are disabled by the wearble, then the melody instrument will
 * be turned off entirely in the music synthesis component.
 *
 * Temperature:
 * - Considering that this sensor is on the person's wrist, it is reasonable to assume
 * that a healthy temperature for the person's skin or the surrounding environment is acceptable.
 * Therefore, our range will be bounded to 0 – 55 degrees Celsius, as standard environment temperatures
 * could potentially reach within this range.
 * - Disabling the temperature sensor in the wearable app will result in a static, default
 * temperature value being used by the music synthesis component.
 *
 * Light Sensor:
 * - There isn’t a need to put a limit on the light sensor. While it is possible to blind yourself
 * with very bright lights, it is too difficult for the watch to track (as it depends on the
 * amount of light your eyes are taking in). Therefore we do not consider misuse of this sensor
 * to fall into our duty of care, as we cannot control it.
 * - Disabling the light sensor in the wearable app will result in a static, default
 * light intensity value being used by the music synthesis component.
 * */


class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    /**
     * Triggers when application tab is navigated off/tabs switched/phone screen locked.
     * Note: To prevent issues with audio playback, I have temporarily changed this method so that it
     * destroys the app on pause, preventing it from running in the background.
     * If you want to add the ability to handle stopping/restarting audio once an app has been moved
     * to/from a background process, you might need to look into the issue with audio automatically
     * playing on startup in the onResume method.
     *
     * When the instance of the app is destroyed (eg. the app is closed), listeners that interface with the
     * wearable app (and receive values from it) are removed. This means that the mobile device will not
     * receive any data from the wearable app, if the mobile app has quit. Currently, the mobile app is
     * also destroyed when the app's process is moved to the background (eg. when the user's screen is locked,
     * or when a user attempts to switch applications without closing). Values are only received
     * from the wearable app when the mobile app is actively running as a foreground process.
     */
    override fun onPause() {

        // Remove the wearable listener when app is paused/navigated off, but not closed
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

        // Close the app and completely remove the task (the app is not allowed to run in the background)
        finishAndRemoveTask()

    }

    /**
     * Normally triggers when an application tab is navigated back to/resumed, and when the app is first
     * initiated (after the onCreate method is called).
     * Note: I've temporarily changed the implementation of the onPause method, which won't allow
     * for the app to run in the background. Because of this, onResume is only currently called when
     * initialising the app (after onCreate is called). If you want to include the ability for the app
     * to run as a background process, you may need to look into some issues with the logic for audio
     * playback in this method.
     */
    public override fun onResume() {
        // Add wearable listener when tab is resumed
        Wearable.getDataClient(this).addListener(this)

        runBlocking {
            // Start the audio stream
            kortholt.startStream()
        }
        // Send a float value of 0.0 to kortholt/PD patch to prevent audio from playing on startup
        kortholt.sendFloat("appOnOff", 0.0f)

        super.onResume()
    }

    /**
     * Handles audio components when the app is closed completely (app instance is destroyed).
     * Toggles the audio off, then stops the audio stream and closes the opened PD patch in
     * Kortholt.
     *
     * When the instance of the app is destroyed (eg. the app is closed), listeners that interface with the
     * wearable app (and receive values from it) are removed. To do this, the onPause method is called - this
     * method is always automatically called before the onDestroy method. This means that the mobile device will not
     * receive any data from the wearable app, if the mobile app has quit. Currently, the mobile app is
     * also destroyed when the app's process is moved to the background (eg. when the user's screen is locked,
     * or when a user attempts to switch applications without closing). Values are only received
     * from the wearable app when the mobile app is actively running as a foreground process.
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
    * Initialise the passive input sensors to the 'disabled' sensor value of 999.0.
    * This indicates to the music synthesis components that the sensors are disabled on startup, until
    * other sensor values are received.
    * */
    // Heart rate
    private var localHeartRate by mutableStateOf<Float>( 999.00F)
    // For temperature
    private var localTemperature by mutableStateOf<Float>( 999.00F)
    // For light sensor
    private var localLight by mutableStateOf<Float>( 999.00F)

    /*
    Initialise the active input sensors and any required vars for their associated calculations/
    value transformations. These are initialised similarly to the passive sensors.
     */
    // Accelerometer values for x, y, z axes:
    private var localAccelValues by mutableStateOf<FloatArray>(floatArrayOf(999.00F, 999.00F, 999.00F))

    // Gyro values for x, y, z axes:
    private var localGyroValues by mutableStateOf<FloatArray>(floatArrayOf(999.00F, 999.00F, 999.00F))
    // Store a timestamp for the last gyro value update into the app
    var gyroTimestamp: Long = 0L

    /**
     * onDataChanged()
    * triggers when data is changed in the wearOS app.
    * this function will listen to all sensor data.
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


                // Store the current accelerometer values
                val tempoAccelValue: FloatArray? = dataMap.getFloatArray("accelerometer")
                if (tempoAccelValue != null) {
                    localAccelValues = tempoAccelValue
                } else {
                    // If accelerometer sensor outputs null values, send values of 0 to audio synthesis component
                    localAccelValues = floatArrayOf(0.0f, 0.0f, 0.0f)
                }
                Log.d(
                    "CHANGED",
                    "Accel values: [${localAccelValues[0]}, ${localAccelValues[1]}, ${localAccelValues[2]}]"
                )

                // Light and temperature sensors
                val tempoTemp = dataMap.getFloat("temperature")
                if (tempoTemp.toInt() != 0) {
                    localTemperature = dataMap.getFloat("temperature")
                    Log.d("CHANGED", "Temperature: $localTemperature degrees C")
                }
                val tempoLight =dataMap.getFloat("light")
                if (tempoLight.toInt() != 0) {
                    localLight = tempoLight
                    Log.d("CHANGED", "Light sensor: $localLight lux")
                }

                // Store the current gyrometer values
                val tempoGyroValues: FloatArray? = dataMap.getFloatArray("gyrometer")
                if (tempoGyroValues != null) {
                    // Reassign the gyro values if a non-null value has been received
                    localGyroValues = tempoGyroValues
                    // Set the timestamp for when the gyro values were saved into the app
                    gyroTimestamp = System.nanoTime()
//                    Log.d("CHANGED", "Gyro values: [${localGyroValues[0]}, ${localGyroValues[1]}, ${localGyroValues[2]}]")
                    Log.d("CHANGED", "Gyro timestamp: $gyroTimestamp")
                }

            }
        }
    }

    /**
     * Method used when an instance of the app is first created.
     * Performs required setup for the audio synthesis components (opening the Pure Data patch).
     * Also sets the mobile app's UI components, and methods for retrieving sensor data from the wearable.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load PD patch via Kortholt after extracting from zip file - patch is used to generate audio
        lifecycleScope.launch { kortholt.openPatch(R.raw.passive_inputs_test, "passive_inputs_test.pd", extractZip = true) }
        setContent {
            MainCompanionAppLayout(localHeartRate, localAccelValues, localGyroValues, gyroTimestamp,
                localLight, localTemperature)
            // Keep the screen on/disable locking the phone while the app is in the foreground
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

    }
}

/**
 * Contains the main structure of the App
 * the structure is extremely likely to change after MVP (most valuable player) is completed
 *
 * @param localHeartRate (float) the heart rate value currently stored by the app (in BPM)
 * @param localAccelValues (float array) accelerometer's acceleration values in the x, y, z axes (m/s^2).
 *                         Values of 999.00 are sent if the sensor is offline/disabled.
 * @param localGyroValues (float array) gyroscope sensor's values in the x, y, z axes (rads/s).
 *                         Values of 999.00 are sent if the sensor is offline/disabled.
 * @param gyroTimestamp (float) A timestamp for the last gyro sensor value update into the app.
 * @param localLight (float) measured light intensity (in lux).
 *                  Valid wearable values are bounded to the range
 *                  [0, 5000] lux. A value of 999 is sent if the sensor is offline/disabled.
 * @param localTemperature (float) measured ambient temperature (in degrees Celsius).
 *                  Valid wearable values are bounded to the range
 *                  [0, 55] degrees Celsius. A value of 999 is sent if the sensor is offline/disabled.
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompanionAppLayout(localHeartRate: Float, localAccelValues: FloatArray,
                           localGyroValues: FloatArray, gyroTimestamp: Long,
                           localLight: Float, localTemperature: Float) {

    // visualizer note states
    var visualizerNoteMagnitude by remember { mutableFloatStateOf(0.0F) }
    var visualizerNoteFlipGyro by remember { mutableFloatStateOf(0.0F) }
    var visualizerNoteTwistGyro by remember { mutableFloatStateOf(0.0F) }


    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.mediumTopAppBarColors  (
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Rubatone")
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

            // Change music tempo
            TempoControl(localHeartRate)

            // Send the light values to the PD patch
            LightControl(localLight)

            // Send the temperature values to the PD patch
            TemperatureControl(localTemperature)

            // Changes the music synth dynamics/volume

            DynamicsControl(localAccelValues, updateVisualizerMagnitude = { newVal ->
                visualizerNoteMagnitude = newVal
            })

            // Changes the music synth pitch (0.0f - fully continuous)
            val pitchControlType = 2.0f
            PitchControl(localGyroValues, pitchControlType, gyroTimestamp, updateVisualizerX = { newVal ->
                visualizerNoteFlipGyro = newVal
            }, updateVisualizerZ = { newVal ->
                visualizerNoteTwistGyro = newVal
            })

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
            AudioVisualization(localHeartRate, localLight, visualizerNoteMagnitude, visualizerNoteFlipGyro, visualizerNoteTwistGyro)
        }
    }
}









@Composable
fun AudioVisualization(localHeartRate: Float, localLight: Float, visualizerNoteMagnitude: Float,
                       visualizerNoteFlipGyro: Float, visualizerNoteTwistGyro: Float) {

    var audioBGColor = Color.Black

    // if light value (lux) detected from watch is less than 25 switch the background color to white
    if (localLight <= 25) {
        audioBGColor = Color.White
    }
    Log.d("BGCOLOR", "Light sensor: $localLight lux, $audioBGColor")

    Column(modifier = Modifier
        .fillMaxSize()
        .background(audioBGColor)
    )
    {
        Row(Modifier.fillMaxWidth(), Arrangement.End) {
            Box(Modifier.padding(12.dp, 8.dp), contentAlignment = Alignment.Center) {
                HeartPulse(localHeartRate) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = HeartShape,
                        modifier = Modifier.size(80.dp),
                        content = {}
                    )
                }
                VisualizationText(localHeartRate)
            }
        }

        Row(
            Modifier
                .fillMaxSize(),
            Arrangement.Center,
            Alignment.CenterVertically) {
            VisualizerNote(localHeartRate, localLight, visualizerNoteMagnitude, visualizerNoteFlipGyro, visualizerNoteTwistGyro)
        }
    }
}


@Composable
fun VisualizationText(sensorVal: Float) {
    Column(modifier = Modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("${sensorVal.roundToInt()}", fontSize = 15.sp, color = Color.White)
    }
}

@Composable
fun VisualizerNote(localHeartRate: Float, localLight: Float, visualizerNoteMagnitude: Float,
                   visualizerNoteFlipGyro: Float, visualizerNoteTwistGyro: Float) {

    Log.d("note-mag", "${visualizerNoteMagnitude}")
    Log.d("note-gyro", "noteX:${visualizerNoteFlipGyro}, noteZ:${visualizerNoteTwistGyro}")

    var noteColor = 0
    var noteSize = 200.dp

    if (localLight.roundToInt() <= 25) {
        if (localHeartRate < 80) {
            noteColor = R.drawable.blackcrotchet
        } else if (localHeartRate >= 80 && localHeartRate < 120){
            noteColor = R.drawable.blacknote
        } else if (localHeartRate >= 120 && localHeartRate < 201) {
            noteColor = R.drawable.blacksemiquaver
        }   else if (localHeartRate >= 201) {
            noteColor = R.drawable.blackcrotchetrest
        }
    } else {
        if (localHeartRate < 80) {
            noteColor = R.drawable.whitecrotchet
        } else if (localHeartRate >= 80 && localHeartRate < 120){
            noteColor = R.drawable.whitenote
        } else if (localHeartRate >= 120 && localHeartRate < 201) {
            noteColor = R.drawable.whitesemiquaver
        } else if (localHeartRate >= 201) {
            noteColor = R.drawable.whitecrotchetrest
        }
    }


    if (visualizerNoteMagnitude > 8) {
        noteSize = 300.dp
    }


    Box(
        modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Image(painter = painterResource(id = noteColor),
            contentDescription = "Note",
            modifier = Modifier
                .size(noteSize)
                .graphicsLayer {
                    this.rotationX = visualizerNoteFlipGyro
//                this.rotationY = 100f
                    this.rotationZ = visualizerNoteTwistGyro
                }
        )
    }
}

@Composable
fun HeartPulse(localHeartRate: Float, content: @Composable () -> Unit) {
    var heartRateSpeedInMiliSecs by rememberSaveable { mutableIntStateOf(0) }
    heartRateSpeedInMiliSecs = 400
    if (localHeartRate.roundToInt() < 900) {
        heartRateSpeedInMiliSecs = 60000 / localHeartRate.toInt()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(heartRateSpeedInMiliSecs),
            repeatMode = RepeatMode.Reverse,
        ), label = "",
    )


    Box(modifier = Modifier.scale(scale)) {
        content()
    }
}

val HeartShape: Shape = object : Shape {
    override fun createOutline(
        size: androidx.compose.ui.geometry.Size,
        layoutDirection: androidx.compose.ui.unit.LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            heartPath(size = size)
            close()
        }
        return Outline.Generic(path)
    }
}

private fun Path.heartPath(size: androidx.compose.ui.geometry.Size): Path {
    val width: Float = size.width
    val height: Float = size.height

    moveTo((width / 2).toFloat(), (height / 5).toFloat())

    cubicTo(
        (5 * width / 14).toFloat(), 0f,
        0f, (height / 15).toFloat(),
        (width / 28).toFloat(), (2 * height / 5).toFloat()
    )

    cubicTo(
        (width / 14).toFloat(), (2 * height / 3).toFloat(),
        (3 * width / 7).toFloat(), (5 * height / 6).toFloat(),
        (width / 2).toFloat(), height.toFloat()
    )

    cubicTo(
        (4 * width / 7).toFloat(), (5 * height / 6).toFloat(),
        (13 * width / 14).toFloat(), (2 * height / 3).toFloat(),
        (27 * width / 28).toFloat(), (2 * height / 5).toFloat()
    )

    cubicTo(
        width.toFloat(), (height / 15).toFloat(),
        (9 * width / 14).toFloat(), 0f,
        (width / 2).toFloat(), (height / 5).toFloat()
    )
    return this
}














/** HeartRateMonitor(localHeartRate)
 *
 * displays heart rate passed from parents
 * */
//@Composable
//fun HeartRateMonitor(localHeartRate: Float) {
//    // state variable that represents the current heart rate, initialized to 0 for ease of testing
//        androidx.compose.material3.Icon(imageVector = Icons.Default.Favorite, contentDescription ="", Modifier.size(120.dp) )
//        Text(text = "${localHeartRate.roundToInt()} BPM", fontSize = 35.sp)
//
//}


/**
 * Creates a button that can be used to turn the audio synthesis component on or off.
 * When starting/stopping audio, a float value is sent to the PD patch via Kortholt to toggle the
 * change. The audio stream must also be started/stopped by Kortholt.
 * When turning audio off, a fade out is applied to the audio to mute it over a period of 4ms.
 * This is applied so that there are no clicks/pops in the audio. Adding a delay of 5ms between
 * toggling the audio off in the PD patch and closing the Kortholt audio stream should allow for
 * the fade out to finish completely. If there are still pops/clicks occurring when the audio
 * is toggled on/off, this delay amount may need to be increased slightly.
 *
 * Note: I was noticing some issues with audio artefacts when pressing the play/pause buttons. I
 * tried removing the starting/stopping of audio streams triggered by these buttons, and this seemed
 * to fix the issue. As the audio stream is appropriately started/stopped when onPause and onResume
 * are called, it is likely not necessary to re-initialise or destroy the audio stream each time
 * the play/stop button is pressed. I've left the previous implementation commented out, in case
 * we need to switch back to this previous implementation.
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
        /*
        runBlocking {
            // Start audio stream
            scope.launch { kortholt.startStream() }
            Log.d("K START", "Kortholt stream is starting")
        }
        */
        // Toggle playback in PD patch by sending float value of 1.0
        kortholt.sendFloat("appOnOff", 1.0f)
        // Change playback status after playback has been started (within the start/stop button)
    }

    // Stop audio (isPlaying = false)
    else {
        // Toggle audio off in PD patch by sending float value of 0
        kortholt.sendFloat("appOnOff", 0.0f)
        /*
        runBlocking {
            scope.launch {
                // Apply a 5ms delay to allow the PD patch to fade audio out (within 4ms)
                delay(5)
                // After the delay, stop the audio stream
                kortholt.stopStream()
            }
            Log.d("K STOP", "Kortholt stream is stopping")
        }
        */
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



/**
 * Sends the ambient temperature sensor value currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The value is periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the state (structure and timbre) of the musical content.
 * These values are both discretised and used as fully continuous values in various ways.
 *
 * @param temperature (float) measured ambient temperature (in degrees Celsius).
 *                  Valid wearable values are bounded to the range
 *                  [0, 55] degrees Celsius. A value of 999 is sent if the sensor is offline/disabled.
 */
@Composable
fun TemperatureControl(temperature: Float) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    Log.d("TEMPERATURE", "Temperature value sent to PD: $temperature degrees Celsius")
    // Call function to modify value in PD patch
    kortholt.sendFloat("appTemperature", temperature)
}

/**
 * Sends the measured light intensity sensor value currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The value is periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the state (structure and timbre) of the musical content.
 * These values are both discretised and used as fully continuous values in various ways.
 *
 * @param light (float) measured light intensity (in lux).
 *                  Valid wearable values are bounded to the range
 *                  [0, 5000] lux. A value of 999 is sent if the sensor is offline/disabled.
 */
@Composable
fun LightControl(light: Float) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    Log.d("LIGHT", "Light value sent to PD: $light Lux")
    // Call function to modify value in PD patch
    kortholt.sendFloat("appLight", light)
}


/**
 * Sends the accelerometer values currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The accelerometer raw values are periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the volume/dynamics of a
 * pulsing sine tone synth - 'stronger' hand movements will correspond to a louder sound/stronger
 * dynamics. If no new value is received/values of 0 acceleration are sent, the volume of the
 * synth is implemented in the PD patch to ramp down to 0 (no sound) in 500ms.
 *
 * @param accelValues (float array) accelerometer's acceleration values in the x, y, z axes (m/s^2).
 *                    Values of 999.00 are sent if the sensor is offline/disabled.
 */
@Composable
fun DynamicsControl(accelValues: FloatArray, updateVisualizerMagnitude: (Float) -> Unit) {
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

        // change visualizer magnitude
        updateVisualizerMagnitude(accelMagnitude)
    }
    Log.d("ACCEL", "PD input value: $accelMagnitude")
    // Send the magnitude input value to the PD patch
    kortholt.sendFloat("appAccelerometer", accelMagnitude)
    /*
    If you need to test the gyrometer/pitch controls only, and don't want to test the accelerometer/
    dynamics, you can comment out the sendFloat method call above and send a hardcoded value (eg. 0.5)
    instead.
     */
//    kortholt.sendFloat("appAccelerometer", 0.5f)
}


/**
 * Takes the accelerometer values for each positional axis (x, y, z), then attempts to isolate
 * and remove the force of gravity from the measured accelerometer values. Gives a sense of the
 * linear acceleration in each positional axis such that the force of gravity is ignored.
 *
 * For more information about this, see here:
 *  * https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-accel
 *  *
 *  * Another potential implementation for this would involve taking data directly from
 *  * Sensors.TYPE_LINEAR_ACCELERATION from the wearable. This may be more useful/accurate data that
 *  * potentially utilises sensor fusion to give more accurate readings/values. For more info,
 *  * see here: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear
 *
 * @param accelValues (float array) accelerometer's acceleration values in the x, y, z axes (m/s^2).
 *  *                                  Values of 999.0 are sent if the sensor is offline/disabled;
 *  *                                  however, implementation of this method assumes that it won't be
 *  *                                  called if this is the case.
 *  * @return (float array) the linear acceleration values in the x, y, z axes (m/s^2)
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


/**
 * Sends the gyro sensor values currently stored in the companion app
 * to the Kortholt/PD patch (for audio synthesis).
 * The gyro raw values are periodically updated from wearable
 * sensor data.
 * The value sent to the PD patch changes the pitch/frequency of a
 * pulsing sine tone synth. The rotation vector of the gyro sensor is calculated and used to determine
 * the rotation of the watch in the y axis, corresponding to vertical rotations of the arm
 * if it was held directly in front of the user's body. These rotations should be in the range
 * [-pi, pi], according to this documentation:
 * https://developer.android.com/reference/android/hardware/SensorManager#getOrientation(float[],%20float[])
 * Arm rotations pointing at an upwards angle should map to a higher pitch, and downwards angle arm
 * rotations to a lower pitch. This should allow for a range of up to 2 octaves of different pitches
 * within 12 TET (or within continuous pitch values in this range), dependant on the chosen pitch
 * format. An arm rotation position roughly perpendicular to the body should correspond to the middle
 * pitch/frequency in the given interval.
 *
 * https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-gyro
 *
 * @param gyroValues (float array) gyroscope sensor's values in the x, y, z axes (rads/s).
 *                         Values of 999.00 are sent if the sensor is offline/disabled.
 * @param pitchControlType (float) 0.0 - continuous pitch; 1.0 - pitch discretised to 12-TET;
 *                                 2.0 - pitch discretised to 12-TET with portamento/pitch bends
 *                                 in-between notes
 * @param gyroTimestamp (float) A timestamp for the last gyro sensor value update into the app.
 */
@Composable
fun PitchControl(gyroValues: FloatArray, pitchControlType: Float, gyroTimestamp: Long,
                 updateVisualizerX: (Float) -> Unit, updateVisualizerZ: (Float) -> Unit) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }

//    Log.d("GYRO", "Gyro values: [${gyroValues[0]}, ${gyroValues[1]}, ${gyroValues[2]}]")

    // Stores the pitch rotation (rotation about the wearable's x axis)
    var pitchRotation = 0.0f
//    var height = 0.0f

    // If the sensor is offline/disabled, send a value of 999.00 directly to PD patch
    if (!gyroValues.contentEquals(floatArrayOf(999.00f, 999.00f, 999.00f))) {
        // If the sensor is online:

        // Get the current rotation vector based on the gyro sensor readings
        var rotationCurrent = getGyroCurrentRotation(gyroValues, gyroTimestamp)

        /*
        Get the orientation values in each axis (azimuth/z, pitch/x, roll/y) based on current rotation.
        The roll (y axis rotation) values should be in the range [-pi, pi].
        For more info about the values received, see here:
        https://developer.android.com/reference/android/hardware/SensorManager#getOrientation(float[],%20float[])
         */
        val orientation = floatArrayOf(0.0f, 0.0f, 0.0f)
        SensorManager.getOrientation(rotationCurrent, orientation)
//        Log.d("GYRO", "Orientation: [${orientation[0]}, ${orientation[1]}, ${orientation[2]}]")
        pitchRotation = orientation[2].toFloat()

        // convert pitchRotation(radians) to degrees and update the note visualizer
        updateVisualizerX((pitchRotation * (180/ PI)).toFloat())
        updateVisualizerZ((orientation[1] * (180/ PI)).toFloat())


        // Tried calculating an approx. height value with the assumption that the user's arm is exactly 1m long - this didn't work better
//        height = sin(pitchRotation.toDouble()).toFloat()
    }

//    Log.d("GYRO", "PD input value: $pitchRotation")
    // Set the pitch control type in the PD patch
    kortholt.sendFloat("appPitchControl", pitchControlType)
    // Send the rotation value in the x axis (pitch) to the PD patch
    kortholt.sendFloat("appGyrometer", pitchRotation)

//    Log.d("GYRO", "Height: $height")
}

/**
 *  Takes the input gyroscope values currently saved in the app, then converts these to a rotation
 *  vector from which rotational motion data can be more appropriately retrieved.
 *  Uses the previously stored 'current rotation vector' to determine what the new current rotation
 *  vector will be, based on the change in values.
 *
 *  Another potential implementation for this would involve taking data directly from
 *  Sensors.TYPE_ROTATION_VECTOR from the wearable. This may be more useful/accurate data that
 *  potentially utilises sensor fusion to give more accurate readings/values that aren't as prone
 *  to drift (drift is due to a single integration of sensor values). For more info,
 *  see here: https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-rotate
 *  @param gyroValues (float array) gyroscope sensor's values in the x, y, z axes (rad/s).
 *                         Values of 999.00 are sent if the sensor is offline/disabled.
 *  @param gyroTimestamp (long) A timestamp for the last gyro sensor value update into the app.
 *  @return rotationCurrent (float array) the current rotation vector. For more info about the values
 *                          in this var, see here:
 *                          https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-rotate
 *                          (this is for a built-in sensor fusion version of this method, but the
 *                          values returned by this method are of the same format)
 */
@Composable
fun getGyroCurrentRotation(gyroValues: FloatArray, gyroTimestamp: Long): FloatArray {
    // Create a constant to convert nanoseconds to seconds.
    val NS2S = 1.0f / 1000000000.0f
    // The maximum allowable margin of error for the gyro's angular speed (may need to tweak this value)
    var EPSILON = 0.1f

    /*
    This timestep's delta rotation (change in rotation) is multiplied by the current rotation
    after computing it from the gyro sample data
     */
    val deltaRotationVector = floatArrayOf(0.0f, 0.0f, 0.0f, 0.0f)
    // Store a timestamp to compare the last recorded time with the most recent gyro value update time
    var timestamp by rememberSaveable { mutableStateOf(0.0f) }
    // Store the current rotation matrix values - these are initialised as a 3x3 identity matrix, then updated based on gyro changes
    var rotationCurrent by rememberSaveable { mutableStateOf(floatArrayOf(1.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 0.0f, 1.0f)) }

    if (timestamp != 0.0f) {
        // Get the difference in time between the last changed timestamp and last gyro sensor update
        val dT = (gyroTimestamp - timestamp) * NS2S
        // Axis of the rotation sample, not normalized yet
        var axisX: Float = gyroValues[0]
        var axisY: Float = gyroValues[1]
        var axisZ: Float = gyroValues[2]

        // Calculate the angular speed of the sample
        val omegaMagnitude: Float = sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ)

        /*
        Normalize the rotation vector if it's big enough to get the axis
        (that is, EPSILON should represent your maximum allowable margin of error)
         */
        if (omegaMagnitude > EPSILON) {
            axisX /= omegaMagnitude
            axisY /= omegaMagnitude
            axisZ /= omegaMagnitude
        }

        /*
        Integrate around this axis with the angular speed by the timestep
        in order to get a delta rotation from this sample over the timestep
        We will convert this axis-angle representation of the delta rotation
        into a quaternion before turning it into the rotation matrix.
         */
        val thetaOverTwo: Float = omegaMagnitude * dT / 2.0f
        val sinThetaOverTwo: Float = sin(thetaOverTwo)
        val cosThetaOverTwo: Float = cos(thetaOverTwo)
        deltaRotationVector[0] = sinThetaOverTwo * axisX
        deltaRotationVector[1] = sinThetaOverTwo * axisY
        deltaRotationVector[2] = sinThetaOverTwo * axisZ
        deltaRotationVector[3] = cosThetaOverTwo
    }
//    Log.d("GYRO", "Delta rotation vector: [${deltaRotationVector[0]}, ${deltaRotationVector[1]}, ${deltaRotationVector[2]}, ${deltaRotationVector[3]}]")
    // Reassign the stored timestamp to the most recent gyro sensor retrieval time
    timestamp = gyroTimestamp.toFloat() ?: 0.0f
    // Get the rotation matrix for the change in rotation
    val deltaRotationMatrix = FloatArray(9) { 0f }
    SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector)
//    Log.d("GYRO", "Delta rotation matrix: [${deltaRotationMatrix[0]}, ${deltaRotationMatrix[1]}, ${deltaRotationMatrix[2]}, ${deltaRotationMatrix[3]}, ${deltaRotationMatrix[4]}, ${deltaRotationMatrix[5]}, ${deltaRotationMatrix[6]}, ${deltaRotationMatrix[7]}, ${deltaRotationMatrix[8]}]")
    /*
    Concatenate the delta rotation matrix we computed with the current rotation
    in order to get the updated rotation (perform a matrix multiplication to get the updated rotation).
    If the current rotation matrix is all zeros (hasn't been instantiated
     */
    rotationCurrent = matrixMultiplication3x3(rotationCurrent, deltaRotationMatrix)
//    Log.d("GYRO", "Current rotation: [${rotationCurrent[0]}, ${rotationCurrent[1]}, ${rotationCurrent[2]}, ${rotationCurrent[3]}, ${rotationCurrent[4]}, ${rotationCurrent[5]}, ${rotationCurrent[6]}, ${rotationCurrent[7]}, ${rotationCurrent[8]}]")
    return rotationCurrent
}

/**
 * Multiply two one-dimensional 9-element arrays together
 * (the arrays are intended to be interpreted as 3x3 matrices, but are stored as 1D arrays for ease
 * of use/value access in Kotlin).
 * I've hardcoded this in and it's terrible but in my defence this will only ever be used by the
 * getGyroCurrentRotation method for the purpose of multiplying two matrices of specific dimensions
 * together.
 *
 * @param array1: (float array) the first 3x3 matrix (9 element array)
 * @param array2: (float array) the second 3x3 matrix (9 element array)
 * @return (float array) the 3x3 matrix (9 element array) resulting from the matrix multiplication
 */
fun matrixMultiplication3x3(array1: FloatArray, array2: FloatArray): FloatArray {
    var result = FloatArray(9) { 0f }

    // First row
    result[0] = array1[0]*array2[0] + array1[1]*array2[3] + array1[2]*array2[6]
    result[1] = array1[0]*array2[1] + array1[1]*array2[4] + array1[2]*array2[7]
    result[2] = array1[0]*array2[2] + array1[1]*array2[5] + array1[2]*array2[8]

    // Second row
    result[3] = array1[3]*array2[0] + array1[4]*array2[3] + array1[5]*array2[6]
    result[4] = array1[3]*array2[1] + array1[4]*array2[4] + array1[5]*array2[7]
    result[5] = array1[3]*array2[2] + array1[4]*array2[5] + array1[5]*array2[8]

    // Third row
    result[6] = array1[6]*array2[0] + array1[7]*array2[3] + array1[8]*array2[6]
    result[7] = array1[6]*array2[1] + array1[7]*array2[4] + array1[8]*array2[7]
    result[8] = array1[6]*array2[2] + array1[7]*array2[5] + array1[8]*array2[8]

//    Log.d("GYRO", "Matmul result: [${result[0]}, ${result[1]}, ${result[2]}, ${result[3]}, ${result[4]}, ${result[5]}, ${result[6]}, ${result[7]}, ${result[8]}]")

    return result
}