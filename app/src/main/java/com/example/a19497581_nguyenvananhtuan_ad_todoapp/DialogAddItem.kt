package com.example.a19497581_nguyenvananhtuan_ad_todoapp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.fragment.app.DialogFragment
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.databinding.ActivityMainBinding
import com.example.a19497581_nguyenvananhtuan_ad_todoapp.model.TaskModel
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.min

class DialogAddItem(
    private val mainBinding: ActivityMainBinding,
    private val isEdit: Boolean = false,
    private val currentItem: TaskModel?,
    private val currentItemPosition: Int?
) : DialogFragment(), DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {

    private lateinit var listener: DialogAddItemListener
    private lateinit var textViewDateTime: TextView
    private val sdf = SimpleDateFormat("dd/MM/yyyy - HH:mm")
    private var day = 0
    private var month = 0
    private var year = 0
    private var hour = 0
    private var minute = 0
    private var savedDay = 0
    private var savedMonth = 0
    private var savedYear = 0
    private var savedHour = 0
    private var savedMinute = 0

    interface DialogAddItemListener {
        fun onDialogPositiveClick(dialog: DialogFragment, isEdit: Boolean, currentItem: TaskModel?, currentItemPosition: Int?)
        fun onDialogNegativeClick(dialog: DialogFragment, isEdit: Boolean, currentItem: TaskModel?, currentItemPosition: Int?)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            // Get the layout inflater
            val inflater = requireActivity().layoutInflater

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout

            val view = inflater.inflate(R.layout.fragment_dialog_add_item, null)
            if (currentItem != null) {
                // check whether currentItem can access data
                view.findViewById<EditText>(R.id.edit_text_task_desc)
                    .setText(currentItem.data?.desc)
            }

            view.findViewById<Button>(R.id.button_set_deadline).setOnClickListener { pickDate() }
            textViewDateTime = view.findViewById(R.id.text_view_datetime)

            builder.setView(view)
                // Add action buttons
                .setPositiveButton(R.string.save) { _, _ ->
                    listener.onDialogPositiveClick(this, isEdit, currentItem, currentItemPosition)
                }
                .setNegativeButton(R.string.cancel) { _, _ ->
                    listener.onDialogNegativeClick(this, isEdit, currentItem, currentItemPosition)
                }
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            listener = context as DialogAddItemListener
        } catch (e: ClassCastException) {
            // The activity doesn't implement the interface, throw exception
            throw ClassCastException((context.toString() +
                    " must implement DialogAddItemListener"))
        }
    }

    private fun getDateTimeCalendar() {
        val cal = Calendar.getInstance()
        day = cal.get(Calendar.DAY_OF_MONTH)
        month = cal.get(Calendar.MONTH)
        year = cal.get(Calendar.YEAR)
        hour = cal.get(Calendar.HOUR)
        minute = cal.get(Calendar.MINUTE)
    }

    private fun pickDate() {
        getDateTimeCalendar()
        DatePickerDialog(mainBinding.root.context, this, year, month, day).show()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        savedDay = dayOfMonth
        savedMonth = month + 1
        savedYear = year

        getDateTimeCalendar()

        TimePickerDialog(mainBinding.root.context, this, hour, minute, true).show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        savedHour = hourOfDay
        savedMinute = minute

        val dateTimeString = "$savedDay/$savedMonth/$savedYear - ${savedHour}:$savedMinute"
        val parsedDate = sdf.parse(dateTimeString)

        textViewDateTime.text = sdf.format(parsedDate.time)
    }
}