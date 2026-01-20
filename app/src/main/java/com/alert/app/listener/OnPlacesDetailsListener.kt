package com.alert.app.listener

import com.alert.app.model.addressmodel.PlaceDetails


interface OnPlacesDetailsListener {
  /**
   * Triggered when the places details are fetched and returns the details of the pace
   */
  fun onPlaceDetailsFetched(placeDetails: PlaceDetails)

  /**
   * Triggered when there is an error and passes the error message along as a parameter
   */
  fun onError(errorMessage: String)
}