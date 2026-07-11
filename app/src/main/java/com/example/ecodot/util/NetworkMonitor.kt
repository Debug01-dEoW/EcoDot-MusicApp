package com.example.ecodot.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

data class NetworkState(
    val isConnected: Boolean,
    val isPoorConnection: Boolean
)

class NetworkMonitor(private val context: Context) {
    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val networkState: Flow<NetworkState> = callbackFlow {
        fun updateState() {
            val activeNetwork = connectivityManager.activeNetwork
            if (activeNetwork == null) {
                trySend(NetworkState(isConnected = false, isPoorConnection = true))
                return
            }
            val caps = connectivityManager.getNetworkCapabilities(activeNetwork)
            val isConnected = caps?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            
            val isPoor = if (!isConnected) {
                true
            } else {
                val bandwidth = caps?.linkDownstreamBandwidthKbps ?: 0
                // Under 2000 Kbps (2 Mbps) is poor for video background streaming
                bandwidth in 1..2000
            }
            trySend(NetworkState(isConnected, isPoor))
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                updateState()
            }

            override fun onLost(network: Network) {
                updateState()
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateState()
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
        } catch (e: Exception) {
            // Catch potential security or system issues
        }
        
        updateState()

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(callback)
            } catch (e: Exception) {
                // Ignore unregistration errors
            }
        }
    }.distinctUntilChanged()
}
