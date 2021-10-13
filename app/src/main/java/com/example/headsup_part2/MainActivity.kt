package com.example.headsup_part2

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.Surface
import android.widget.*
import androidx.core.view.isVisible
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {
    private lateinit var llTop: LinearLayout
    private lateinit var llMain: LinearLayout
    private lateinit var llCelebrity: LinearLayout
// textview for displaying to the players if they wine "Please Rotate Device"
    private lateinit var tvTime: TextView
// textview for displaying the players' name
    private lateinit var tvName: TextView
  //  these three names the players not allow to say it
    private lateinit var tvTaboo1: TextView
    private lateinit var tvTaboo2: TextView
    private lateinit var tvTaboo3: TextView
//Textview to display the time to the players
    private lateinit var tvMain: TextView
    // to start the game
    private lateinit var btStart: Button

    private var gameActive = false
    // array with the players name on it
    private lateinit var celebrities: ArrayList<JSONObject>
//
    private var celeb = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        llTop = findViewById(R.id.llTop)
        llMain = findViewById(R.id.llMain)
        llCelebrity = findViewById(R.id.llCelebrity)

        tvTime = findViewById(R.id.tvTime)

        tvName = findViewById(R.id.tvName)
        tvTaboo1 = findViewById(R.id.tvTaboo1)
        tvTaboo2 = findViewById(R.id.tvTaboo2)
        tvTaboo3 = findViewById(R.id.tvTaboo3)

        tvMain = findViewById(R.id.tvMain)
        btStart = findViewById(R.id.btStart)
        btStart.setOnClickListener { requestAPI() }

        celebrities = arrayListOf()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val rotation = windowManager.defaultDisplay.rotation
        if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
            if(gameActive){
                celeb++
                newCelebrity(celeb)
                updateStatus(false)
            }else{
                updateStatus(false)
            }
        }else{
            if(gameActive){
                updateStatus(true)
            }else{
                updateStatus(false)
            }
        }
    }
// if there is time left the player will Rotate the device
    private fun newTimer(){
        if(!gameActive){
            gameActive = true
            tvMain.text = "Please Rotate Device"
            btStart.isVisible = false
            val rotation = windowManager.defaultDisplay.rotation
            if(rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180){
                updateStatus(false)
            }else{
                updateStatus(true)
            }

            object : CountDownTimer(60000, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    tvTime.text = "Time: ${millisUntilFinished / 1000}"
                }
// if the game is over we will call onFinish() and we will not update the state
                override fun onFinish() {
                    gameActive = false
                    tvTime.text = "Time: --"
                    tvMain.text = "Heads Up!"
                    btStart.isVisible = true
                    updateStatus(false)
                }
            }
                    // her the time will start from 0 and the game will start
                .start()
        }
    }

    private fun newCelebrity(id: Int){
        if(id < celebrities.size){
            tvName.text = celebrities[id].getString("name")
            tvTaboo1.text = celebrities[id].getString("taboo1")
            tvTaboo2.text = celebrities[id].getString("taboo2")
            tvTaboo3.text = celebrities[id].getString("taboo3")
        }
    }

    private fun requestAPI(){
        CoroutineScope(Dispatchers.IO).launch {
            val data = async {
                getCelebrities()
            }.await()
            if(data.isNotEmpty()){
                withContext(Main){
                    parseJSON(data)
                    celebrities.shuffle()
                    newCelebrity(0)
                    newTimer()
                }
            }else{

            }
        }
    }

    private suspend fun parseJSON(result: String){
        withContext(Dispatchers.Main){
            celebrities.clear()
            val jsonArray = JSONArray(result)
            for(i in 0 until jsonArray.length()){
                celebrities.add(jsonArray.getJSONObject(i))
            }
        }
    }

    private fun getCelebrities(): String{
        var response = ""
        try {
            response = URL("https://dojo-recipes.herokuapp.com/celebrities/")
                .readText(Charsets.UTF_8)
        }catch (e: Exception){
            println("Error: $e")
        }
        return response
    }
// this function to update the status
    private fun updateStatus(showCelebrity: Boolean){
        if(showCelebrity){
            llCelebrity.isVisible = true
            llMain.isVisible = false
        }else{
            llCelebrity.isVisible = false
            llMain.isVisible = true
        }
    }
}