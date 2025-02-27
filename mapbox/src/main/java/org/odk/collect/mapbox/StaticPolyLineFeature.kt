package org.odk.collect.mapbox

import com.mapbox.geojson.Point
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotation
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PolylineAnnotationOptions
import org.odk.collect.maps.LineDescription
import org.odk.collect.maps.MapFragment
import org.odk.collect.maps.MapPoint

/** A polyline that can not be manipulated by dragging Symbols at its vertices. */
internal class StaticPolyLineFeature(
    private val polylineAnnotationManager: PolylineAnnotationManager,
    private val featureId: Int,
    private val featureClickListener: MapFragment.FeatureListener?,
    private val lineDescription: LineDescription
) : LineFeature {
    override val points = mutableListOf<MapPoint>()
    private var polylineAnnotation: PolylineAnnotation? = null

    init {
        lineDescription.points.forEach {
            points.add(it)
        }

        val points = points
            .map {
                Point.fromLngLat(it.longitude, it.latitude, it.altitude)
            }
            .toMutableList()
            .also {
                if (lineDescription.closed && it.isNotEmpty()) {
                    it.add(it.first())
                }
            }

        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }

        if (points.size > 1) {
            polylineAnnotation = polylineAnnotationManager.create(
                PolylineAnnotationOptions()
                    .withPoints(points)
                    .withLineColor(lineDescription.getStrokeColor())
                    .withLineWidth(MapUtils.convertStrokeWidth(lineDescription))
            ).also {
                polylineAnnotationManager.update(it)
            }
        }

        polylineAnnotationManager.addClickListener { annotation ->
            polylineAnnotation?.let {
                if (annotation.id == it.id && featureClickListener != null) {
                    featureClickListener.onFeature(featureId)
                    true
                } else {
                    false
                }
            } ?: false
        }
    }

    override fun dispose() {
        polylineAnnotation?.let {
            polylineAnnotationManager.delete(it)
        }
        points.clear()
    }
}
