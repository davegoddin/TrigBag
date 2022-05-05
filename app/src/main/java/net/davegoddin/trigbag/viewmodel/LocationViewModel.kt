package net.davegoddin.trigbag.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class LocationViewModel : ViewModel() {
    private val mutableCurrentLocation = MutableLiveData<Location>()
    val currentLocation: LiveData<Location> get() = mutableCurrentLocation

    fun updateLocation(location: Location)
    {

        // initial set
        if (mutableCurrentLocation.value == null) {
            mutableCurrentLocation.value = location
            return
        }


        // check current lat/lng against attempted update to avoid unnecessary updates
        val currentLat = mutableCurrentLocation.value!!.latitude
        val currentLong = mutableCurrentLocation.value!!.longitude

        if (location.latitude != currentLat || location.longitude != currentLong)
        {
            mutableCurrentLocation.value = location
        }

    }

}