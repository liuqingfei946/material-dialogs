/*
 * Licensed under Apache-2.0
 *
 * Designed and developed by Aidan Follestad (@afollestad)
 */
package com.afollestad.materialdialogs.internal.list

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.internal.main.DialogLayout
import com.afollestad.materialdialogs.utils.invalidateDividers
import com.afollestad.materialdialogs.utils.waitForLayout

typealias InvalidateDividersDelegate = (scrolledDown: Boolean, atBottom: Boolean) -> Unit

/**
 * A [RecyclerView] which reports whether or not it's scrollable, along with reporting back to a
 * [DialogLayout] to invalidate dividers.
 *
 * @author Aidan Follestad (afollestad)
 */
class DialogRecyclerView(
  context: Context,
  attrs: AttributeSet? = null
) : RecyclerView(context, attrs) {

  private var invalidateDividersDelegate: InvalidateDividersDelegate? = null

  fun attach(dialog: MaterialDialog) {
    this.invalidateDividersDelegate = dialog::invalidateDividers
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    waitForLayout {
      invalidateDividers()
      invalidateOverScroll()
    }
    addOnScrollListener(scrollListeners)
  }

  override fun onDetachedFromWindow() {
    removeOnScrollListener(scrollListeners)
    super.onDetachedFromWindow()
  }

  override fun onScrollChanged(
    left: Int,
    top: Int,
    oldl: Int,
    oldt: Int
  ) {
    super.onScrollChanged(left, top, oldl, oldt)
    invalidateDividers()
  }

  private fun isAtTop(): Boolean {
    val lm = layoutManager
    return when (lm) {
      is LinearLayoutManager -> lm.findFirstCompletelyVisibleItemPosition() == 0
      is GridLayoutManager -> lm.findFirstCompletelyVisibleItemPosition() == 0
      else -> false
    }
  }

  private fun isAtBottom(): Boolean {
    val lastIndex = adapter!!.itemCount - 1
    val lm = layoutManager
    return when (lm) {
      is LinearLayoutManager -> lm.findLastCompletelyVisibleItemPosition() == lastIndex
      is GridLayoutManager -> lm.findLastCompletelyVisibleItemPosition() == lastIndex
      else -> false
    }
  }

  private fun isScrollable() = isAtBottom() && isAtTop()

  private val scrollListeners = object : RecyclerView.OnScrollListener() {
    override fun onScrolled(
      recyclerView: RecyclerView,
      dx: Int,
      dy: Int
    ) {
      super.onScrolled(recyclerView, dx, dy)
      invalidateDividers()
    }
  }

  private fun invalidateDividers() {
    if (childCount == 0 || measuredHeight == 0) {
      return
    }
    invalidateDividersDelegate?.invoke(!isAtTop(), !isAtBottom())
  }

  private fun invalidateOverScroll() {
    overScrollMode = when {
      childCount == 0 || measuredHeight == 0 -> OVER_SCROLL_NEVER
      isScrollable() -> OVER_SCROLL_NEVER
      else -> OVER_SCROLL_IF_CONTENT_SCROLLS
    }
  }
}
