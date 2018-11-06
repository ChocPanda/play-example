package io.panda.example.controllers

import play.api.http.HeaderNames

package object tools {

  implicit class CustomHeaderNames(headerNames: HeaderNames) {
    val BEARER_TOKEN: String = "bearer"
  }

}
