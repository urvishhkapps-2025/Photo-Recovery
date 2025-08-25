// SelectionHost.kt
package com.Blue.photorecovery.common

interface SelectionHost {
    /** Delete currently selected items. Return the number deleted. */
    fun onDeleteRequest(): Int
}

interface PageVisibility {
    fun onHidden()
}