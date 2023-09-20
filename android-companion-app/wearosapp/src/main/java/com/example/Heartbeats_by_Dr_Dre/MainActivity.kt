/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.Heartbeats_by_Dr_Dre

import android.content.pm.PackageManager
import android.graphics.drawable.VectorDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
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

class MainActivity : ComponentActivity() {
    /** onCreate()
     * Requests permission, handles startup stuff, then starts the program
     * Note that this isn't a composable function, so don't try making it do anything else
     * If it does have to handle something, it should be passed to the WearApp() function
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
 *
 * @onButtonPress: Action to be taken on pressing the input button
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = onButtonPress,
                            Modifier
                                .size(width = 80.dp, height = 40.dp)
                                .align(Alignment.CenterHorizontally),
                        ) {
                            androidx.compose.material3.Text(
                                text = "Inputs",
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
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
 * Function for the home page, handles everything inside the function
 *
 * @onSuccess: Action to be taken on pressing the input button
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
                                text = "Retry",
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
                            "Please go to settings and grant this app the required permissions",
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onSurface,
                    modifier = Modifier
                        .background(MaterialTheme.colors.background)
                        .padding(innerPadding)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center
                )
        }
    }
}

/** SensorList()
 * Main Function for sensor list, handles top level styling
 * Note this calls functions IT SHOULD NOT HANDLE INFORMATION DISPLAY INTERNALLY
 * If it does display information, it might cause A LOT of problems for us later on
 *
 * @onBackPress: Action to be taken when you click to go back
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
                            androidx.compose.material3.Text("< Inputs")
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
                BarometerSensor()
                LightSensor()
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

    val heartRateSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

//        toggles the sensor on the wearable
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

    sensorButton(
        onclick = { heartRateSensorListener.toggleSensor() },
        image = Icons.Default.Favorite,
        text = "Heart Rate"// ${sensorStatus.value} bpm"
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

    val gyrometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
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
    sensorButton(
        onclick = { gyrometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_3d_rotation_24),
        text = "Gyrometer" /*+
                " ${df.format(sensorStatus.value[0])}x" +
                " · ${df.format(sensorStatus.value[1])}y" +
                " · ${df.format(sensorStatus.value[2])}z"*/
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

    val accelerometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
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
    sensorButton(
        onclick = { accelerometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_waving_hand_24),
        text = "Accelerometer" /*+
                " ${df.format(sensorStatus.value[0])}x" +
                " · ${df.format(sensorStatus.value[1])}y" +
                " · ${df.format(sensorStatus.value[2])}z"*/
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

    val temperatureSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                if (sensorOn.value) {
                    sensorStatus.value = event.values[0]
                    sendTemperatureToPhone(sensorStatus.value)
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

    sensorButton(
        onclick = { temperatureSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_thermostat_24),
        text = "Temperature"// ${sensorStatus.value} °C"
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

    val barometerSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
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

    sensorButton(
        onclick = { barometerSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_air_24),
        text = "Barometer" // ${sensorStatus.value} hPa"
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

    val lightSensorListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
            //
        }

        //        toggles the sensor on the wearable
        fun toggleSensor() {
            sensorOn.value = sensorOn.value != true
            sensorStatus.value = default

        }

        override fun onSensorChanged(event: SensorEvent) {
            if (event.sensor.type == Sensor.TYPE_PRESSURE) {
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

    sensorButton(
        onclick = { lightSensorListener.toggleSensor() },
        image = ImageVector.vectorResource(R.drawable.baseline_light_mode_24),
        text = "Light"// ${sensorStatus.value} lx"
    )
}

/**
 * Function for creating a sensor button
 *
 * @onclick: onclick effect, should be a toggle sensor or equivalent
 * @image: Displayed image for the sensors symbol, should be a material3 ImageVector
 * @text: The text in the button
 *
 * @return Sensor Button
 */
@Composable
fun sensorButton(onclick: () -> Unit, image: ImageVector, text: String) {
    Button(
        onClick = onclick,
        enabled=true,
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

/** DefaultPreview()
 * It's the apps preview
 * Don't Worry about this, we'll change it when we need to
 */
//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp("Preview Android", 22)
//}