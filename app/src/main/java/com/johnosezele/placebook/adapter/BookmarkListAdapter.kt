package com.johnosezele.placebook.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.johnosezele.placebook.R
import com.johnosezele.placebook.databinding.BookmarkItemBinding
import com.johnosezele.placebook.ui.MapsActivity
import com.johnosezele.placebook.viewmodel.MapsViewModel

class BookmarkListAdapter(
    private var bookmarkData: List<MapsViewModel.BookmarkView>?,
    private val mapsActivity: MapsActivity
) : RecyclerView.Adapter<BookmarkListAdapter.ViewHolder>(){

    class ViewHolder (
        val binding: BookmarkItemBinding,
        private val mapsActivity: MapsActivity
            ) : RecyclerView.ViewHolder(binding.root){
        //when click event is fired, get the bookmarkView associated with the ViewHolder and
        // call mapToBookmark() to zoom the map to the bookmark
                init {
                    binding.root.setOnClickListener {
                        val bookmarkView = itemView.tag as MapsViewModel.BookmarkView
                        mapsActivity.moveToBookmark(bookmarkView)
                    }
                }

            }

    //setBookmarkData is designed to be called when the bookmark data changes. It
    //assigns bookmarks to the new BookmarkView List and refreshes the
    //RecyclerView by calling notifyDataSetChanged() .

    fun setBookmarkData(bookmarks: List<MapsViewModel.BookmarkView>) {
        this.bookmarkData = bookmarks
        notifyDataSetChanged()
    }

    //onCreateViewHolder is used to create a ViewHolder by inflating
    //the bookmark_item layout and passing in the mapsActivity property
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       val layoutInflater = LayoutInflater.from(parent.context)
        val binding = BookmarkItemBinding.inflate(layoutInflater, parent, false)
        return  ViewHolder(binding, mapsActivity)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //make sure bookmarkData is not null before doing the binding
        bookmarkData?.let { list->

            //bookmarkViewData is assigned to the bookmark data for the current item position
            val bookmarkViewData = list[position]

            //A reference to the bookmarkViewData is assigned to the holder’s itemView.tag ,
            //and the ViewHolder items are populated from the bookmarkViewDa
            holder.binding.root.tag = bookmarkViewData
            holder.binding.bookmarkData = bookmarkViewData

            holder.binding.bookmarkIcon.setImageResource(R.drawable.ic_other)
        }
    }

    //getItemCount() is overridden to return the number of items in the
    //bookmarkData list
    override fun getItemCount() = bookmarkData?.size ?: 0


}


