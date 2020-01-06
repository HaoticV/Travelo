package com.example.travelo.directionHelpers;

import com.google.android.gms.maps.model.PolylineOptions;

public interface TaskLoadedCallback {
    void onTaskDone(PolylineOptions polylineOptions);
}