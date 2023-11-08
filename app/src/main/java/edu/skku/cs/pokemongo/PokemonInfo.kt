package edu.skku.cs.pokemongo

data class PokemonInfo (
    val id: Long,
    val name: String,
    val height: Long,
    val weight: Long,
    val types: List<TypeResponse>,
) {
    val idString get() = when(id.toString().length) {
        1 -> "#00$id"
        2 -> "#0$id"
        else -> "#$id"
    }

    data class TypeResponse(
        val slot: Int,
        val type: Type
    )

    data class Type(
        val name: String
    )
}
