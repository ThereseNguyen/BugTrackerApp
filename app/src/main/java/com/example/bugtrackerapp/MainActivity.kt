package com.example.bugtrackerapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
class MainActivity : AppCompatActivity() {

    private lateinit var database: BugDatabase
    private lateinit var bugDao: BugDao
    private lateinit var repository: BugRepository

    private var editingBug: Bug? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Create Room database
        val migration1To2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(
                database: androidx.sqlite.db.SupportSQLiteDatabase
            ) {
                database.execSQL(
                    "ALTER TABLE bugs ADD COLUMN creationDate INTEGER NOT NULL DEFAULT 0"
                )

                database.execSQL(
                    "ALTER TABLE bugs ADD COLUMN synced INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        database = Room.databaseBuilder(
            applicationContext,
            BugDatabase::class.java,
            "bug_database"
        )
            .addMigrations(migration1To2)
            .build()

        bugDao = database.bugDao()
        repository = BugRepository(bugDao)

        // Find views
        val titleEditText = findViewById<EditText>(R.id.titleEditText)
        val descriptionEditText = findViewById<EditText>(R.id.descriptionEditText)
        val statusSpinner = findViewById<Spinner>(R.id.statusSpinner)
        val prioritySpinner = findViewById<Spinner>(R.id.prioritySpinner)
        val addBugButton = findViewById<Button>(R.id.addBugButton)
        val syncBugsButton = findViewById<Button>(R.id.syncBugsButton)
        val bugListContainer = findViewById<LinearLayout>(R.id.bugListContainer)

        // Status options
        val statusOptions = arrayOf(
            "Open",
            "In Progress",
            "Resolved"
        )

        // Priority options
        val priorityOptions = arrayOf(
            "Low",
            "Medium",
            "High"
        )

        statusSpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            statusOptions
        )

        prioritySpinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            priorityOptions
        )

        // Load bugs
        loadBugs(
            bugListContainer,
            titleEditText,
            descriptionEditText,
            statusSpinner,
            prioritySpinner,
            addBugButton
        )

        // Add or Update button
        addBugButton.setOnClickListener {

            val title = titleEditText.text.toString().trim()
            val description = descriptionEditText.text.toString().trim()
            val status = statusSpinner.selectedItem.toString()
            val priority = prioritySpinner.selectedItem.toString()

            if (title.isEmpty() || description.isEmpty()) {

                Toast.makeText(
                    this,
                    "Please enter the title and description",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            val currentBug = editingBug

            if (currentBug == null) {

                val bug = Bug(
                    title = title,
                    description = description,
                    status = status,
                    priority = priority
                )

                lifecycleScope.launch(Dispatchers.IO) {

                    repository.addBug(bug)

                    runOnUiThread {

                        Toast.makeText(
                            this@MainActivity,
                            "Bug added successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        clearForm(
                            titleEditText,
                            descriptionEditText,
                            addBugButton
                        )

                        loadBugs(
                            bugListContainer,
                            titleEditText,
                            descriptionEditText,
                            statusSpinner,
                            prioritySpinner,
                            addBugButton
                        )
                    }
                }

            } else {

                val updatedBug = currentBug.copy(
                    title = title,
                    description = description,
                    status = status,
                    priority = priority
                )

                lifecycleScope.launch(Dispatchers.IO) {

                    repository.updateBug(updatedBug)

                    runOnUiThread {

                        Toast.makeText(
                            this@MainActivity,
                            "Bug updated successfully",
                            Toast.LENGTH_SHORT
                        ).show()

                        editingBug = null

                        clearForm(
                            titleEditText,
                            descriptionEditText,
                            addBugButton
                        )

                        loadBugs(
                            bugListContainer,
                            titleEditText,
                            descriptionEditText,
                            statusSpinner,
                            prioritySpinner,
                            addBugButton
                        )
                    }
                }
            }
        }

        // Sync button
        syncBugsButton.setOnClickListener {

            lifecycleScope.launch(Dispatchers.IO) {

                when (val result = repository.syncUnsyncedBugs()) {

                    SyncResult.Success -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "All bugs are synced",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }

                    SyncResult.NetworkError -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Network error. Bugs remain unsynced. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }

                    is SyncResult.Error -> {
                        runOnUiThread {
                            Toast.makeText(
                                this@MainActivity,
                                "Sync error: ${result.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")

    private fun loadBugs(
        container: LinearLayout,
        titleEditText: EditText,
        descriptionEditText: EditText,
        statusSpinner: Spinner,
        prioritySpinner: Spinner,
        actionButton: Button
    ) {

        lifecycleScope.launch(Dispatchers.IO) {

            val bugs = repository.getAllBugs()

            runOnUiThread {

                container.removeAllViews()

                for (bug in bugs) {

                    val bugLayout = LinearLayout(this@MainActivity)

                    bugLayout.orientation = LinearLayout.VERTICAL

                    bugLayout.setPadding(
                        16,
                        16,
                        16,
                        16
                    )

                    // Bug information
                    val bugTextView = TextView(this@MainActivity)

                    val dateFormat = SimpleDateFormat(
                        "dd/MM/yyyy HH:mm",
                        Locale.getDefault()
                    )

                    val formattedDate = dateFormat.format(
                        Date(bug.creationDate)
                    )

                    bugTextView.text = """
                    Bug #${bug.id}
                    Title: ${bug.title}
                    Description: ${bug.description}
                    Status: ${bug.status}
                    Priority: ${bug.priority}
                    Created: $formattedDate
                    Sync: ${if (bug.synced) "Synced" else "Not Synced"}
                    """.trimIndent()

                    bugTextView.textSize = 16f

                    // EDIT button
                    val editButton = Button(this@MainActivity)

                    editButton.text = "EDIT"

                    editButton.setOnClickListener {

                        editingBug = bug

                        titleEditText.setText(bug.title)
                        descriptionEditText.setText(bug.description)

                        setSpinnerValue(
                            statusSpinner,
                            bug.status
                        )

                        setSpinnerValue(
                            prioritySpinner,
                            bug.priority
                        )

                        actionButton.text = "UPDATE BUG"

                        titleEditText.requestFocus()

                        Toast.makeText(
                            this@MainActivity,
                            "Edit mode",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    // DELETE button
                    val deleteButton = Button(this@MainActivity)

                    deleteButton.text = "DELETE"

                    deleteButton.setOnClickListener {

                        lifecycleScope.launch(Dispatchers.IO) {

                            repository.deleteBug(bug)

                            runOnUiThread {

                                Toast.makeText(
                                    this@MainActivity,
                                    "Bug deleted",
                                    Toast.LENGTH_SHORT
                                ).show()

                                loadBugs(
                                    container,
                                    titleEditText,
                                    descriptionEditText,
                                    statusSpinner,
                                    prioritySpinner,
                                    actionButton
                                )
                            }
                        }
                    }

                    bugLayout.addView(bugTextView)
                    bugLayout.addView(editButton)
                    bugLayout.addView(deleteButton)

                    val layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    )

                    layoutParams.setMargins(
                        0,
                        0,
                        0,
                        16
                    )

                    bugLayout.layoutParams = layoutParams

                    container.addView(bugLayout)
                }
            }
        }
    }

    private fun setSpinnerValue(
        spinner: Spinner,
        value: String
    ) {

        val adapter = spinner.adapter

        for (i in 0 until adapter.count) {

            if (adapter.getItem(i).toString() == value) {

                spinner.setSelection(i)

                break
            }
        }
    }

    private fun clearForm(
        titleEditText: EditText,
        descriptionEditText: EditText,
        actionButton: Button
    ) {

        titleEditText.text.clear()
        descriptionEditText.text.clear()

        actionButton.text = "ADD BUG"
    }
}