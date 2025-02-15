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
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonColors
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.example.Heartbeats_by_Dr_Dre.presentation.theme.Companion_AppTheme
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.CancellationException
import java.text.DecimalFormat

/**
 * The ethics of the watch app:
 * We consider ethics to be a crucial consideration when designing our watch app.
 *
 * The primary ethical considerations we consider to be our responsibility are:
 * 1. Our responsibility for handling the users data in a way that ensures transparency and
 * privacy for the user
 * 2. Our duty of care in ensuring our users are not encouraged to hurt themselves when
 * using our application
 *
 * Handling User Data:
 * When handling user data, we need to ensure the user is aware of what data is being used
 * and how is it being used. This should be done before the user interacts with the application
 *
 * - Google provides the user with a need for consent when using the bio-metric readings,
 * we consider this acceptable.
 * - We must make sure the data is not being used in ways that might conflict with what
 * the user expects of us. To do this, we shouldn’t send any data to a “middleman”,
 * instead we should have the data only be sent between the watch app and the phone app,
 * therefore ensuring the data can’t be used for anything else.
 * - The user expects us to use the data only for the functions they can perceive,
 * (i.e. to create music). Therefore we shouldn’t require any sensor or private
 * information for any other function of the app.
 *
 * Duty of Care:
 * When it comes to our duty of care, we have to decide on the line between what is
 * inhibiting the users ability to use the application, and what is encouraging undue
 * risk to our users. This is also difficult as health is very dependent on a persons
 * age and overall health status.
 * Fortunately for us, the “Guiness Book of World Records” was faced with a very similar issue,
 * so we can apply their reasoning to our own sensors.
 *
 * For our use case then, we shouldn’t encourage users to perform activities that are both:
 * - Risky to a users long term health
 * - An average, unqualified person could reasonably try and accomplish
 *
 * Heart Rate:
 * While a person could artificially increase their heart rate to risky places via drugs,
 * this is far more difficult to do then trying to lower your heart rate, which you can do by
 * holding your breath for long periods of time
 * (natural response of the body to preserve resources) therefore we should put much more
 * stringent checks in place to ensure the user doesn’t lower their heart rate too low.
 * Therefore, a heart rate in the range of 50 - 200 bpm is acceptable, while it is possible to
 * have a heart rate lower then 50 and still be healthy (such as if you are an athlete) the
 * risk of our users doing it by unhealthy means is too great.
 *
 * Gyrometer + Accelerometer:
 * For these sensors, there’s not much need to put a limiter on them.
 * While it is possible to permanently harm yourself by moving your hand too quickly,
 * or in a awkward direction, we should just make sure the user isn’t “rewarded” in any way by
 * doing this, and only make sensors in the safe range. Due to the nature of how these
 * sensors output music, we can make it so putting your arm in an awkward position causes a
 * negative value to be sent, therefore confusing and stopping the applications output.
 *
 * Temperature:
 * Considering that this sensor is on the persons wrist, it is reasonable to assume
 * that a healthy temperature to be on the persons skin is acceptable.
 * Therefore, our range will be 0 – 55 degrees celcius, if the temperature goes lower or higher,
 * it shouldn’t be affecting the watch’s sensor
 *
 * Light Sensor:
 * There isn’t a need to put a limit on the light sensor. While it is possible to blind yourself
 * with very bright lights, it is too difficult for the watch to track (as it depends on the
 * amount of light your eyes are taking in). Therefore we do not consider miss-use of this sensor
 * to fall into our duty of care, as we cannot control it.
 * */

