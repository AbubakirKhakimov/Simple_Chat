package com.x.a_technologies.simple_chat.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.x.a_technologies.simple_chat.databinding.ChooseCountryItemLayoutBinding
import com.x.a_technologies.simple_chat.models.PhoneMask

interface ChooseCountryAdapterCallBack{
    fun itemSelected(position: Int)
}

class ChooseCountryAdapter(private val phoneMasksList: ArrayList<PhoneMask>, private val chooseCountryAdapterCallBack: ChooseCountryAdapterCallBack)
    : RecyclerView.Adapter<ChooseCountryAdapter.ItemHolder>(), Filterable{
    inner class ItemHolder(val binding: ChooseCountryItemLayoutBinding):RecyclerView.ViewHolder(binding.root)

    private var filteredList = phoneMasksList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemHolder {
        return ItemHolder(ChooseCountryItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ItemHolder, position: Int) {
        val item = filteredList[position]
        holder.binding.name.text = item.name
        holder.binding.countryCode.text = item.countryCode
        Glide.with(holder.binding.root).load(item.imageUrl).into(holder.binding.image)

        holder.binding.root.setOnClickListener {
            chooseCountryAdapterCallBack.itemSelected(phoneMasksList.indexOf(item))
        }
    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    override fun getFilter(): Filter {
        return object: Filter(){
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredMasks = ArrayList<PhoneMask>()

                if (constraint.toString().isNotEmpty()){
                    for (item in phoneMasksList){
                        if (item.name.lowercase().contains(constraint.toString().lowercase())){
                            filteredMasks.add(item)
                        }
                    }
                }else{
                    filteredMasks.addAll(phoneMasksList)
                }

                val filterResults = FilterResults()
                filterResults.values = filteredMasks
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as ArrayList<PhoneMask>
                notifyDataSetChanged()
            }
        }
    }
}