package ro.pub.cs.systems.eim.practicaltest02v8

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class PracticalTest02v8MainActivity : AppCompatActivity() {

    private lateinit var currencyEditText: EditText
    private lateinit var requestButton: Button
    private lateinit var resultTextView: TextView
    private lateinit var navigateButton: Button

    private val client = OkHttpClient()
    private val cache = mutableMapOf<String, Pair<String, Long>>()
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm:ss 'UTC'", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_practical_test02v8_main)

        currencyEditText = findViewById(R.id.currencyEditText)
        requestButton = findViewById(R.id.requestButton)
        resultTextView = findViewById(R.id.resultTextView)
        navigateButton = findViewById(R.id.navigateButton)

        requestButton.setOnClickListener {
            val currency = currencyEditText.text.toString().uppercase()
            if (currency == "USD" || currency == "EUR") {
                fetchBitcoinRate(currency)
            } else {
                resultTextView.text = "Invalid currency. Please enter USD or EUR."
            }
        }

        navigateButton.setOnClickListener {
            // Implement navigation to Activity 2
        }
    }

    private fun fetchBitcoinRate(currency: String) {
        val currentTime = System.currentTimeMillis()
        val cachedData = cache[currency]

        if (cachedData != null && currentTime - cachedData.second < 60000) {
            resultTextView.text = "Rate: ${cachedData.first}\nCached"
            return
        }

        val request = Request.Builder()
            .url("https://api.coindesk.com/v1/bpi/currentprice/$currency.json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    resultTextView.text = "Failed to fetch data: ${e.message}"
                }
                Log.e("PracticalTest02v8", "Error fetching data", e)
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) {
                    runOnUiThread {
                        resultTextView.text = "Unexpected response: ${response.message}"
                    }
                    Log.e("PracticalTest02v8", "Unexpected response: ${response.message}")
                    return
                }

                response.body?.let {
                    try {
                        val jsonString = it.string()
                        Log.d("PracticalTest02v8", "Response JSON: $jsonString")
                        val json = JSONObject(jsonString)
                        val rate = json.getJSONObject("bpi").getJSONObject(currency).getString("rate")
                        val updatedTime = json.getJSONObject("time").getString("updated")
                        val updatedTimeMillis = dateFormat.parse(updatedTime).time

                        // Cache the result
                        cache[currency] = Pair(rate, updatedTimeMillis)

                        runOnUiThread {
                            resultTextView.text = "Rate: $rate\nUpdated: $updatedTime"
                        }
                        Log.d("PracticalTest02v8", "Parsed rate: $rate, updated: $updatedTime")
                    } catch (e: Exception) {
                        runOnUiThread {
                            resultTextView.text = "Error parsing response: ${e.message}"
                        }
                        Log.e("PracticalTest02v8", "Error parsing response", e)
                    }
                } ?: run {
                    runOnUiThread {
                        resultTextView.text = "Empty response body"
                    }
                    Log.e("PracticalTest02v8", "Empty response body")
                }
            }
        })
    }
}