package edu.skku.cs.pokemongo

import android.annotation.SuppressLint
import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException

class InfoActivity : AppCompatActivity() {
    private val client = OkHttpClient()
    private val color = mutableMapOf<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_info)

        val pokemonName = intent.getStringExtra(PokedexActivity.EXT_POKEMON_NAME)
        val pokemonNumber = intent.getIntExtra(PokedexActivity.EXT_POKEMON_NUMBER, 1)
        val imageUrl = intent.getStringExtra(PokedexActivity.EXT_POKEMON_URL)

        color["bug"] = 0xFF179A55.toInt()
        color["dark"] = 0xFF040706.toInt()
        color["dragon"] = 0xFF378A94.toInt()
        color["electric"] = 0xFFE0E64B.toInt()
        color["fairy"] = 0xFF9E1A44.toInt()
        color["fire"] = 0xFFB22328.toInt()
        color["flying"] = 0xFF90B1C5.toInt()
        color["ghost"] = 0xFF363069.toInt()
        color["ice"] = 0xFF7ECFF2.toInt()
        color["poison"] = 0xFF642785.toInt()
        color["psychic"] = 0xFFAC296B.toInt()
        color["rock"] = 0xFF4B190E.toInt()
        color["steel"] = 0xFF5C756D.toInt()
        color["water"] = 0xFF2648DC.toInt()
        color["fighting"] = 0xFF9F422A.toInt()
        color["grass"] = 0xFF007C42.toInt()
        color["ground"] = 0xFFAD7235.toInt()
        color["normal"] = 0xFF5C756D.toInt()

        val textName = findViewById<TextView>(R.id.textName)
        textName.text = pokemonName!!.uppercase()

        val imagePokemon = findViewById<ImageView>(R.id.imagePokemon)
        Glide.with(applicationContext)
            .load(imageUrl)
            .error(R.mipmap.ic_launcher)  // any image in case of error
            .override(250.toPx(), 250.toPx()) // resizing
            .fitCenter()
            .into(imagePokemon)

        val host = "https://pokeapi.co/api/v2/pokemon/"
        val req = Request.Builder().url("$host$pokemonNumber").build()

        client.newCall(req).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            @SuppressLint("SetTextI18n")
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val str = response.body!!.string()
                    val data = Gson().fromJson(str, PokemonInfo::class.java)


                    CoroutineScope(Dispatchers.Main).launch {
                        val textId = findViewById<TextView>(R.id.textId)
                        textId.text = data.idString

                        val typeBackground = findViewById<ConstraintLayout>(R.id.typeBackground)
                        typeBackground.setBackgroundColor(color[data.types[0].type.name]!!)

                        val textHeight = findViewById<TextView>(R.id.textHeight)
                        val height = data.height.toDouble() / 10
                        textHeight.text = "Height:\t\t\t${height}m"

                        val textWeight = findViewById<TextView>(R.id.textWeight)
                        val weight = data.weight.toDouble() / 10
                        textWeight.text = "Height:\t\t\t${weight}kg"
                    }
                }
            }
        })
    }

    // dp to px
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}