class MainActivity : ComponentActivity() {
    /** onCreate()
     * Requests permission, handles startup activities, then starts the program
     * Note that this isn't a composable function, so don't attempt much else
     * If it does have to handle something, it should be passed to a composable function
     * (Such as the navigation controls)
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        var startScreen = "home_screen"
        if (checkSelfPermission("android.permission.BODY_SENSORS")
            != PackageManager.PERMISSION_GRANTED ||
            checkSelfPermission("android.permission.ACTIVITY_RECOGNITION")
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                (arrayOf(
                    "android.permission.BODY_SENSORS",
                    "android.permission.ACTIVITY_RECOGNITION"
                )), 1);
            startScreen = "check_permissions"
        }
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberSwipeDismissableNavController()
            SwipeDismissableNavHost(
                navController = navController,
                startDestination = startScreen
            ) {
                composable("home_screen") {
                    HomePage(onButtonPress = { navController.navigate("sensor_list")})
                }
                composable("sensor_list") {
                    SensorList(onBackPress = { navController.navigate("home_screen")})
                    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                composable("check_permissions") {
                    RequestPermissions(onSuccess = { navController.navigate("home_screen")})
                }
            }
        }
    }

        companion object {
        private const val TAG = "MainActivity"
        private const val HEART_RATE_PATH = "/heart_rate"
        private const val HEART_RATE_KEY = "heart_rate"

    }


}

/** HomePage()
 * Function for the home page, handles everything inside the function
 * This is the page users will receive on startup of the application, it shouldn't handle sensors
 * You can assume the user has granted permissions for the sensors
 *
 * @onButtonPress: Action to be taken on pressing the input button
 * @return: Home Page
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomePage(onButtonPress: (() -> Unit)) {

    Companion_AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(80.dp),
                    colors = TopAppBarDefaults.topAppBarColors  (
                        containerColor = MaterialTheme.colors.background,
                        titleContentColor = MaterialTheme.colors.onSurface,
                    ),
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.background),
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.baseline_directions_walk_24),
                                contentDescription ="",
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            androidx.compose.material3.Text(
                                text = "Rubatone",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.background,
                ) {
                    //
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = onButtonPress,
                    Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                ) {
                    androidx.compose.material3.Text(
                        text = "Start",
                        fontSize = 30.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

/** RequestPermissions()
 * Function for requesting permissions, handles everything inside the function
 * This page should only appear on first startup of the application,
 * or if the user hasn't given required permissions.
 *
 * @onSuccess: Action to be taken on pressing the connect button with required sensors
 * @return: page requesting permissions
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestPermissions(onSuccess: (() -> Unit)) {
    val context = LocalContext.current
    val permissionStatus = object {
        fun CheckPermissions() {
            if (context.checkSelfPermission("android.permission.BODY_SENSORS")
                == PackageManager.PERMISSION_GRANTED &&
                context.checkSelfPermission("android.permission.ACTIVITY_RECOGNITION")
                == PackageManager.PERMISSION_GRANTED) {
                    onSuccess()
            }
        }
    }
    Companion_AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(80.dp),
                    colors = TopAppBarDefaults.topAppBarColors  (
                        containerColor = MaterialTheme.colors.background,
                        titleContentColor = MaterialTheme.colors.onSurface,
                    ),
                    title = {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colors.background),
                            verticalArrangement = Arrangement.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector =
                                ImageVector.vectorResource(R.drawable.baseline_directions_walk_24),
                                contentDescription ="",
                                modifier = Modifier
                                    .size(20.dp)
                                    .align(Alignment.CenterHorizontally)
                            )
                            androidx.compose.material3.Text(
                                text = "Rubatone",
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                )
            },
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .height(50.dp)
                        .fillMaxWidth(),
                    containerColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.background,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { permissionStatus.CheckPermissions() },
                            Modifier
                                .size(width = 80.dp, height = 40.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            androidx.compose.material3.Text(
                                text = "Connect",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
                androidx.compose.material3.Text(
                    text = "Permissions are needed to run this application.\n" +
                            "If you have trouble running this application, " +
                            "please go to settings and grant the required permissions",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .padding(innerPadding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    textAlign = TextAlign.Center
                )
        }
    }
}

/** SensorList()
 * Main Function for sensor list, handles top level styling
 * It should handle styling only, and call functions to handle page functionality
 *
 * @onBackPress: Action to be taken when you click to go back
 * @return: Page of Sensors
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorList(onBackPress: (() -> Unit)) {
    Companion_AppTheme {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    modifier = Modifier.height(40.dp),
                    colors = TopAppBarDefaults.topAppBarColors  (
                        containerColor = MaterialTheme.colors.background,
                        titleContentColor = MaterialTheme.colors.onSurface,
                    ),
                    title = {
                        TextButton(
                            onClick = onBackPress,
                            Modifier
                                .fillMaxSize(),
                        ) {
                            androidx.compose.material3.Text("< Stop")
                        }
                    }
                )
            },

        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(innerPadding)
                    .background(MaterialTheme.colors.background)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center
            ) {
                HeartRateSensor()
                GyrometerSensor()
                AccelerometerSensor()
                TemperatureSensor()
                LightSensor()
                //BarometerSensor()
                androidx.compose.material3.Text("\n\n")
            }
        }
    }
}

/** HeartRateSensor()
 * Handles the heart rate sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live heart rate
 * @return Float of data for Heart Rate Sensor
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

    val heartRateSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    val default: Float = 999.00F
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val heartRateSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

//        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_HEART_RATE) {
                if (sensorOn.value) {
                    if (event.values[0] in 50.0..200.0){
                        sensorStatus.value = event.values[0]
                        sendHeartRateToPhone(sensorStatus.value)
                    } else {
                        buttonOn.value = disabledButton
                    }
                } else {
                    sensorStatus.value = 999.00F
                }
            }
        }

//        send heart rate data to phone
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
        SensorManager.SENSOR_DELAY_NORMAL
    )

    SensorButton(
        onclick = { heartRateSensorListener.toggleSensor() },
        image = Icons.Default.Favorite,
        text = "Heart Rate", //\n ${sensorStatus.value} bpm",
        buttonOn = buttonOn
    )
}

/** GyrometerSensor()
 * Handles the gyrometer sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live gyrometer reading
 * @return FloatArray of data for gyrometer sensor
 */
@Composable
fun GyrometerSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val gyrometerPath = "/gyrometer"
    val gyrometerKey = "gyrometer"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val gyrometerSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    val default = floatArrayOf(999.00F, 999.00F, 999.00F)
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val gyrometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values
                    sendGyroscopeToPhone(sensorStatus.value)
                } else {
                    sensorStatus.value = default
                }
            }
        }

        //        send gyrometer data to phone
        private fun sendGyroscopeToPhone(gyrometerValue: FloatArray) {
            try {
                val putDataMapRequest = PutDataMapRequest.create(gyrometerPath)
                val dataMap = putDataMapRequest.dataMap
                dataMap.putFloatArray(gyrometerKey, gyrometerValue)
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
        gyrometerSensorListener,
        gyrometerSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    val df = DecimalFormat("#.##")
    SensorButton(
        onclick = { gyrometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_3d_rotation_24),
        text = "Gyrometer", /*+
                "\n ${df.format(sensorStatus.value[0])}x" +
                " · ${df.format(sensorStatus.value[1])}y" +
                " · ${df.format(sensorStatus.value[2])}z"*/
        buttonOn = buttonOn
    )
}

/** AccelerometerSensor()
 * Handles the accelerometer sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live accelerometer reading
 * @return FloatArray of data for accelerometer sensor
 */
@Composable
fun AccelerometerSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val accelerometerPath = "/accelerometer"
    val accelerometerKey = "accelerometer"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val accelerometerSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    val default = floatArrayOf(999.00F, 999.00F, 999.00F)
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val accelerometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values
                    sendAccelerometerToPhone(sensorStatus.value)
                } else {
                    sensorStatus.value = default
                }
            }
        }

        //        send accelerometer data to phone
        private fun sendAccelerometerToPhone(accelerometerValue: FloatArray) {
            try {
                val putDataMapRequest = PutDataMapRequest.create(accelerometerPath)
                val dataMap = putDataMapRequest.dataMap
                dataMap.putFloatArray(accelerometerKey, accelerometerValue)
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
        accelerometerSensorListener,
        accelerometerSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    )

    val df = DecimalFormat("#.##")
    SensorButton(
        onclick = { accelerometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_waving_hand_24),
        text = "Accelerometer", /*+
                "\n ${df.format(sensorStatus.value[0])}x" +
                " · ${df.format(sensorStatus.value[1])}y" +
                " · ${df.format(sensorStatus.value[2])}z"*/
        buttonOn = buttonOn
    )
}

