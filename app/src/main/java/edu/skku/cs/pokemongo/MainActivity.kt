package edu.skku.cs.pokemongo

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.transition.Transition
import com.bumptech.glide.request.target.CustomTarget
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.*
import java.io.IOException
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var mMap: GoogleMap
    private lateinit var userLocationMarker: Marker
    private lateinit var locationManager: LocationManager
    private lateinit var location: Location
    private lateinit var pokemonList: List<Pokemon>

    private val showPokemonCount = 5

    private val host = "https://pokeapi.co/api/v2/"
    private val path = "pokemon?limit=100&offset=0"
    private val client = OkHttpClient()

    companion object {
        const val EXT_POKEMON_NAME = "extra_key_for_pokemon_name"
        const val EXT_POKEMON_NUMBER = "extra_key_for_pokemon_number"

        var caughtPokemonMap = mutableMapOf<Int, String>()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Default Data
        caughtPokemonMap[50] = "diglett"
        caughtPokemonMap[78] = "rapidash"
        caughtPokemonMap[217] = "ursaring"
        caughtPokemonMap[432] = "purugly"


        val mapFragment: SupportMapFragment = supportFragmentManager.findFragmentById( R.id.mapview) as SupportMapFragment
        mapFragment.getMapAsync(this)

        getUserLocation()

        val btnPokedex: View = findViewById(R.id.buttonPokedex)
        btnPokedex.setOnClickListener {
            val intent = Intent(applicationContext, PokedexActivity::class.java).apply { }
            startActivity(intent)
        }
    }

    private fun resizeMapIcon(imageBitmap: Bitmap): Bitmap {
        val width =  imageBitmap.width / 3
        val height = imageBitmap.height / 3
        return Bitmap.createScaledBitmap(imageBitmap, width, height, false)
    }

    private fun getUserLocation() {
        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                        getUserLocation()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        getUserLocation()
                    } else -> {
                        // No location access granted.
                        Toast.makeText(this,"User Not Granted Permissions",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        Toast.makeText(applicationContext, "Welcome to Pokemon Go!", Toast.LENGTH_SHORT).show()

        val locationListener = MyLocationListener()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3, 1f, locationListener)
    }

    @SuppressLint("PotentialBehaviorOverride")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(
                applicationContext,
                R.raw.style_json
            )
        )

        // Check location permission
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val locationPermissionRequest = registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                when {
                    permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                        // Precise location access granted.
                        getUserLocation()
                    }
                    permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                        // Only approximate location access granted.
                        getUserLocation()
                    } else -> {
                        // No location access granted.
                        Toast.makeText(this,"User Not Granted Permissions",Toast.LENGTH_SHORT).show()
                    }
                }
            }
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION))
            return
        }

        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!

        // Current / Last known location of user
        val startPosition = LatLng(location.latitude, location.longitude)

        val characterImageBitmap = BitmapFactory.decodeResource( resources, R.drawable.character )

        userLocationMarker = mMap.addMarker(
            MarkerOptions()
                .position(startPosition)
                .title("Me")
                .icon(
                    BitmapDescriptorFactory.fromBitmap(
                        resizeMapIcon( characterImageBitmap )
                    )
                )
        )!!
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPosition, 18f))

        loadPokemon(showPokemonCount - 1) // onResume will load 1 more

        mMap.setOnMarkerClickListener { marker: Marker ->
            if (marker != userLocationMarker) {
                if (abs( marker.position.latitude - userLocationMarker.position.latitude) < 0.0005
                    && abs( marker.position.longitude - userLocationMarker.position.longitude) < 0.0005) {
                    val intent = Intent(applicationContext, CatchActivity::class.java).apply {
                        putExtra(EXT_POKEMON_NAME, marker.title.toString())
                        putExtra(EXT_POKEMON_NUMBER, marker.snippet.toString())
                    }

                    marker.remove()
                    startActivity(intent)
                }
            }
            false
        }
    }

    override fun onResume() {
        super.onResume()

        try {
           loadPokemon()
        } catch (_: Exception) {}
    }

    inner class MyLocationListener : LocationListener {
        override fun onLocationChanged(location: Location) {
            try {
                // Update user location
                val currentPosition = LatLng(location.latitude, location.longitude)
                userLocationMarker.position = currentPosition
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, 18f))
            } catch (_: Exception) {}
        }
    }

    private fun loadPokemon(number: Int = 1) {
        val req = Request.Builder().url(host + path).build()

        client.newCall(req).enqueue(object: Callback{
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")
                    val str = response.body!!.string()
                    val data = Gson().fromJson(str, PokemonListResponse::class.java)

                    pokemonList = data.results
                    val showPokemonList = pokemonList.shuffled().subList(0, number)

                    CoroutineScope(Dispatchers.Main).launch {
                        for (pokemon in showPokemonList) {
                            pokemon.position = randomLatLng(location.latitude, location.longitude)

                            val pokemonMarker = mMap.addMarker(
                                MarkerOptions()
                                    .position(pokemon.position)
                                    .title(pokemon.name)
                                    .snippet(pokemon.number)
                            )!!
                            loadMarkerIcon(pokemonMarker, pokemon.imageUrl)
                        }
                    }
                }
            }
        })
    }

    // Generate random latitude and longitude around given location
    private fun randomLatLng(latitude: Double, longitude: Double): LatLng {
        val latRange = 0.002
        val lngRange = 0.001
//        val latRange = 0.0008
//        val lngRange = 0.0008
        return LatLng(latitude + Random.nextDouble(-latRange, latRange), longitude + Random.nextDouble(-lngRange, lngRange))
    }

    private fun loadMarkerIcon(marker: Marker, imageUrl: String) {
        Glide.with(this)
            .asBitmap().load(imageUrl).fitCenter().into(object: CustomTarget<Bitmap>() {
                override fun onResourceReady( resource: Bitmap, transition: Transition<in Bitmap>? ) {
                    marker.setIcon(BitmapDescriptorFactory.fromBitmap(
                        resizeMapIcon( resource )
                    ))
                }

                override fun onLoadCleared(placeholder: Drawable?) { }
            })
    }
}