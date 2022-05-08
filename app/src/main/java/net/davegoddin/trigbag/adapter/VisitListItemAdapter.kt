package net.davegoddin.trigbag.adapter

import android.media.Rating
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import net.davegoddin.trigbag.R
import net.davegoddin.trigbag.model.TrigPointDisplay
import net.davegoddin.trigbag.model.Visit
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class VisitListItemAdapter (private val fragment: Fragment, val dataset : List<Visit>) : RecyclerView.Adapter<VisitListItemAdapter.VisitListItemViewHolder>() {

    inner class VisitListItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtDate: TextView = view.findViewById(R.id.txt_visit_date)
        val txtComment: TextView = view.findViewById(R.id.txt_visit_comment)
        val ratRating: RatingBar = view.findViewById(R.id.rat_visit_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VisitListItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_visit, parent, false)
        return VisitListItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: VisitListItemViewHolder, position: Int) {
        val item = dataset[position]

        val visitDate = LocalDate.ofEpochDay(item.dateTime!!)

        holder.txtDate.text = visitDate.format(DateTimeFormatter.ofPattern("dd LLL yyyy"))
        holder.txtComment.text = item.comment
        holder.ratRating.rating = item.rating ?: 0f

    }

    override fun getItemCount() = dataset.count()

}