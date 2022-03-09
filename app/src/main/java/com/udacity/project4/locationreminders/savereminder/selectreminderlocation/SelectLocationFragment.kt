package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil

import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    private val DEFAULT_ZOOM = 16f

    private lateinit var map: GoogleMap

    private var selectedPoi: PointOfInterest? = null
    private var selectedLat: Double? = null
    private var selectedLong: Double? = null

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true && permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
            setupMapAndLocation(map)
        } else {
            Snackbar.make(binding.root, R.string.permission_denied_explanation, Snackbar.LENGTH_LONG)
                .setAction(R.string.settings) {
                    // Displays App settings screen.
                    startActivity(Intent().apply {
                        action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                        data = Uri.fromParts("package", requireContext().packageName, null)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    })
                }.show()
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        supportMapFragment.getMapAsync(this)

        binding.btnSelectLocation.setOnClickListener {
            onLocationSelected()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        _viewModel.setSelectedLocation(selectedPoi, selectedLat, selectedLong)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // set any existing selected location
        if (_viewModel.hasSelectedLocation()) {
            setMapMarker(map, _viewModel.selectedPOI.value, _viewModel.latitude.value!!, _viewModel.longitude.value!!)
        }

        // custom style
        map.setMapStyle(
            MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style)
        )

        // on location set
        map.setOnMapLongClickListener {
            setMapMarker(map, null, it.latitude, it.longitude)
        }

        // on poi set
        map.setOnPoiClickListener{
            setMapMarker(map, it, it.latLng.latitude, it.latLng.longitude)
        }

        _viewModel.onMapLoaded()

        setupMapAndLocation(map)
    }

    private fun setupMapAndLocation(map: GoogleMap) {
        // check for permission
        if (
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.getFusedLocationProviderClient(requireActivity()).lastLocation.addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    val myLocation = task.result

                    if (myLocation != null) {
                        map.moveCamera(
                            CameraUpdateFactory
                                .newLatLngZoom(LatLng(myLocation.latitude, myLocation.longitude), DEFAULT_ZOOM)
                        )

                    }
                }
            }

            map.isMyLocationEnabled = true
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun setMapMarker(map: GoogleMap, poi: PointOfInterest?, latitude: Double, longitude: Double) {
        map.clear()

        selectedPoi = poi
        selectedLat = latitude
        selectedLong = longitude

        val title = poi?.name?: getString(R.string.dropped_pin)

        val markerOptions = MarkerOptions()
            .position(LatLng(latitude, longitude))
            .title(title)

        if (poi == null) {
            markerOptions.snippet(
                String.format("Lat: %1$.5f, Long: %2$.5f", latitude, longitude)
            )
        }

        val marker = map.addMarker(markerOptions)

        if (poi != null) {
            marker?.showInfoWindow()
        }

        _viewModel.saveBtnVisible.value = true
    }

}
