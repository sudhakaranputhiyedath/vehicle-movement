package com.office.vehiclemovement

import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.time.OffsetDateTime

data class Trip(
    val id: Long,
    val vehicleNo: String,
    val driverName: String,
    val place: String,
    val outTime: OffsetDateTime,
    val inTime: OffsetDateTime?,
)

/** Talks to the shared Supabase database. Every call blocks, so run it off the main thread. */
object TripApi {
    private const val BASE_URL = "https://ttxkexsrzuejwhxqetjl.supabase.co/rest/v1"

    // Public "anon" key. The database only lets it read trips, add trips and mark them in.
    private const val API_KEY =
        "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InR0eGtleHNyenVlandoeHFldGpsIiwicm9sZSI6ImFub24iLCJpYXQiOjE3OTM0OTM1NzEsImV4cCI6MjEwOTA2OTU3MX0.b7Zr-QE7kUj0p6zVPBVX4kQQEyXTAi0gE78w4BrSlno"

    fun loadTrips(limit: Int = 500): List<Trip> {
        val body = request(
            "GET",
            "/vehicle_trips?select=id,vehicle_no,driver_name,place,out_time,in_time" +
                "&order=out_time.desc&limit=$limit",
            null,
        )
        val arr = JSONArray(body)
        return (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Trip(
                id = o.getLong("id"),
                vehicleNo = o.getString("vehicle_no"),
                driverName = o.getString("driver_name"),
                place = o.getString("place"),
                outTime = OffsetDateTime.parse(o.getString("out_time")),
                inTime = if (o.isNull("in_time")) null else OffsetDateTime.parse(o.getString("in_time")),
            )
        }
    }

    fun addTrip(vehicleNo: String, driverName: String, place: String, outTime: OffsetDateTime) {
        val json = JSONObject()
            .put("vehicle_no", vehicleNo)
            .put("driver_name", driverName)
            .put("place", place)
            .put("out_time", outTime.toString())
        request("POST", "/vehicle_trips", json.toString(), prefer = "return=minimal")
    }

    /** Returns false if the trip was already marked in (for example from another phone). */
    fun markIn(tripId: Long, inTime: OffsetDateTime): Boolean {
        val json = JSONObject().put("trip_id", tripId).put("in_at", inTime.toString())
        return request("POST", "/rpc/mark_vehicle_in", json.toString()).trim() == "true"
    }

    private fun request(method: String, path: String, body: String?, prefer: String? = null): String {
        val conn = URL(BASE_URL + path).openConnection() as HttpURLConnection
        try {
            conn.requestMethod = method
            conn.connectTimeout = 15_000
            conn.readTimeout = 20_000
            conn.setRequestProperty("apikey", API_KEY)
            conn.setRequestProperty("Authorization", "Bearer $API_KEY")
            conn.setRequestProperty("Accept", "application/json")
            if (prefer != null) conn.setRequestProperty("Prefer", prefer)
            if (body != null) {
                conn.doOutput = true
                conn.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                conn.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            }
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val text = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() } ?: ""
            if (code !in 200..299) {
                val msg = runCatching { JSONObject(text).optString("message") }.getOrNull()
                throw IOException("Server error $code: ${msg?.ifBlank { null } ?: text.take(200)}")
            }
            return text
        } finally {
            conn.disconnect()
        }
    }
}
