/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.maps;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringDef;

import org.odk.collect.maps.markers.MarkerDescription;
import org.odk.collect.maps.markers.MarkerIconDescription;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Interface for a Fragment that renders a map view.  The plan is to have one
 * implementation for each map SDK, e.g. GoogleMapFragment, OsmDroidMapFragment, etc.
 *
 * This is intended to be a single map API that provides all functionality needed
 * for the three geo widgets (collecting or editing a point, a trace, or a shape):
 *   - Basic control of the viewport (panning, zooming)
 *   - Displaying and getting the current GPS location
 *   - Requesting a callback on the first GPS location fix
 *   - Requesting callbacks for short clicks and long presses on the map
 *   - Adding editable points to the map
 *   - Adding editable traces (polylines) to the map
 *   - Adding editable shapes (closed polygons) to the map
 *
 * Editable points, traces, and shapes are called "map features" in this API.
 * To keep the API small, features are not exposed as objects; instead, they are
 * identified by integer feature IDs.  To keep the API unified (instead of having
 * three distinct modes), the map always supports all three kinds of features,
 * even though the geo widgets only use one kind of feature at a time.
 */
public interface MapFragment {
    MapPoint INITIAL_CENTER = new MapPoint(0, -30);
    float INITIAL_ZOOM = 2;
    float POINT_ZOOM = 16;

    String KEY_REFERENCE_LAYER = "REFERENCE_LAYER";

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({BOTTOM, CENTER})
    @interface IconAnchor {}

    String CENTER = "center";
    String BOTTOM = "bottom";

    void init(@Nullable ReadyListener readyListener, @Nullable ErrorListener errorListener);

    /** Gets the point currently shown at the center of the map view. */
    @NonNull MapPoint getCenter();

    /**
     * Gets the current zoom level.  For maps that only support zooming by
     * powers of 2, the zoom level will always be an integer.
     */
    double getZoom();

    /**
     * Centers the map view on the given point, leaving zoom level unchanged,
     * possibly with animation.
     */
    void setCenter(@Nullable MapPoint center, boolean animate);

    /**
     * Centers the map view on the given point, zooming in to a close-up level
     * deemed appropriate by the implementation, possibly with animation.
     */
    void zoomToPoint(@Nullable MapPoint center, boolean animate);

    /**
     * Centers the map view on the given point with a zoom level as close as
     * possible to the given zoom level, possibly with animation.
     */
    void zoomToPoint(@Nullable MapPoint center, double zoom, boolean animate);

    /**
     * Adjusts the map's viewport to enclose all of the given points, possibly
     * with animation.  A scaleFactor of 1.0 ensures that all the points will be
     * just visible in the viewport; a scaleFactor less than 1 shrinks the view
     * beyond that.  For example, a scaleFactor of 0.8 causes the bounding box
     * to occupy at most 80% of the width and 80% of the height of the viewport,
     * ensuring a margin of at least 10% on all sides.
     */
    void zoomToBoundingBox(Iterable<MapPoint> points, double scaleFactor, boolean animate);

    /**
     * Adds a marker to the map at the given location. If draggable is true,
     * the user will be able to drag the marker to change its location.
     * Returns a positive integer, the featureId for the newly added shape.
     */
    int addMarker(MarkerDescription markerDescription);

    List<Integer> addMarkers(List<MarkerDescription> markers);

    /** Sets the icon for a marker. */
    void setMarkerIcon(int featureId, MarkerIconDescription markerIconDescription);

    /** Gets the location of an existing marker. */
    MapPoint getMarkerPoint(int featureId);

    /**
     * Adds a polyline to the map with the given sequence of vertices.
     * The vertices will have handles that can be dragged by the user.
     * Returns a positive integer, the featureId for the newly added shape.
     */
    int addPolyLine(LineDescription lineDescription);

    /**
     * Adds a polygon to the map with given sequence of vertices. * Returns a positive integer,
     * the featureId for the newly added shape.
     */
    int addPolygon(PolygonDescription polygonDescription);

    /** Appends a vertex to the polyline or polygon specified by featureId. */
    void appendPointToPolyLine(int featureId, @NonNull MapPoint point);

    /**
     * Removes the last vertex of the polyline or polygon specified by featureId.
     * If there are no vertices, does nothing.
     */
    void removePolyLineLastPoint(int featureId);

    /**
     * Returns the vertices of the polyline or polygon specified by featureId, or an
     * empty list if the featureId does not identify an existing polyline or polygon.
     */
    @NonNull List<MapPoint> getPolyLinePoints(int featureId);

    /** Removes all map features from the map. */
    void clearFeatures();

    /** Sets or clears the callback for a click on the map. */
    void setClickListener(@Nullable PointListener listener);

    /** Sets or clears the callback for a long press on the map. */
    void setLongPressListener(@Nullable PointListener listener);

    /** Sets or clears the callback for a click on a feature. */
    void setFeatureClickListener(@Nullable FeatureListener listener);

    /** Sets or clears the callback for when a drag is completed. */
    void setDragEndListener(@Nullable FeatureListener listener);

    /**
     * Enables/disables GPS tracking.  While enabled, the GPS location is shown
     * on the map, the first GPS fix will trigger any pending callbacks set by
     * runOnGpsLocationReady(), and every GPS fix will invoke the callback set
     * by setGpsLocationListener().
     */
    void setGpsLocationEnabled(boolean enabled);

    /** Gets the last GPS location fix, or null if there hasn't been one. */
    @Nullable MapPoint getGpsLocation();

    /** Gets the provider of the last fix, or null if there hasn't been one. */
    @Nullable String getLocationProvider();

    /**
     * Queues a callback to be invoked on the UI thread as soon as a GPS fix is
     * available.  If there already is a location fix, the callback is invoked
     * immediately; otherwise, when a fix is obtained, it will be invoked once.
     * To begin searching for a GPS fix, call setGpsLocationEnabled(true).
     * Activities that set callbacks should call setGpsLocationEnabled(false)
     * in their onStop() or onDestroy() methods, to prevent invalid callbacks.
     */
    void runOnGpsLocationReady(@NonNull ReadyListener listener);

    /**
     * Sets or clears the callback for GPS location updates.  This callback
     * will only be invoked while GPS is enabled with setGpsLocationEnabled().
     */
    void setGpsLocationListener(@Nullable PointListener listener);

    void setRetainMockAccuracy(boolean retainMockAccuracy);

    /**
     * @return true if the {@link MapFragment} center has already been set (by {@link MapFragment#zoomToPoint(MapPoint, boolean)} for instance).
     */
    boolean hasCenter();

    interface ErrorListener {
        void onError();
    }

    interface ReadyListener {
        void onReady(@NonNull MapFragment mapFragment);
    }

    interface PointListener {
        void onPoint(@NonNull MapPoint point);
    }

    interface FeatureListener {
        void onFeature(int featureId);
    }

}
