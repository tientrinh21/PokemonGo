package edu.skku.cs.pokemongo

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.GridView

class PokedexActivity : AppCompatActivity() {
    companion object {
        const val EXT_POKEMON_NAME = "extra_key_for_pokemon_name"
        const val EXT_POKEMON_NUMBER = "extra_key_for_pokemon_number"
        const val EXT_POKEMON_URL = "extra_key_for_pokemon_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pokedex)

        val pokedexAdapter = PokedexAdapter(this, MainActivity.caughtPokemonMap)
        val gridViewPokedex = findViewById<GridView>(R.id.gridViewPokedex)
        gridViewPokedex.numColumns = 2
        gridViewPokedex.adapter = pokedexAdapter
    }
}