/** TemperatureSensor()
 * Handles the temperature sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live temperature
 * @return Float of data for temperature sensor
 */
@Composable
fun TemperatureSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val temperaturePath = "/temperature"
    val temperatureKey = "temperature"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val temperatureSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE)

    val default: Float = 999.00F
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val temperatureSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                if (sensorOn.value) {
                    if (event.values[0] in 0.0..55.0){
                        sensorStatus.value = event.values[0]
                        sendTemperatureToPhone(sensorStatus.value)
                    } else {
                        buttonOn.value = disabledButton
                    }
                } else {
                    sensorStatus.value = 999.00F
                }
            }
        }

        //        send temperature data to phone
        private fun sendTemperatureToPhone(temperatureValue: Float) {
            try {
                val putDataMapRequest = PutDataMapRequest.create(temperaturePath)
                val dataMap = putDataMapRequest.dataMap
                dataMap.putFloat(temperatureKey, temperatureValue)
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
        temperatureSensorListener,
        temperatureSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    SensorButton(
        onclick = { temperatureSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_thermostat_24),
        text = "Temperature",//\n ${sensorStatus.value} °C"
        buttonOn = buttonOn
    )
}

/** BarometerSensor()
 * Handles the barometer sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live barometer
 * @return Float of data for barometer sensor
 */
@Composable
fun BarometerSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val barometerPath = "/barometer"
    val barometerKey = "barometer"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val barometerSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)

    val default: Float = 999.00F
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val barometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values[0]
                    sendBarometerToPhone(sensorStatus.value)
                } else {
                    sensorStatus.value = 999.00F
                }
            }
        }

        //        send barometer data to phone
        private fun sendBarometerToPhone(barometerValue: Float) {
            try {
                val putDataMapRequest = PutDataMapRequest.create(barometerPath)
                val dataMap = putDataMapRequest.dataMap
                dataMap.putFloat(barometerKey, barometerValue)
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
        barometerSensorListener,
        barometerSensor,
        SensorManager.SENSOR_DELAY_NORMAL
    )

    SensorButton(
        onclick = { barometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_air_24),
        text = "Barometer", //\n ${sensorStatus.value} hPa"
        buttonOn = buttonOn
    )
}

