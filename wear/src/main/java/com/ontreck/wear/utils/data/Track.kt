package com.ontreck.wear.utils.data

class Track(private var id: String, private var title: String) {

    fun getId(): String {
        return id
    }

    fun getTitle(): String {
        return title
    }

    fun setTitle(title: String) {
        this.title = title
    }

}