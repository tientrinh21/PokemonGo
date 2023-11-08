package edu.skku.cs.pokemongo

import android.annotation.SuppressLint
import android.content.res.Resources
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.DimenRes
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlin.random.Random


class CatchActivity : AppCompatActivity() {
    private var catchTries: Int = 0

    @SuppressLint("ResourceAsColor", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catch)

        val pokemonName = intent.getStringExtra(MainActivity.EXT_POKEMON_NAME)
        val pokemonNumber = intent.getStringExtra(MainActivity.EXT_POKEMON_NUMBER)

        catchTries = 3

        val host = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/versions/generation-v/black-white/animated/"
        val imageUrl = "$host$pokemonNumber.gif"
        val imageViewPokemon = findViewById<ImageView>(R.id.imageViewPokemon)

        val textViewPokemonName = findViewById<TextView>(R.id.textViewPokemonName)
        textViewPokemonName.text = pokemonName!!.uppercase()

        Glide.with(this)
            .asGif()
            .load(imageUrl)
            .error(R.mipmap.ic_launcher)  // any image in case of error
            .override(250.toPx(), 250.toPx()) // resizing
            .fitCenter()
            .into(imageViewPokemon)

        val textViewInstruction = findViewById<TextView>(R.id.textViewInstruction)
        textViewInstruction.text = "Press CATCH to try catching the Pokemon"

        val btnCatch = findViewById<Button>(R.id.buttonCatch)
        btnCatch.leftDrawable(R.drawable.poke_ball, R.dimen.btn_icon)
        btnCatch.setOnClickListener {
            val isCatch = Random.nextDouble() > 0.7 // 30% chance of catching the Pokemon

            if (isCatch) {
                textViewInstruction.text = "Gotcha! ${pokemonName.uppercase()} was caught!"
                btnCatch.isEnabled = false
                btnCatch.setBackgroundColor(R.color.crimson)
                MainActivity.caughtPokemonMap[pokemonNumber!!.toInt()] = pokemonName
            } else {
                textViewInstruction.text = "Oh no, ${pokemonName.uppercase()} broke free!"
                catchTries--
                if (catchTries == 0) {
                    btnCatch.isEnabled = false
                    btnCatch.setBackgroundColor(R.color.crimson)
                    textViewInstruction.text = "You have no chances to catch left"
                }
            }
        }
    }

    // dp to px
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun Button.leftDrawable(@DrawableRes id: Int = 0, @DimenRes sizeRes: Int) {
        val drawable = ContextCompat.getDrawable(context, id)
        val size = resources.getDimensionPixelSize(sizeRes)
        drawable?.setBounds(0, 0, size, size)
        this.setCompoundDrawables(drawable, null, null, null)
    }
}