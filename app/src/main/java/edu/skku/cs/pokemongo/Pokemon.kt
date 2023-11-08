package edu.skku.cs.pokemongo

import com.google.android.gms.maps.model.LatLng

data class Pokemon (val name: String, val url: String) {
    val number get() =
        url.split("/").dropLast(1).last()

    val imageUrl get() =
        "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/$number.png"

    lateinit var position: LatLng
}