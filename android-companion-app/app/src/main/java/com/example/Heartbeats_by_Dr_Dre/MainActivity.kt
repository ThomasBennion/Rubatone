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
import kotlin.math.roundToInt

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {


    // triggers when application tab is navigated off/ tabs switched/ phone screen locked
    override fun onPause() {
        // remove wearable listener when app is paused/navigated off, but not closed
        Wearable.getDataClient(this).removeListener(this)

        // Pause playback by sending float value of 0.0f
        kortholt.sendFloat("appOnOff", 0.0f)


        runBlocking {
            delay(600)
            kortholt.stopStream()
        }

        super.onPause()
    }

    // Triggers when application tab is navigated back to / resumed
    public override fun onResume() {
        // add wearable listener when tab is resumed
        Wearable.getDataClient(this).addListener(this)

        // Resume playback by sending float value of 1.0f
        runBlocking {
            kortholt.startStream()
        }
        kortholt.sendFloat("appOnOff", 1.0f)

        super.onResume()
    }

    override fun onDestroy() {
        // Pause playback by sending float value of 0.0f
        kortholt.sendFloat("appOnOff", 0.0f)
        // Stop audio stream and close patch
        runBlocking {
            kortholt.stopStream()
            kortholt.closePatch()
        }
        super.onDestroy()
    }

    /*
    * Initialize all your state variables related to sensor data here
    * */
    private var localHeartRate by mutableStateOf<Float>( 999.00F) // heart rate

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
                localHeartRate = dataMap.getFloat("heart_rate")
            }
        }
    }

//    On create
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Load PD patch
        lifecycleScope.launch { kortholt.openPatch(R.raw.pulse_mockup_one_file, "pulse_mockup_one_file.pd", extractZip = true) }
        setContent {
            MainCompanionAppLayout(localHeartRate)
        }
    }
}

/** MainCompanionAppLayout(localHeartRate)
 * Contains the main structure of the App
 * the structure is extremely likely to change after MVP is completed
 * */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainCompanionAppLayout(localHeartRate: Float) {
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
 * Controls the playback of the synth tone
 * (note on/off).
 */
@Composable
fun PlayControl(modifier: Modifier) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    val scope = rememberCoroutineScope()

    // Store state for synth playback (initialise to false)
    var isPlaying by rememberSaveable { mutableStateOf(false) }

    //   call function to modify heart rate tempo in PD patch
//    kortholt.sendFloat("appHeartRate", heartRate)


    // play audio
    if (isPlaying) {
        runBlocking {
            // Start audio stream
            scope.launch { kortholt.startStream() }
            Log.d("K START", "Kortholt stream is starting")
        }
        // Toggle playback in PD patch by sending float value of 1
        kortholt.sendFloat("appOnOff", 1.0f)
        // Change playback status after playback has been started
    }

    // Stop audio
    else {
        // Toggle playback off in PD patch by sending float value of 0
        // Change playback status after playback has been stopped
        // isPlaying = false

        kortholt.sendFloat("appOnOff", 0.0f)
        runBlocking {
            // Stop audio stream
            scope.launch {
//                delay(600)
                kortholt.stopStream()
            }
            Log.d("K STOP", "Kortholt stream is stopping")
        }

    }

    // Create a button
    ExtendedFloatingActionButton(modifier = modifier,
        // isPlaying is current true, set to false, and vice versa
        onClick = {
            isPlaying = !isPlaying

        // Play audio
            }) {
        // Change the button label
        if (!isPlaying) {
            // If stopped, set the button label to 'Play'
            Text(text = "play")
        } else {
            // If playing, set the button label to 'Stop'
            Text(text = "stop")
        }
    }
}

@Composable
fun TempoControl(heartRate: Float) {
    val context = LocalContext.current
    val kortholt = remember { context.kortholt }
    //   call function to modify heart rate tempo in PD patch
    kortholt.sendFloat("appHeartRate", heartRate)
}