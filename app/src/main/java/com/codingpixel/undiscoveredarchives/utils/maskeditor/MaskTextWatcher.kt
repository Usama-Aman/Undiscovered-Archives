package com.android.sitbak.utils.maskeditor

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import com.codingpixel.undiscoveredarchives.utils.maskeditor.isPlaceHolder
import java.lang.ref.WeakReference
import java.math.BigDecimal
import java.text.NumberFormat


class MaskTextWatcher(private val editText: EditText, private val mask: String) :
        TextWatcher {

    private var selfChange = false

    override fun afterTextChanged(s: Editable?) {
        if (selfChange) return
        selfChange = true
        format(s)
        selfChange = false
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
    }

    fun format(text: Editable?) {
        if (text.isNullOrEmpty()) return
        text.apply {
            // reset input filters
            val editableFilters = filters
            filters = emptyArray()

            val formatted = StringBuilder()
            val list = toMutableList()

            // apply mask
            mask.forEach { m ->
                if (list.isNullOrEmpty()) return@forEach
                var c = list[0]
                if (m.isPlaceHolder()) {
                    if (!c.isLetterOrDigit()) {
                        // find next letter or digit
                        val iterator = list.iterator()
                        while (iterator.hasNext()) {
                            c = iterator.next()
                            if (c.isLetterOrDigit()) break
                            iterator.remove()
                        }
                    }
                    if (list.isNullOrEmpty()) return@forEach
                    formatted.append(c)
                    list.removeAt(0)
                }
                else {
                    formatted.append(m)
                    if (m == c) {
                        list.removeAt(0)
                    }
                }
            }
            val previousLength = length
            val currentLength = formatted.length
            replace(0, previousLength, formatted, 0, currentLength)

            // set correct cursor position when editing
            if (currentLength < previousLength) {
                val currentSelection = findCursorPosition(text, editText.selectionStart)
                editText.setSelection(currentSelection)
            }

            // restore input filters
            filters = editableFilters
        }
    }

    fun unFormat(text: Editable?): String? {
        if (text.isNullOrEmpty()) return null
        val unformatted = StringBuilder()
        val textLength = text.length
        mask.forEachIndexed { index, m ->
            if (index >= textLength) return@forEachIndexed
            if (m.isPlaceHolder()) {
                unformatted.append(text[index])
            }
        }
        return unformatted.toString()
    }

    private fun findCursorPosition(text: Editable?, start: Int): Int {
        if (text.isNullOrEmpty()) return start
        val textLength = text.length
        val maskLength = mask.length
        var position = start
        for (i in start until maskLength) {
            if (mask[i].isPlaceHolder()) {
                break
            }
            position++
        }
        position++
        return if (position < textLength) position else textLength
    }
}


class MoneyTextWatcher(editText: EditText) : TextWatcher {
    private val editTextWeakReference: WeakReference<EditText> = WeakReference(editText)

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}

    override fun afterTextChanged(editable: Editable) {
        val editText = editTextWeakReference.get() ?: return
        val s = editable.toString()
        if (s.isEmpty()) return
        editText.removeTextChangedListener(this)
        val cleanString = s.replace("[$,.]".toRegex(), "")
        val parsed = BigDecimal(cleanString).setScale(2, BigDecimal.ROUND_FLOOR)
            .divide(BigDecimal(100), BigDecimal.ROUND_FLOOR)
        val formatted = NumberFormat.getCurrencyInstance().format(parsed)
        editText.setText(formatted)
        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)
    }
}