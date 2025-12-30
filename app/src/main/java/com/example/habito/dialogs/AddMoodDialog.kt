package com.example.habito.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.habito.R
import com.example.habito.data.MoodEntry
import com.google.android.material.button.MaterialButton

/**
 * Dialog for adding a new mood entry
 */
class AddMoodDialog(
    private val context: Context,
    private val onSave: (MoodEntry) -> Unit
) : DialogFragment() {

    private var selectedEmoji = "😊"
    private lateinit var noteEditText: EditText

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_add_mood, null)
        
        noteEditText = view.findViewById(R.id.editTextNote)
        
        // Setup emoji selection
        setupEmojiSelection(view)
        
        return AlertDialog.Builder(context)
            .setTitle("Add Mood")
            .setView(view)
            .setPositiveButton("Save") { _, _ ->
                val note = noteEditText.text.toString().trim()
                val moodEntry = MoodEntry(
                    emoji = selectedEmoji,
                    note = note
                )
                onSave(moodEntry)
            }
            .setNegativeButton("Cancel", null)
            .create()
    }

    private fun setupEmojiSelection(view: View) {
        val emojiContainer = view.findViewById<LinearLayout>(R.id.emojiContainer)
        val emojis = listOf("😢", "😔", "😐", "😊", "😄", "🤩")
        
        emojis.forEach { emoji ->
            val button = MaterialButton(context).apply {
                text = emoji
                textSize = 24f
                minWidth = 0
                setPadding(16, 8, 16, 8)
                val layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                layoutParams.marginEnd = 8
                layoutParams.bottomMargin = 8
                this.layoutParams = layoutParams
                setOnClickListener {
                    selectedEmoji = emoji
                    // Update button states
                    for (i in 0 until emojiContainer.childCount) {
                        val child = emojiContainer.getChildAt(i)
                        if (child is MaterialButton) {
                            child.isSelected = child.text == emoji
                        }
                    }
                }
            }
            emojiContainer.addView(button)
        }
    }
}
