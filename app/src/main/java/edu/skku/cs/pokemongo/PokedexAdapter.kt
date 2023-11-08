package edu.skku.cs.pokemongo

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import java.util.*

class PokedexAdapter(private val context: Context, private val pokemonMap: Map<Int, String>) : BaseAdapter() {
    private val pokemonNumberList: List<Int> = pokemonMap.keys.toList()

    override fun getCount(): Int {
        return pokemonMap.size
    }

    override fun getItem(p0: Int): Any {
        return pokemonMap[pokemonNumberList[p0]]!!
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    @SuppressLint("ViewHolder", "InflateParams")
    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val pokemonEntryView = inflater.inflate(R.layout.pokemon_entry, null)


        val host = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/"
        val imageUrl = "$host${pokemonNumberList[p0]}.png"
        val imageViewEntry = pokemonEntryView.findViewById<ImageView>(R.id.imageViewEntry)

        Glide.with(context)
            .load(imageUrl)
            .error(R.mipmap.ic_launcher)  // any image in case of error
            .override(100.toPx(), 100.toPx()) // resizing
            .fitCenter()
            .into(imageViewEntry)

        val textViewEntry = pokemonEntryView.findViewById<TextView>(R.id.textViewEntry)
        textViewEntry.text = pokemonMap[pokemonNumberList[p0]]!!.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(
                Locale.getDefault()
            ) else it.toString()
        }

        val constraintLayoutEntry = pokemonEntryView.findViewById<ConstraintLayout>(R.id.constraintLayoutEntry)
        constraintLayoutEntry.setOnClickListener {
            val intent = Intent(context, InfoActivity::class.java).apply {
                putExtra(PokedexActivity.EXT_POKEMON_NAME, pokemonMap[pokemonNumberList[p0]])
                putExtra(PokedexActivity.EXT_POKEMON_NUMBER, pokemonNumberList[p0])
                putExtra(PokedexActivity.EXT_POKEMON_URL, imageUrl)
            }
            context.startActivity(intent)
        }
        
        return pokemonEntryView
    }

    // dp to px
    private fun Int.toPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
}