/** LightSensor()
 * Handles the light sensor information
 * Try not to pass anything to this function if possible, it should be as self contained as possible
 *
 * @return Text displaying live light
 * @return Float of data for light sensor
 */
@Composable
fun LightSensor() {
    val context = LocalContext.current
    val dataClient by lazy { Wearable.getDataClient(context) }

    val lightPath = "/light"
    val lightKey = "light"


    val mSensorManager: SensorManager = LocalContext.current.getSystemService(
        ComponentActivity.SENSOR_SERVICE
    ) as SensorManager

    val lightSensor: Sensor? = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

    val default: Float = 999.00F
    val sensorStatus = remember {
        mutableStateOf(default)
    }
    val sensorOn = remember {
        mutableStateOf(true)
    }
    val defaultButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.primaryButtonColors();
    val disabledButton: ButtonColors =
        androidx.wear.compose.material.ButtonDefaults.secondaryButtonColors();
    val buttonOn = remember {
        mutableStateOf(defaultButton)
    }

    val lightSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            if (sensorOn.value) {
                buttonOn.value = disabledButton
            } else {
                buttonOn.value = defaultButton
            }
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default
        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_LIGHT) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values[0]
                    sendLightToPhone(sensorStatus.value)
                } else {
                    sensorStatus.value = 999.00F
                }
            }
        }

        //        send light data to phone
        private fun sendLightToPhone(lightValue: Float) {
            try {
                val putDataMapRequest = PutDataMapRequest.create(lightPath)
                val dataMap = putDataMapRequest.dataMap
                dataMap.putFloat(lightKey, lightValue)
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
        lightSensorListener,
        lightSensor,
        SensorManager.SENSOR_DELAY_FASTEST
    )

    SensorButton(
        onclick = { lightSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_light_mode_24),
        text = "Light",//\n ${sensorStatus.value} lx"
        buttonOn = buttonOn
    )
}

/**
 * Function for creating a sensor button
 *
 * @onclick: onclick effect, should be a toggle sensor or equivalent
 * @image: Displayed image for the sensors symbol, should be a material3 ImageVector
 * @text: The text in the button
 * @buttonOn: The colours of the button, they should reflect the state the sensor is in (on or off)
 *
 * @return Sensor Button
 */
@Composable
fun SensorButton(onclick: () -> Unit, image: ImageVector, text: String, buttonOn: MutableState<ButtonColors>) {
    Button(
        onClick = onclick,
        enabled=true,
        colors = buttonOn.value,
        modifier = Modifier
            .size(240.dp, 60.dp)
            .padding(vertical = 5.dp)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            androidx.compose.material3.Icon(imageVector = image,
                contentDescription ="", Modifier.size(40.dp))
            Text(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Left,
                text = text
            )
        }
    }
}