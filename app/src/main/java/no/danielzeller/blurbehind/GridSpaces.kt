package no.danielzeller.blurbehind

import android.graphics.Rect
import android.support.v7.widget.RecyclerView
import android.view.View
import no.danielzeller.blurbehind.R

class GridSpaces(val space: Int, val topPadding: Int, val bottomPadding: Int) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val position = parent.getChildAdapterPosition(view)

        outRect.top = space
        outRect.bottom = space
        outRect.left = space
        outRect.right = space

        //Padding for small grid items. Don't do this at home kids.
        val adapter = parent.adapter!!
        if (adapter.getItemViewType(position) == R.layout.card4) {
            if (adapter.getItemViewType(position - 1) == R.layout.card4) {
                outRect.left = space / 2
            } else {
                outRect.right = space / 2
            }
        }

        //Padding top first item
        if (position == 0) {
            outRect.top = topPadding
        }

        //Padding bottom last item
        if (position == adapter.itemCount - 1) {
            outRect.bottom = bottomPadding
        }
    }
}