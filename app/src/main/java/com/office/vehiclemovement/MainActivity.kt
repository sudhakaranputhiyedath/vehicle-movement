package com.office.vehiclemovement

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.InputFilter
import android.text.InputType
import android.text.TextWatcher
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class MainActivity : Activity() {

    private val io = Executors.newSingleThreadExecutor()
    private val main = Handler(Looper.getMainLooper())
    private val zone: ZoneId get() = ZoneId.systemDefault()
    private val fmtDateTime = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    private val fmtCsv = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    private lateinit var vehicleInput: AutoCompleteTextView
    private lateinit var driverInput: AutoCompleteTextView
    private lateinit var placeInput: AutoCompleteTextView
    private lateinit var outTimeText: TextView
    private lateinit var outButton: Button
    private lateinit var statusText: TextView
    private lateinit var outsideTitle: TextView
    private lateinit var outsideList: LinearLayout
    private lateinit var historyTitle: TextView
    private lateinit var historyList: LinearLayout
    private lateinit var searchInput: EditText

    /** null means "use the current time when the OUT button is pressed". */
    private var chosenOutTime: LocalDateTime? = null
    private var trips: List<Trip> = emptyList()
    private var pendingCsv: String? = null

    private val autoRefresh = object : Runnable {
        override fun run() {
            refresh(quiet = true)
            main.postDelayed(this, 30_000)
        }
    }

    // ---------- lifecycle ----------

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(buildUi())
    }

    override fun onResume() {
        super.onResume()
        main.post(autoRefresh)
    }

    override fun onPause() {
        super.onPause()
        main.removeCallbacks(autoRefresh)
    }

    override fun onDestroy() {
        super.onDestroy()
        io.shutdown()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, MENU_REFRESH, 0, "Refresh").setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        menu.add(0, MENU_EXPORT, 1, "Export to Excel (CSV)")
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        MENU_REFRESH -> { refresh(quiet = false); true }
        MENU_EXPORT -> { exportCsv(); true }
        else -> super.onOptionsItemSelected(item)
    }

    // ---------- UI building ----------

    private fun buildUi(): View {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(12), dp(16), dp(24))
        }

        // Vehicle OUT form
        val form = card()
        form.addView(sectionTitle("Vehicle Out  /  വാഹനം പുറത്തേക്ക്"))
        vehicleInput = autoInput("Vehicle number (വാഹന നമ്പർ)", caps = true)
        driverInput = autoInput("Driver name (ഡ്രൈവർ)", caps = false)
        placeInput = autoInput("Place (സ്ഥലം)", caps = false)
        form.addView(vehicleInput)
        form.addView(driverInput)
        form.addView(placeInput)

        val timeRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        outTimeText = TextView(this).apply {
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
        }
        val changeTime = Button(this, null, android.R.attr.borderlessButtonStyle).apply {
            text = "Change time"
            setOnClickListener {
                pickDateTime(chosenOutTime ?: LocalDateTime.now()) { picked ->
                    chosenOutTime = picked
                    updateOutTimeLabel()
                }
            }
        }
        timeRow.addView(outTimeText)
        timeRow.addView(changeTime)
        form.addView(timeRow)
        updateOutTimeLabel()

        outButton = coloredButton("VEHICLE OUT", COLOR_OUT).apply {
            setOnClickListener { submitOut() }
        }
        form.addView(outButton, LinearLayout.LayoutParams(MATCH_PARENT, dp(52)))
        root.addView(form)

        statusText = TextView(this).apply {
            textSize = 13f
            setTextColor(Color.GRAY)
            setPadding(dp(4), dp(8), dp(4), 0)
        }
        root.addView(statusText)

        // Vehicles currently outside
        outsideTitle = sectionTitle("Vehicles outside").apply { setPadding(dp(4), dp(16), 0, dp(4)) }
        root.addView(outsideTitle)
        outsideList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(outsideList)

        // History
        historyTitle = sectionTitle("History").apply { setPadding(dp(4), dp(20), 0, dp(4)) }
        root.addView(historyTitle)
        searchInput = EditText(this).apply {
            hint = "Search vehicle / driver / place / date"
            isSingleLine = true
            addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
                override fun afterTextChanged(s: Editable?) = renderHistory()
            })
        }
        root.addView(searchInput)
        historyList = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(historyList)

        return ScrollView(this).apply {
            isFillViewport = true
            setBackgroundColor(Color.rgb(245, 246, 248))
            addView(root, MATCH_PARENT, WRAP_CONTENT)
        }
    }

    private fun card(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(14), dp(12), dp(14), dp(14))
        background = GradientDrawable().apply {
            setColor(Color.WHITE)
            cornerRadius = dp(10).toFloat()
            setStroke(dp(1), Color.rgb(222, 225, 230))
        }
        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT).apply {
            bottomMargin = dp(8)
        }
    }

    private fun sectionTitle(text: String) = TextView(this).apply {
        this.text = text
        textSize = 17f
        setTypeface(typeface, Typeface.BOLD)
        setTextColor(Color.rgb(33, 33, 33))
    }

    private fun autoInput(hintText: String, caps: Boolean) = AutoCompleteTextView(this).apply {
        hint = hintText
        isSingleLine = true
        threshold = 1
        inputType = InputType.TYPE_CLASS_TEXT or
            (if (caps) InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS else InputType.TYPE_TEXT_FLAG_CAP_WORDS)
        if (caps) filters = arrayOf(InputFilter.AllCaps())
    }

    private fun coloredButton(label: String, color: Int) = Button(this).apply {
        text = label
        setTextColor(Color.WHITE)
        textSize = 16f
        setTypeface(typeface, Typeface.BOLD)
        background = GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(8).toFloat()
        }
    }

    private fun updateOutTimeLabel() {
        val t = chosenOutTime
        outTimeText.text = if (t == null) "Out time: Now" else "Out time: ${t.format(fmtDateTime)}"
    }

    // ---------- actions ----------

    private fun submitOut() {
        val vehicle = vehicleInput.text.toString().trim().uppercase()
        val driver = driverInput.text.toString().trim()
        val place = placeInput.text.toString().trim()
        when {
            vehicle.isEmpty() -> { vehicleInput.error = "Required"; return }
            driver.isEmpty() -> { driverInput.error = "Required"; return }
            place.isEmpty() -> { placeInput.error = "Required"; return }
        }
        val outTime = (chosenOutTime ?: LocalDateTime.now()).atZone(zone).toOffsetDateTime()
        if (outTime.isAfter(OffsetDateTime.now().plusMinutes(5))) {
            toast("Out time cannot be in the future")
            return
        }
        if (trips.any { it.inTime == null && it.vehicleNo.equals(vehicle, ignoreCase = true) }) {
            AlertDialog.Builder(this)
                .setTitle("Already outside")
                .setMessage("$vehicle is already marked as outside. Add another OUT entry anyway?")
                .setPositiveButton("Add anyway") { _, _ -> doSubmitOut(vehicle, driver, place, outTime) }
                .setNegativeButton("Cancel", null)
                .show()
            return
        }
        doSubmitOut(vehicle, driver, place, outTime)
    }

    private fun doSubmitOut(vehicle: String, driver: String, place: String, outTime: OffsetDateTime) {
        outButton.isEnabled = false
        runInBackground({ TripApi.addTrip(vehicle, driver, place, outTime) }) { result ->
            outButton.isEnabled = true
            result.onSuccess {
                toast("$vehicle marked OUT")
                vehicleInput.setText("")
                driverInput.setText("")
                placeInput.setText("")
                chosenOutTime = null
                updateOutTimeLabel()
                hideKeyboard()
                refresh(quiet = true)
            }.onFailure { showError("Could not save", it) }
        }
    }

    private fun askMarkIn(trip: Trip) {
        AlertDialog.Builder(this)
            .setTitle("${trip.vehicleNo} – Vehicle In")
            .setMessage("Driver: ${trip.driverName}\nPlace: ${trip.place}\nOut: ${fmt(trip.outTime)}\n\nWhen did it come back?")
            .setPositiveButton("Now") { _, _ -> doMarkIn(trip, OffsetDateTime.now()) }
            .setNeutralButton("Pick time") { _, _ ->
                pickDateTime(LocalDateTime.now()) { picked ->
                    doMarkIn(trip, picked.atZone(zone).toOffsetDateTime())
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun doMarkIn(trip: Trip, inTime: OffsetDateTime) {
        if (inTime.isBefore(trip.outTime)) {
            toast("In time cannot be before out time (${fmt(trip.outTime)})")
            return
        }
        if (inTime.isAfter(OffsetDateTime.now().plusMinutes(5))) {
            toast("In time cannot be in the future")
            return
        }
        runInBackground({ TripApi.markIn(trip.id, inTime) }) { result ->
            result.onSuccess { updated ->
                toast(if (updated) "${trip.vehicleNo} marked IN" else "${trip.vehicleNo} was already marked IN")
                refresh(quiet = true)
            }.onFailure { showError("Could not save", it) }
        }
    }

    private fun refresh(quiet: Boolean) {
        if (!quiet) statusText.text = "Loading…"
        runInBackground({ TripApi.loadTrips() }) { result ->
            result.onSuccess {
                trips = it
                statusText.text = "Updated ${LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss a"))}"
                renderAll()
            }.onFailure {
                statusText.text = "Offline – could not load (${it.message})"
                if (!quiet) showError("Could not load", it)
            }
        }
    }

    // ---------- rendering ----------

    private fun renderAll() {
        updateSuggestions()
        renderOutside()
        renderHistory()
    }

    private fun updateSuggestions() {
        fun adapter(values: List<String>) =
            ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, values.distinct())
        vehicleInput.setAdapter(adapter(trips.map { it.vehicleNo }))
        driverInput.setAdapter(adapter(trips.map { it.driverName }))
        placeInput.setAdapter(adapter(trips.map { it.place }))
    }

    private fun renderOutside() {
        val outside = trips.filter { it.inTime == null }
        outsideTitle.text = "Vehicles outside (${outside.size})  /  പുറത്തുള്ളവ"
        outsideList.removeAllViews()
        if (outside.isEmpty()) {
            outsideList.addView(muted("All vehicles are in."))
            return
        }
        for (trip in outside) {
            val row = card().apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
            }
            val info = TextView(this).apply {
                textSize = 15f
                setTextColor(Color.rgb(33, 33, 33))
                text = "${trip.vehicleNo}\n${trip.driverName}  →  ${trip.place}\n" +
                    "Out: ${fmt(trip.outTime)}  (${duration(trip.outTime, OffsetDateTime.now())} ago)"
                layoutParams = LinearLayout.LayoutParams(0, WRAP_CONTENT, 1f)
            }
            val inBtn = coloredButton("IN", COLOR_IN).apply {
                setOnClickListener { askMarkIn(trip) }
            }
            row.addView(info)
            row.addView(inBtn, LinearLayout.LayoutParams(dp(76), dp(48)))
            outsideList.addView(row)
        }
    }

    private fun renderHistory() {
        if (!::historyList.isInitialized) return
        val q = searchInput.text.toString().trim().lowercase()
        val list = trips.filter { t ->
            q.isEmpty() || listOf(t.vehicleNo, t.driverName, t.place, fmt(t.outTime), t.inTime?.let { fmt(it) } ?: "")
                .any { it.lowercase().contains(q) }
        }
        historyTitle.text = "History (${list.size})  /  ചരിത്രം"
        historyList.removeAllViews()
        if (list.isEmpty()) {
            historyList.addView(muted(if (trips.isEmpty()) "No trips yet." else "No matching trips."))
            return
        }
        for (t in list.take(300)) {
            val row = card()
            val head = TextView(this).apply {
                textSize = 15f
                setTypeface(typeface, Typeface.BOLD)
                setTextColor(Color.rgb(33, 33, 33))
                text = "${t.vehicleNo}  •  ${t.driverName}  •  ${t.place}"
            }
            val inLine = t.inTime?.let { "In:   ${fmt(it)}   (${duration(t.outTime, it)})" } ?: "In:   — still outside"
            val body = TextView(this).apply {
                textSize = 14f
                setTextColor(if (t.inTime == null) COLOR_OUT else Color.rgb(80, 80, 80))
                text = "Out: ${fmt(t.outTime)}\n$inLine"
            }
            row.addView(head)
            row.addView(body)
            historyList.addView(row)
        }
    }

    private fun muted(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(Color.GRAY)
        setPadding(dp(4), dp(4), dp(4), dp(8))
    }

    // ---------- CSV export ----------

    private fun exportCsv() {
        if (trips.isEmpty()) { toast("Nothing to export"); return }
        val sb = StringBuilder("﻿Vehicle No,Driver Name,Place,Out Time,In Time,Duration\r\n")
        for (t in trips.sortedBy { it.outTime }) {
            sb.append(listOf(
                t.vehicleNo,
                t.driverName,
                t.place,
                t.outTime.atZoneSameInstant(zone).format(fmtCsv),
                t.inTime?.atZoneSameInstant(zone)?.format(fmtCsv) ?: "",
                t.inTime?.let { duration(t.outTime, it) } ?: "outside",
            ).joinToString(",") { csvCell(it) }).append("\r\n")
        }
        pendingCsv = sb.toString()
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/csv"
            putExtra(Intent.EXTRA_TITLE, "vehicle-movement-${LocalDate.now()}.csv")
        }
        startActivityForResult(intent, REQ_EXPORT)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQ_EXPORT) return
        val uri = data?.data
        val csv = pendingCsv
        pendingCsv = null
        if (resultCode != RESULT_OK || uri == null || csv == null) return
        try {
            contentResolver.openOutputStream(uri)?.use { it.write(csv.toByteArray(Charsets.UTF_8)) }
            toast("Exported ${trips.size} trips")
        } catch (e: Exception) {
            showError("Export failed", e)
        }
    }

    private fun csvCell(s: String): String =
        if (s.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) "\"" + s.replace("\"", "\"\"") + "\"" else s

    // ---------- helpers ----------

    private fun pickDateTime(start: LocalDateTime, onPicked: (LocalDateTime) -> Unit) {
        DatePickerDialog(this, { _, y, m, d ->
            TimePickerDialog(this, { _, h, min ->
                onPicked(LocalDateTime.of(y, m + 1, d, h, min))
            }, start.hour, start.minute, false).show()
        }, start.year, start.monthValue - 1, start.dayOfMonth).apply {
            datePicker.maxDate = System.currentTimeMillis()
        }.show()
    }

    private fun <T> runInBackground(work: () -> T, done: (Result<T>) -> Unit) {
        io.execute {
            val r = runCatching(work)
            main.post { if (!isFinishing && !isDestroyed) done(r) }
        }
    }

    private fun fmt(t: OffsetDateTime): String = t.atZoneSameInstant(zone).format(fmtDateTime)

    private fun duration(from: OffsetDateTime, to: OffsetDateTime): String {
        val mins = Duration.between(from, to).toMinutes().coerceAtLeast(0)
        val days = mins / (60 * 24)
        val hours = (mins / 60) % 24
        val m = mins % 60
        return when {
            days > 0 -> "${days}d ${hours}h ${m}m"
            hours > 0 -> "${hours}h ${m}m"
            else -> "${m}m"
        }
    }

    private fun showError(title: String, e: Throwable) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage((e.message ?: e.toString()) + "\n\nCheck the internet connection and try again.")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        currentFocus?.let { imm.hideSoftInputFromWindow(it.windowToken, 0) }
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    companion object {
        private const val MENU_REFRESH = 1
        private const val MENU_EXPORT = 2
        private const val REQ_EXPORT = 10
        private val COLOR_OUT = Color.rgb(198, 40, 40)
        private val COLOR_IN = Color.rgb(46, 125, 50)
    }
}
