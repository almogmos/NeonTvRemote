package com.example.neontvremote

import android.content.Context
import android.hardware.ConsumerIrManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.txtStatus)

        val btnIr = findViewById<Button>(R.id.btnIrPower)
        val btnWifi = findViewById<Button>(R.id.btnWifiPower)

        btnIr.setOnClickListener { sendIrPowerTest() }
        btnWifi.setOnClickListener {
            status.text = "WiFi not configured yet. Need TV OS and model details."
        }
    }

    private fun sendIrPowerTest() {
        val ir = getSystemService(Context.CONSUMER_IR_SERVICE) as? ConsumerIrManager
        if (ir == null || !ir.hasIrEmitter()) {
            status.text = "No IR emitter detected on this device."
            return
        }

        val freq = 38000

        val candidates = listOf(
            0x20DF10EFL, // common LG power
            0xE0E040BFL, // common Samsung power
            0xA90L       // common Sony power (short form)
        )

        for ((index, code) in candidates.withIndex()) {
            try {
                val pattern = if (code <= 0xFFFF) {
                    buildSony12Pattern(code.toInt())
                } else {
                    buildNec32Pattern(code)
                }
                ir.transmit(freq, pattern)
                status.text = "IR sent candidate ${index + 1}. If TV reacted, tell me which number worked."
                return
            } catch (e: Exception) {
                // try next
            }
        }

        status.text = "IR send failed for all candidates."
    }

    private fun buildNec32Pattern(code: Long): IntArray {
        val headerMark = 9000
        val headerSpace = 4500
        val bitMark = 560
        val zeroSpace = 560
        val oneSpace = 1690
        val endMark = 560

        val pulses = ArrayList<Int>(2 + 32 * 2 + 1)

        pulses.add(headerMark)
        pulses.add(headerSpace)

        for (i in 0 until 32) {
            val bit = (code shr i) and 1L
            pulses.add(bitMark)
            pulses.add(if (bit == 1L) oneSpace else zeroSpace)
        }

        pulses.add(endMark)

        return pulses.toIntArray()
    }

    private fun buildSony12Pattern(code: Int): IntArray {
        val headerMark = 2400
        val headerSpace = 600
        val oneMark = 1200
        val zeroMark = 600
        val bitSpace = 600

        val pulses = ArrayList<Int>(2 + 12 * 2)

        pulses.add(headerMark)
        pulses.add(headerSpace)

        for (i in 0 until 12) {
            val bit = (code shr i) and 1
            pulses.add(if (bit == 1) oneMark else zeroMark)
            pulses.add(bitSpace)
        }

        return pulses.toIntArray()
    }
}
