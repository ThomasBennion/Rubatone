package com.example.Heartbeats_by_Dr_Dre

import android.app.Application
import android.util.Log
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.google.android.gms.wearable.DataClient
import com.google.android.gms.wearable.DataEvent
import com.google.android.gms.wearable.DataEventBuffer
import com.google.android.gms.wearable.DataMapItem

class ClientDataViewModel(application: Application) : AndroidViewModel(application),
    DataClient.OnDataChangedListener {
    private val _events = mutableStateListOf<Event>()

//    val events: List<Event> = _events

    var localHeartRate by mutableStateOf<String?>(null)
    override fun onDataChanged(p0: DataEventBuffer) {
        Log.e("listening", "data is being transferred")
        for (event in p0) {
            if (event.type == DataEvent.TYPE_CHANGED) {
                val item = event.dataItem
                val uri = item.uri
                val path = uri.path
                val nodeId = item.uri.authority
                val dataItem = DataMapItem.fromDataItem(item)
                val dataMap = (dataItem as DataMapItem).dataMap
                if (dataMap.getString("heart-rate") != null) {
                    localHeartRate = dataMap.getString("heart-rate")
                    Log.d("success", dataMap.getString("heart-rate")!!)
                }
                else {
                    Log.e("error", "heart rate cannot be found")
                }
            }
        }
    }
}

data class Event(@StringRes val title: Int, val text: String)