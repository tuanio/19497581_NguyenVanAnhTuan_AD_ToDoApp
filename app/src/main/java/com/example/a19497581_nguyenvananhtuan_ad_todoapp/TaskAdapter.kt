package com.example.a19497581_nguyenvananhtuan_ad_todoapp

import android.graphics.Color
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.databinding.ActivityMainBinding
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.model.TaskItemModel
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.model.TaskModel
import com.google.firebase.database.DatabaseReference

open class TaskAdapter(
    private val mainBinding: ActivityMainBinding,
    private var database: DatabaseReference,
    private val mainThis: MainActivity,
    private var dataset: MutableList<TaskModel>
    ):
    RecyclerView.Adapter<TaskAdapter.ViewHolder>() {

    private val taskAdapterTag = "TaskAdapterTag"
    private var selectedItem: Int? = null

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var desc: TextView = view.findViewById(R.id.text_view_desc)
        var status: TextView = view.findViewById(R.id.text_view_status)
        var dateString: TextView = view.findViewById(R.id.text_view_date_string)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_item, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.desc.text = dataset[position].data!!.desc
        holder.status.text = dataset[position].data!!.status?.let { Utils.getStatus(it) }
        holder.dateString.text = dataset[position].data!!.dateString
        holder.desc.tag = dataset[position].id
        holder.itemView.setOnClickListener { onClickItem(it, position) }

        val textView = holder.itemView.findViewById<TextView>(R.id.text_view_status)
        if (dataset[position].data!!.status == false) {
            textView
                .setTextColor(
                    ContextCompat
                        .getColor(
                            mainBinding.root.context,
                            android.R.color.tab_indicator_text
                        )
                )
        } else {
            textView
                .setTextColor(
                    Color.parseColor("#FF7900")
                )
        }

        if (selectedItem != null) {
            // ẩn mấy nút đi
            toggleMenu(true)

            if (selectedItem == position) {
                holder.itemView.setBackgroundColor(
                    Color.parseColor("#FFEEDD")
                )
            } else {
                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        mainBinding.root.context,
                        R.color.white
                    )
                )
            }
        } else {
            toggleMenu(false)
        }
    }

    private fun toggleMenu(isShow: Boolean = false) {
        val inflater = MenuInflater(mainBinding.root.context)
        inflater.inflate(R.menu.top_app_bar, mainBinding.materialToolbar.menu)

        arrayOf(
            R.id.item_check,
            R.id.item_delete,
            R.id.item_edit
        ).map {
            mainBinding
                .materialToolbar
                .menu
                .findItem(it)
                .isVisible = isShow
        }
    }

    private fun onClickItem(view: View, position: Int) {
        if (selectedItem != position) {
            val previousItem = selectedItem
            selectedItem = position
            previousItem?.apply { notifyItemChanged(this) }
            notifyItemChanged(position)
        }

        mainBinding.materialToolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.item_edit -> {
                    mainThis.showDialog(true, dataset[position], position)
                    true
                }
                R.id.item_delete -> {
                    dataset[position].id?.let { itemDataIdIt ->
                        database
                            .child("task")
                            .child(itemDataIdIt)
                            .removeValue()
                            .addOnSuccessListener {
                                dataset.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, itemCount)
                                if (position == itemCount) {
                                    toggleMenu(false)
                                }

                                Toast.makeText(
                                    mainBinding.root.context,
                                    "Item deleted!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { failIt ->
                                Toast.makeText(
                                    mainBinding.root.context,
                                    "Fail to delete item!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e(taskAdapterTag, "Fail to delete item", failIt)
                            }
                    }
                    true
                }
                R.id.item_check -> {

                    val itemUpdate = TaskModel(
                        dataset[position].id,
                        TaskItemModel(
                            dataset[position].data!!.desc,
                            dataset[position].data!!.status?.xor(true),
                            dataset[position].data!!.dateString
                        )
                    )

                    dataset[position].id?.let { itemDataIdIt ->

                        database
                            .child("task")
                            .child(itemDataIdIt)
                            .setValue(itemUpdate.data)
                            .addOnSuccessListener {
                                dataset[position] = itemUpdate
                                notifyItemChanged(position)
                                Toast.makeText(
                                    mainBinding.root.context,
                                    "Item checked!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }.addOnFailureListener { failIt ->
                                Toast.makeText(
                                    mainBinding.root.context,
                                    "Fail to checked item!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                Log.e(taskAdapterTag, "Fail to checked item", failIt)
                            }
                    }
                    true
                }
                else -> false
            }
        }
    }

    override fun getItemCount(): Int {
        return dataset.size
    }
}