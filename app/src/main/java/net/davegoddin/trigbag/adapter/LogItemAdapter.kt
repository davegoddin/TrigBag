package net.davegoddin.trigbag.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import net.davegoddin.trigbag.R
import net.davegoddin.trigbag.model.TrigPointDisplay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LogItemAdapter (private val fragment: Fragment, val dataset : List<TrigPointDisplay>) : RecyclerView.Adapter<LogItemAdapter.LogItemViewHolder>() {
    inner class LogItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtTitle = view.findViewById<TextView>(R.id.txt_logitem_name)
        val txtDate = view.findViewById<TextView>(R.id.txt_logitem_lastvisit)
        val txtVisits = view.findViewById<TextView>(R.id.txt_logitem_totalvisits)
        val ratAvgRating = view.findViewById<RatingBar>(R.id.rat_logitem_rating)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_log, parent, false)
        return LogItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: LogItemViewHolder, position: Int) {
        val item = dataset[position]

        val sortedVisits = item.visits.sortedByDescending { visit -> visit.dateTime }

        var avgRating = 0f
        var ratingCount = 0
        item.visits.forEach {
            if (it.rating != null && it.rating != 0f)
            {
                ratingCount++
                avgRating += it.rating
            }
        }

        avgRating /= ratingCount

        holder.txtTitle.text = item.trigPoint.name

        val args : Bundle = Bundle()
        args.putString("trigPointDisplay", Gson().toJson(item))

        holder.txtTitle.setOnClickListener{
            fragment.findNavController().navigate(R.id.action_LogFragment_to_detailFragment, args)
        }

        if (sortedVisits.any())
        {
            holder.txtDate.text = LocalDate.ofEpochDay(sortedVisits[0].dateTime!!).format(DateTimeFormatter.ofPattern("dd LLL yyyy"))
        }
        else
        {
            holder.txtDate.text = "Not visited"
        }

        holder.ratAvgRating.rating = avgRating
        holder.txtVisits.text = item.visits.count().toString()
    }

    override fun getItemCount() = dataset.count()
}