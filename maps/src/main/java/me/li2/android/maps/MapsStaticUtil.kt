/*
 * Created by Weiyi Li on 13/03/20.
 * https://github.com/li2
 */
package me.li2.android.maps

/**
 * @param color black, brown, green, purple, yellow, blue, gray, orange, red, white or 24-bit color (example: 0xFFFFCC)
 * @param locations can be coordinate string "62.107733,-145.541936", or place name "Williamsburg,Brooklyn,NY"
 * @param label character from the set {A-Z, 0-9}
 * @param size [MarkerInfo.MarkerSize]
 * @param icon custom icon using a URL (which should be URL-encoded).
 * @param anchor specify a different anchor point, "10,5", or predefined top, bottom, left, right, center, topleft, topright, bottomleft, or bottomright
 */
data class MarkerInfo(val color: String,
                      val locations: List<String>,
                      val label: Char? = null,
                      val size: MarkerSize? = null,
                      val icon: String? = null,
                      val anchor: String? = null) {

    enum class MarkerColor(val value: String) {
        BLACK("black"),
        BROWN("brown"),
        GREEN("green"),
        PURPLE("purple"),
        YELLOW("yellow"),
        BLUE("blue"),
        GRAY("gray"),
        ORANGE("orange"),
        RED("red"),
        WHITE("white"),
    }

    enum class MarkerSize(val value: String) {
        TINY("tiny"),
        MID("mid"),
        SMALL("small")
    }

    override fun toString(): String {
        val sizeStr = if (size?.value != null) "size:${size.value}%7C" else ""
        val anchorStr = if (icon != null && anchor != null) "anchor:$anchor%7C" else ""
        val iconStr = if (icon != null) "icon:$icon%7C" else ""
        return "$anchorStr$iconStr${sizeStr}color:${color}%7Clabel:${label?.toUpperCase()}%7C${locations.joinToString("%7C")}"
    }
}

enum class MapType(val value: String) {
    ROADMAP("roadmap"),
    SATELLITE("satellite"),
    TERRAIN("terrain"),
    HYBRID("hybrid")
}

object MapsStaticUtil {
    /**
     * @param apiKey
     * @param central can be coordinate string "62.107733,-145.541936", or place name "Williamsburg,Brooklyn,NY"
     * @param markers see [MarkerInfo]
     * @param size such as "480x480"
     * @param mapType see [MapType]
     * @param zoomLevel the zoom level of the map, which determines the magnification level of the map.
     * @see <a href="https://developers.google.com/maps/documentation/maps-static/dev-guide">Maps Static API Get started</a>
     */
    fun generateMapStaticImageUrl(
            apiKey: String,
            central: String,
            markers: List<MarkerInfo>,
            size: String = "480x480",
            mapType: MapType = MapType.ROADMAP,
            zoomLevel: Int = 14): String {
        val endPoint = "https://maps.googleapis.com/maps/api/staticmap"
        val markersStr = markers.joinToString("&markers=")
        return "$endPoint?center=$central&zoom=$zoomLevel&size=$size&scale=2&maptype=${mapType.value}&markers=$markersStr&key=$apiKey"
    }
}
