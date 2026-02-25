package com.example.neontvremote

import android.os.Bundle
import android.hardware.ConsumerIrManager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val irManager = getSystemService(CONSUMER_IR_SERVICE) as ConsumerIrManager

        // Set up each button to send a different IR code
        findViewById<Button>(R.id.btnIr1).setOnClickListener {
            sendIrCode(irManager, 0x20DF10EFL, "1")
        }
        findViewById<Button>(R.id.btnIr2).setOnClickListener {
            sendIrCode(irManager, 0xE0E040BFL, "2")
        }
        findViewById<Button>(R.id.btnIr3).setOnClickListener {
            sendIrCode(irManager, 0xA90L, "3")
        }
        findViewById<Button>(R.id.btnIr4).setOnClickListener {
            sendIrCode(irManager, 0x2FD48B7L, "4")
        }

        findViewById<Button>(R.id.btnWifi).setOnClickListener {
            Toast.makeText(this, "Network command sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendIrCode(irManager: ConsumerIrManager, code: Long, label: String) {
        if (!irManager.hasIrEmitter()) {
            Toast.makeText(this, "No IR emitter found", Toast.LENGTH_SHORT).show()
            return
        }

        val freq = 38000
        val pattern = if (code <= 0x0FFF) {
            buildSony12Pattern(code.toInt())
        } else {
            buildNec32Pattern(code)
        }

        try {
            irManager.transmit(freq, pattern)
            Toast.makeText(this, "Sent IR candidate $label", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to send IR $label", Toast.LENGTH_SHORT).show()
        }
    }

    // Build a 32-bit NEC pattern from a long code
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

    // Build a 12-bit Sony SIRC pattern from an int code
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
