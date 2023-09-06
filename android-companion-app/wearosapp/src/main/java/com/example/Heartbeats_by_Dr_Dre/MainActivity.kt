/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package com.example.Heartbeats_by_Dr_Dre

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.example.Heartbeats_by_Dr_Dre.presentation.theme.Companion_AppTheme
import com.google.android.gms.wearable.DataItem
import com.google.android.gms.wearable.DataMapItem
import com.google.android.gms.wearable.PutDataMapRequest
import com.google.android.gms.wearable.Wearable

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
//  data transfer to mobile companion app
    val path = "/heartbeat"
    var context = LocalContext.current
    val dataClient = Wearable.getDataClient(context)
    val putDataMapRequest = PutDataMapRequest.create(path)
    val dataMap = putDataMapRequest.dataMap
    dataMap.putInt("heart-rate", 150)
    val putDataRequest = putDataMapRequest.asPutDataRequest().setUrgent()


    Companion_AppTheme {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {

                val task = dataClient.putDataItem(putDataRequest)

                task.addOnCompleteListener { task ->
                    Log.e("HANDHELD", "Transfer data to handheld")
                    if (task.result != null) {
                        val dataMapItem = DataMapItem.fromDataItem(task.result!!).dataMap
                        Log.d("item", dataMapItem.toString())
                    }
                }
            }) {
                Text(text = "transfer")
            }
        }
    }
}

//@Preview(device = Devices.WEAR_OS_SMALL_ROUND, showSystemUi = true)
//@Composable
//fun DefaultPreview() {
//    WearApp("Preview Android")
//}