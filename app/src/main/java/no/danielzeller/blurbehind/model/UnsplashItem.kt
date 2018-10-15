package no.danielzeller.blurbehind.model

import java.io.Serializable

class UnsplashItem(val imageUrl: String, val heading: String, val subHeading: String, val layoutID: Int, val articleContent: String, val action: Any) : Serializable