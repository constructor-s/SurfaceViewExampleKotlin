package com.shirunjie.surfaceviewexample

class FlashlightCone(
    viewWidth: Int,
    viewHeight: Int
) {
    var x = viewWidth / 2
    var y = viewHeight / 2
    // Adjust the radius for the narrowest view dimension.
    val radius = if (viewWidth <= viewHeight) x / 3 else y / 3

    fun update(newX: Int, newY: Int) {
        x = newX
        y = newY
    }
}
