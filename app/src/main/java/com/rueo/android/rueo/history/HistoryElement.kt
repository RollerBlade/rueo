package com.rueo.android.rueo.history

class HistoryElement constructor(var word: String) {
    var left: HistoryElement? = null
    var right: HistoryElement? = null

    override fun toString(): String {
        return word
    }
}
