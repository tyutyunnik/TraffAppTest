package com.fhww.sprt.saad

data class Rectangle(
    var x: Float,
    var y: Float,
    var width: Float,
    var height: Float
)

fun Rectangle.overlaps(r: Rectangle): Boolean =
    x < r.x + r.width && x + width > r.x && y < r.y + r.height && y + height > r.y