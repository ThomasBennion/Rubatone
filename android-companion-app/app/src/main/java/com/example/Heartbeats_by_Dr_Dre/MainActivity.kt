package com.example.Heartbeats_by_Dr_Dre

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.Wearable
import kotlin.math.roundToInt

class MainActivity : ComponentActivity(), DataClient.OnDataChangedListener {

    // triggers when application tab is navigated off/ tabs switched/ phone screen locked
    override fun onPause() {
        super.onPause()
        Wearable.getDataClient(this).removeListener(this)
        Log.d("PAUSED", "listener is paused")
    }

    // Triggers when application tab is navigated back to / resumed
    public override fun onResume() {
        super.onResume()
        Wearable.getDataClient(this).addListener(this)
        Log.d("RESUME", "listener is resumed")
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                ExtendedFloatingActionButton(
                    onClick = {  },
                ) {
                    androidx.compose.material3.Icon(imageVector = Icons.Default.Close, contentDescription ="", Modifier.size(30.dp))
                    Text(text = "Mute")
                }
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
