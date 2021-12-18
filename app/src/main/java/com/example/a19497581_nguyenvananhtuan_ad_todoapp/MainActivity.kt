package com.example.a19497581_nguyenvananhtuan_ad_todoapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.databinding.ActivityMainBinding
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.model.TaskItemModel
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.model.TaskModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.logging.SimpleFormatter
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity(), DialogAddItem.DialogAddItemListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var database: DatabaseReference
    private var layoutManager: RecyclerView.LayoutManager? = null
    private var adapter: RecyclerView.Adapter<TaskAdapter.ViewHolder>? = null
    private val firebaseTag = "FirebaseTag"
    private lateinit var dataset: MutableList<TaskModel>
    private val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        database = Firebase.database.reference

        setContentView(binding.root)

        layoutManager = LinearLayoutManager(this)

        binding.recyclerView.layoutManager = layoutManager

        createAdapter()

        binding.buttonAddItem.setOnClickListener { showDialog() }
    }

    private fun createAdapter() {
        database.child("task").get()
            .addOnSuccessListener {
                dataset = it.children.map { el ->
                    TaskModel(
                        el.key.toString(),
                        el.getValue(TaskItemModel::class.java)
                    )
                } as ArrayList<TaskModel>

                // initialize adapter
                adapter = TaskAdapter(binding, database, this, dataset)
                binding.recyclerView.adapter = adapter

                // add divider
                val decorator = DividerItemDecoration(binding.recyclerView.context, LinearLayoutManager.VERTICAL)
                binding.recyclerView.addItemDecoration(decorator)

            }.addOnFailureListener {
                Log.e(firebaseTag, "Fail to get task", it)
            }
    }

    fun showDialog(isEdit: Boolean = false, currentItem: TaskModel? = null, currentItemPosition: Int? = null) {
        val dialogAddItem = DialogAddItem(binding, isEdit, currentItem, currentItemPosition)
        dialogAddItem.show(this.supportFragmentManager, "additemtag")
    }

    override fun onDialogPositiveClick(dialog: DialogFragment, isEdit: Boolean, currentItem: TaskModel?, currentItemPosition: Int?) {
        val taskDesc: EditText = dialog.dialog!!.findViewById(R.id.edit_text_task_desc)

        val textViewDateTime: TextView = dialog.dialog!!.findViewById(R.id.text_view_datetime)
        val dateTimeFormatted = sdf.parse(textViewDateTime.text.toString())

        val uniqueID = when(isEdit) {
            false -> "${dateTimeFormatted.time}${System.currentTimeMillis()}" // parsed date + current date for automatic sort
            else -> currentItem?.id
        }
        val data = TaskItemModel(
            taskDesc.text.toString(),
            false,
            textViewDateTime.text.toString()
        )
        if (currentItemPosition != null) {
            data.status = dataset[currentItemPosition].data?.status
        }
        if (uniqueID != null) {
            database
                .child("task")
                .child(uniqueID)
                .setValue(data)
                .addOnSuccessListener {
                    // update adapter
                    val item = TaskModel(uniqueID, data)

                    if (isEdit) {
                        dataset[currentItemPosition!!] = item
                        adapter?.notifyItemChanged(currentItemPosition)
                    } else {
                        dataset.add(item)
                        adapter?.notifyItemInserted(dataset.size - 1)
                    }
                }.addOnFailureListener {
                    Toast.makeText(
                        baseContext,
                        "Fail to set value",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onDialogNegativeClick(dialog: DialogFragment, isEdit: Boolean, currentItem: TaskModel?, currentItemPosition: Int?) {

    }

}