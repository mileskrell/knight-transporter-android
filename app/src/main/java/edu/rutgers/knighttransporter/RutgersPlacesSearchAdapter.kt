package edu.rutgers.knighttransporter

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.mapbox.geojson.Feature
import com.mapbox.mapboxsdk.geometry.LatLng
import com.miguelcatalan.materialsearchview.R
import edu.rutgers.knighttransporter.feature_stuff.*
import java.util.*

/**
 * Based off of com.miguelcatalan.materialsearchview.SearchAdapter (by Miguel Catalan Bañuls)
 */
class RutgersPlacesSearchAdapter(
    context: Context,
    private val adapterPlaceItems: Array<AdapterPlaceItem>
) : BaseAdapter(), Filterable {
    private var suggestions = emptyList<AdapterPlaceItem>()
    private val inflater = LayoutInflater.from(context)

    data class AdapterPlaceItem(val icon: Drawable, val placeType: PlaceType, val feature: Feature) {
        val placeName = feature.getNameForPlaceType(placeType)
        val latLng = LatLng(
            feature.getNumberProperty(LATITUDE).toDouble(),
            feature.getNumberProperty(LONGITUDE).toDouble()
        )
    }

    companion object {
        const val MAX_SUGGESTIONS = 5
    }

    override fun getFilter(): Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            if (constraint.isNullOrBlank()) {
                return FilterResults()
            }

            // TODO: Split query on spaces and search for places containing all words.
            //  E.g. you might search "farm weather" when you're looking for
            //  "Hort Farm 1 - Weather Station Auxiliary Building".

            // Retrieve the autocomplete results.
            val searchResults = adapterPlaceItems.filter { nameLatLng ->
                nameLatLng.placeName.toLowerCase(Locale.getDefault())
                    .contains(constraint.toString().toLowerCase(Locale.getDefault()))
            }.toMutableList()

            // Prioritize results that begin with the search string
            var itemsMovedToFront = 0
            for (i in 0 until searchResults.size) {
                if (searchResults[i].placeName.toLowerCase(Locale.getDefault())
                        .startsWith(constraint.toString().toLowerCase(Locale.getDefault()))
                ) {
                    searchResults.add(0, searchResults.removeAt(i))
                    if (++itemsMovedToFront == MAX_SUGGESTIONS) {
                        // No point in searching for more
                        break
                    }
                }
            }

            // Assign the data to the FilterResults
            return FilterResults().apply {
                values = searchResults.take(MAX_SUGGESTIONS)
                count = searchResults.size
            }
        }

        override fun publishResults(constraint: CharSequence, results: FilterResults) {
            if (results.values != null) {
                @Suppress("UNCHECKED_CAST")
                suggestions = results.values as List<AdapterPlaceItem>
                notifyDataSetChanged()
            }
        }
    }

    override fun getCount() = suggestions.size

    override fun getItem(position: Int) = suggestions[position]

    override fun getItemId(position: Int) = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var actualConvertView = convertView // because Kotlin makes arguments vals
        val viewHolder: SuggestionsViewHolder

        if (convertView == null) {
            actualConvertView = inflater.inflate(R.layout.suggest_item, parent, false)
            viewHolder = SuggestionsViewHolder(actualConvertView)
            actualConvertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as SuggestionsViewHolder
        }

        viewHolder.run {
            textView.text = getItem(position).placeName
            textView.maxLines = 2
            textView.ellipsize = TextUtils.TruncateAt.END
            imageView.setImageDrawable(getItem(position).icon)
        }

        return actualConvertView!!
    }

    private inner class SuggestionsViewHolder(convertView: View) {
        val textView: TextView = convertView.findViewById(R.id.suggestion_text)
        val imageView: ImageView = convertView.findViewById(R.id.suggestion_icon)
    }
}
