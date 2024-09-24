package com.malakezzat.weatherforecast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast

class ConnectionBroadcastReceiver : BroadcastReceiver() {

    var hasConnection : Boolean = false
    lateinit var receiverInterface : ReceiverInterface
    override fun onReceive(context: Context, intent: Intent?) {
        receiverInterface = context as ReceiverInterface
        if (isConnected(context)) {
            receiverInterface.loadFromNetwork()
            Toast.makeText(context, "has internet", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "has no internet", Toast.LENGTH_SHORT).show()
            receiverInterface.loadFromDataBase()
        }
    }

    fun isConnected(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            else -> false
        }
    }

    fun hasConnection() : Boolean{
        return hasConnection
    }
}