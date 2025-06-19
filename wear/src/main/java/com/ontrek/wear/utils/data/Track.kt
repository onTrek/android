package com.ontrek.wear.utils.data

class Track(private var id: String, private var title: String) {  // TODO: convert to data class

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