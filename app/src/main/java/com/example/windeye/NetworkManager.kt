package com.example.windeye

import java.io.*
import java.net.*
import kotlin.concurrent.thread
import kotlin.random.Random

class NetworkManager {
    
    private var serverSocket: ServerSocket? = null
    private var clientSocket: Socket? = null
    private var isServer = false
    private var isConnected = false
    
    private var onClientConnected: (() -> Unit)? = null
    private var onDataReceived: ((Map<String, Any>) -> Unit)? = null
    
    private var inputStream: ObjectInputStream? = null
    private var outputStream: ObjectOutputStream? = null
    
    fun setOnClientConnected(callback: () -> Unit) {
        onClientConnected = callback
    }
    
    fun setOnDataReceived(callback: (Map<String, Any>) -> Unit) {
        onDataReceived = callback
    }
    
    fun startServer(callback: (String?) -> Unit) {
        thread {
            try {
                serverSocket = ServerSocket(8888)
                val gameCode = generateGameCode()
                isServer = true
                
                callback(gameCode)
                
                val socket = serverSocket?.accept()
                clientSocket = socket
                
                if (socket != null) {
                    setupStreams(socket)
                    isConnected = true
                    onClientConnected?.invoke()
                    startListening()
                }
                
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    
    fun connectToServer(gameCode: String, callback: (Boolean) -> Unit) {
        thread {
            try {
                val localIP = getLocalIPAddress()
                val baseIP = localIP.substring(0, localIP.lastIndexOf('.') + 1)
                
                var connected = false
                
                for (i in 1..254) {
                    if (connected) break
                    
                    try {
                        val ip = "$baseIP$i"
                        if (ip == localIP) continue
                        
                        val socket = Socket()
                        socket.connect(InetSocketAddress(ip, 8888), 1000)
                        
                        setupStreams(socket)
                        clientSocket = socket
                        isConnected = true
                        connected = true
                        
                        startListening()
                        break
                        
                    } catch (e: Exception) {
                        // Continuer
                    }
                }
                
                callback(connected)
                
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    private fun setupStreams(socket: Socket) {
        outputStream = ObjectOutputStream(socket.getOutputStream())
        inputStream = ObjectInputStream(socket.getInputStream())
    }
    
    private fun startListening() {
        thread {
            try {
                while (isConnected) {
                    val data = inputStream?.readObject() as? Map<String, Any>
                    if (data != null) {
                        onDataReceived?.invoke(data)
                    }
                }
            } catch (e: Exception) {
                isConnected = false
            }
        }
    }
    
    private fun sendData(data: Map<String, Any>) {
        thread {
            try {
                outputStream?.writeObject(data)
                outputStream?.flush()
            } catch (e: Exception) {
                // Erreur d'envoi
            }
        }
    }
    
    fun sendPosition(progress: Float, speed: Float) {
        sendData(mapOf(
            "type" to "opponent_position",
            "progress" to progress,
            "speed" to speed
        ))
    }
    
    fun sendRaceFinished(winner: Int) {
        sendData(mapOf(
            "type" to "race_finished",
            "winner" to winner
        ))
    }
    
    fun sendNewRace() {
        sendData(mapOf("type" to "new_race"))
    }
    
    fun disconnect() {
        isConnected = false
        try {
            inputStream?.close()
            outputStream?.close()
            clientSocket?.close()
            serverSocket?.close()
        } catch (e: Exception) {
            // Ignorer
        }
    }
    
    private fun generateGameCode(): String {
        return (1..6).map { Random.nextInt(0, 10) }.joinToString("")
    }
    
    private fun getLocalIPAddress(): String {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val intf = interfaces.nextElement()
                val addresses = intf.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (!addr.isLoopbackAddress && addr is Inet4Address) {
                        return addr.hostAddress ?: ""
                    }
                }
            }
        } catch (e: Exception) {
            // Fallback
        }
        return "192.168.1.100"
    }
}
