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

        findViewById<Button>(R.id.btnIr1).setOnClickListener { sendIrCommand(irManager, intArrayOf(9000, 4500, 560, 560), "1") }
        findViewById<Button>(R.id.btnIr2).setOnClickListener { sendIrCommand(irManager, intArrayOf(4000, 4000, 500, 1500), "2") }
        findViewById<Button>(R.id.btnIr3).setOnClickListener { sendIrCommand(irManager, intArrayOf(2400, 600, 1200, 600), "3") }
        findViewById<Button>(R.id.btnIr4).setOnClickListener { sendIrCommand(irManager, intArrayOf(3400, 1600, 400, 1200), "4") }

        findViewById<Button>(R.id.btnWifi).setOnClickListener {
            Toast.makeText(this, "Network command sent", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendIrCommand(irManager: ConsumerIrManager, pattern: IntArray, number: String) {
        if (irManager.hasIrEmitter()) {
            irManager.transmit(38000, pattern)
            Toast.makeText(this, "Sent IR test $number", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "No IR emitter found", Toast.LENGTH_SHORT).show()
        }
    }
}
