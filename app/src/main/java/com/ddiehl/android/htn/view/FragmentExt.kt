package com.ddiehl.android.htn.view

import android.content.Context
import androidx.fragment.app.Fragment

/**
 * Checks the type of the fragment's host [Context] or parent [Fragment]. If one of those is an instance
 * of type [T], it is returned.
 *
 * Throws an [IllegalStateException] if the neither the host [Context] nor parent [Fragment] match the expected type.
 */
inline fun <reified T> Fragment.getDelegate(): T {
    val context = context
    if (context != null && context is T) {
        return context
    }

    val parentFragment = parentFragment
    if (parentFragment != null && parentFragment is T) {
        return parentFragment
    }

    throw IllegalStateException("Either host Context or parent Fragment must implement ${T::class.java.simpleName}")
}

/**
 * Checks the type of the fragment's host [Context] or parent [Fragment]. If one of those is an instance
 * of type [T], it is returned.
 *
 * Throws an [IllegalStateException] if the neither the host [Context] nor parent [Fragment] match the expected type.
 *
 * Same as [getDelegate] but doesn't use a reified type for Java compatibility.
 */
fun <T> Fragment.getDelegate(clazz: Class<T>): T {
    val context = context
    @Suppress("UNCHECKED_CAST") // We're actually checking it
    if (context != null && clazz.isInstance(context)) {
        return context as T
    }

    val parentFragment = parentFragment
    @Suppress("UNCHECKED_CAST") // We're actually checking it
    if (parentFragment != null && clazz.isInstance(parentFragment)) {
        return parentFragment as T
    }

    throw IllegalStateException("Either host Context or parent Fragment must implement ${clazz.simpleName}")
}
