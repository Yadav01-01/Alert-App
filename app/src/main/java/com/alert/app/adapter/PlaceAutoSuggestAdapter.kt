package com.alert.app.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.gms.tasks.Tasks // <-- Important

class PlaceAutoSuggestAdapter(
    context: Context,
    private val placesClient: PlacesClient
) : ArrayAdapter<AutocompletePrediction>(context, android.R.layout.simple_list_item_1), Filterable {

    private var results: List<AutocompletePrediction> = emptyList()

    override fun getCount(): Int = results.size
    override fun getItem(position: Int): AutocompletePrediction? = results.getOrNull(position)
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)

        val item = getItem(position)
        view.findViewById<TextView>(android.R.id.text1).apply {
            text = item?.getFullText(null) ?: ""
        }
        return view
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filterResults = FilterResults()
            if (constraint.isNullOrBlank()) {
                filterResults.values = emptyList<AutocompletePrediction>()
                filterResults.count = 0
                return filterResults
            }

            val request = FindAutocompletePredictionsRequest.builder()
                .setQuery(constraint.toString())
                .build()

            try {
                val response = Tasks.await(placesClient.findAutocompletePredictions(request))
                filterResults.values = response.autocompletePredictions
                filterResults.count = response.autocompletePredictions.size
            } catch (e: Exception) {
                filterResults.values = emptyList<AutocompletePrediction>()
                filterResults.count = 0
            }

            return filterResults
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            if (results != null && results.count > 0) {
                this@PlaceAutoSuggestAdapter.results = results.values as List<AutocompletePrediction>
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }
    }
}