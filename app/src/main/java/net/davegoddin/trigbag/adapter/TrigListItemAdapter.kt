package net.davegoddin.trigbag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment.Companion.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import net.davegoddin.trigbag.R
import net.davegoddin.trigbag.model.TrigPointDisplay

class TrigListItemAdapter (private val fragment: Fragment, val dataset : List<TrigPointDisplay>) : RecyclerView.Adapter<TrigListItemAdapter.TrigListItemViewHolder>() {

    inner class TrigListItemViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val txtName : TextView = view.findViewById(R.id.txt_listitem_name)
        val txtDistance : TextView = view.findViewById(R.id.txt_listitem_distance)
        val imgBagged: ImageView = view.findViewById(R.id.img_listitem_bagged)
        val conDetails : ConstraintLayout = view.findViewById(R.id.con_listitem_details)
        val txtCondition : TextView = view.findViewById(R.id.txt_listitem_condition)
        val txtType : TextView = view.findViewById(R.id.txt_listitem_type)
        val txtCountry : TextView = view.findViewById(R.id.txt_listitem_country)
        val btnExpand : ImageButton = view.findViewById(R.id.btn_listitem_expand)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrigListItemViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.listitem_trig, parent, false)
        return TrigListItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: TrigListItemViewHolder, position: Int) {
        val item = dataset[position]

        //set text values
        holder.txtName.text = item.trigPoint.name
        holder.txtCondition.text = item.trigPoint.condition
        holder.txtCountry.text = item.trigPoint.country
        holder.txtType.text = item.trigPoint.type
        holder.txtDistance.text = "${String.format("%.1f", (item.distance/1000))} km"

        holder.txtName.setOnClickListener{
            fragment.findNavController().navigate(R.id.action_NearFragment_to_detailFragment)
        }


        //set visibility of bagged icon
        if (!item.visits.any())
        {
            holder.imgBagged.visibility = View.INVISIBLE
        }

        // expand/reduce details
        holder.btnExpand.setOnClickListener {
            if (holder.conDetails.isVisible)
            {
                holder.conDetails.visibility = View.GONE
                holder.btnExpand.setImageResource(R.drawable.expand_icon)
            }
            else
            {
                holder.conDetails.visibility = View.VISIBLE
                holder.btnExpand.setImageResource(R.drawable.reduce_icon)
            }
        }

    }

    override fun getItemCount() = dataset.size


}