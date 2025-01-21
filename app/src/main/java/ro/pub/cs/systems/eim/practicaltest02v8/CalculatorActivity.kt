package ro.pub.cs.systems.eim.practicaltest02v8

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import java.io.IOException

class CalculatorActivity : AppCompatActivity() {

    private lateinit var operationEditText: EditText
    private lateinit var t1EditText: EditText
    private lateinit var t2EditText: EditText
    private lateinit var calculateButton: Button
    private lateinit var resultTextView: TextView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        operationEditText = findViewById(R.id.operationEditText)
        t1EditText = findViewById(R.id.t1EditText)
        t2EditText = findViewById(R.id.t2EditText)
        calculateButton = findViewById(R.id.calculateButton)
        resultTextView = findViewById(R.id.resultTextView)

        calculateButton.setOnClickListener {
            val operation = operationEditText.text.toString()
            val t1 = t1EditText.text.toString()
            val t2 = t2EditText.text.toString()
            fetchCalculationResult(operation, t1, t2)
        }
    }

    private fun fetchCalculationResult(operation: String, t1: String, t2: String) {
        val url = "http://10.41.38.246:8080/expr/expr_get.py?operation=$operation&t1=$t1&t2=$t2"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    resultTextView.text = "Failed to fetch data: ${e.message}"
                }
                Log.e("CalculatorActivity", "Error fetching data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        resultTextView.text = "Unexpected response: ${response.message}"
                    }
                    Log.e("CalculatorActivity", "Unexpected response: ${response.message}")
                    return
                }

                response.body?.let {
                    val result = it.string()
                    runOnUiThread {
                        resultTextView.text = "Result: $result"
                    }
                } ?: run {
                    runOnUiThread {
                        resultTextView.text = "Empty response body"
                    }
                    Log.e("CalculatorActivity", "Empty response body")
                }
            }
        })
    }}