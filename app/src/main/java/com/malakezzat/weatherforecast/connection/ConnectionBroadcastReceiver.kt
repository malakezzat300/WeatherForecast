package com.malakezzat.weatherforecast.connection

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.widget.Toast
import com.malakezzat.weatherforecast.R

class ConnectionBroadcastReceiver(private val fragment: ReceiverInterface) : BroadcastReceiver() {

    var hasConnection : Boolean = false
    override fun onReceive(context: Context, intent: Intent?) {
        if (isConnected(context)) {
            fragment.loadFromNetwork()
        } else {
            Toast.makeText(context, context.getString(R.string.no_internet), Toast.LENGTH_SHORT).show()
            fragment.loadFromDataBase()
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