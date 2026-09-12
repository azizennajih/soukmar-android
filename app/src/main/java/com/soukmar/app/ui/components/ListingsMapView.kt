package com.soukmar.app.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.soukmar.app.data.remote.dto.ListingDto
import com.soukmar.app.ui.model.formatPriceParts
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

private val MOROCCO_CENTER = GeoPoint(31.7917, -7.0926)
private const val DEFAULT_ZOOM = 6.0
private const val SINGLE_PIN_ZOOM = 14.0

/** Mirrors the web app's `app-listings-map` (Leaflet + OpenStreetMap tiles):
 * plots every listing that has coordinates, centers/zooms to fit them (or
 * falls back to a Morocco-wide view when none have coordinates yet), and
 * navigates to the listing on marker tap. Used both as the single-pin map
 * on a listing's detail page and the multi-marker map for search results. */
@Composable
fun ListingsMapView(
    listings: List<ListingDto>,
    modifier: Modifier = Modifier,
    /** Fixed height for a compact embedded map (e.g. a listing's own detail
     * page); leave null to have the map fill whatever size [modifier]
     * already gives it (e.g. `Modifier.fillMaxSize()` for a full-screen map). */
    heightDp: Int? = null,
    onMarkerClick: (String) -> Unit
) {
    val withCoords = remember(listings) { listings.filter { it.lat != null && it.lng != null } }
    val sizedModifier = if (heightDp != null) modifier.fillMaxWidth().height(heightDp.dp) else modifier.fillMaxSize()

    AndroidView(
        modifier = sizedModifier,
        factory = { context ->
            MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(DEFAULT_ZOOM)
                controller.setCenter(MOROCCO_CENTER)
            }
        },
        update = { mapView ->
            mapView.overlays.clear()

            val points = withCoords.map { GeoPoint(it.lat!!, it.lng!!) }
            when {
                points.isEmpty() -> {
                    mapView.controller.setZoom(DEFAULT_ZOOM)
                    mapView.controller.setCenter(MOROCCO_CENTER)
                }
                points.size == 1 -> {
                    mapView.controller.setZoom(SINGLE_PIN_ZOOM)
                    mapView.controller.setCenter(points.first())
                }
                else -> {
                    val box = BoundingBox.fromGeoPoints(points)
                    mapView.zoomToBoundingBox(box, false, 80)
                }
            }

            withCoords.forEach { listing ->
                val marker = Marker(mapView)
                marker.position = GeoPoint(listing.lat!!, listing.lng!!)
                marker.title = listing.title
                marker.snippet = listing.price?.let { "${formatPriceParts(it, listing.currency).first} ${listing.currency}" }
                marker.setOnMarkerClickListener { _, _ ->
                    onMarkerClick(listing.id)
                    true
                }
                mapView.overlays.add(marker)
            }
            mapView.invalidate()
        },
        onRelease = { mapView -> mapView.onDetach() }
    )
}
