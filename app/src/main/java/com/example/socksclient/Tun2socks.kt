package com.example.socksclient

object Tun2socks {
    init {
        System.loadLibrary("sockstether")
    }

    external fun startTun2socks(tunDev: String, socksServer: String, udpgwServer: String): Int
    external fun stop()
}
