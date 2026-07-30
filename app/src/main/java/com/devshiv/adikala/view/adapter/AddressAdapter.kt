package com.devshiv.adikala.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.devshiv.adikala.databinding.ItemAddressBinding
import com.devshiv.adikala.model.Address
import com.devshiv.adikala.repository.CustomerRepository

class AddressAdapter(
    private var addresses: List<Address>,
    private val onEdit: (Address) -> Unit,
    private val onDelete: (Address) -> Unit,
    private val onSetDefault: (Address) -> Unit
) : RecyclerView.Adapter<AddressAdapter.ViewHolder>() {

    fun updateList(newList: List<Address>) {
        addresses = newList
        notifyDataSetChanged()
    }

    inner class ViewHolder(
        val binding: ItemAddressBinding
    ) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemAddressBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val address = addresses[position]

        with(holder.binding) {

            tvDefaultBadge.visibility =
                if (address.isDefault) android.view.View.VISIBLE else android.view.View.GONE

            btnSetDefault.visibility =
                if (address.isDefault) android.view.View.GONE else android.view.View.VISIBLE

            tvAddressType.text =
                if (address.isDefault) "Default Address"
                else "Address ${position + 1}"

            val fullAddress = listOfNotNull(
                address.address1,
                address.address2,
                address.city,
                address.state,
                address.country,
                address.zip
            )
                .filter { it.isNotEmpty() }
                .joinToString(", ")

            tvFullAddress.text = fullAddress

            tvPhone.text = address.phone ?: "No phone"

            btnEdit.setOnClickListener {
                onEdit(address)
            }

            btnDelete.setOnClickListener {
                onDelete(address)
            }

            btnSetDefault.setOnClickListener {
                onSetDefault(address)
            }
        }
    }

    override fun getItemCount(): Int = addresses.size
}