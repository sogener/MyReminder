package gleb.kalinin.myreminder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gleb.kalinin.myreminder.model.dataBase.DBHandler
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_ID
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_NAME
import gleb.kalinin.myreminder.model.dto.ToDo
import gleb.kalinin.myreminder.model.dto.ToDoItem
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.activity_item.*

class ItemActivity : AppCompatActivity() {

    lateinit var dbHandler : DBHandler
    var todoId : Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item)
        // toolbar
        setSupportActionBar(item_toolbar)
        // toolbar back arrow
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.title = intent.getStringExtra(INTENT_TODO_NAME)
        todoId = intent.getLongExtra(INTENT_TODO_ID, -1)
        dbHandler = DBHandler(this)

        rv_item.layoutManager = LinearLayoutManager(this)

        fab_item.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val todoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Add") { _: DialogInterface, _: Int ->
                if (todoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = todoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item) // тут ошибка
                    refreshList() // или тут
                }
            }
            dialog.setNegativeButton("Cancel") {_: DialogInterface, _:Int ->

            }
            dialog.show()
        }
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList() {
        rv_item.adapter = ItemAdapter(this, dbHandler, dbHandler.getToDoItem(todoId))
    }

    class ItemAdapter(val context: Context, val dbHandler: DBHandler, val list: MutableList<ToDoItem>) : RecyclerView.Adapter<ItemAdapter.ViewHolder> () {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(context).inflate(R.layout.rv_child_item, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
            // значению toDoName -> даём 1-ое значение из базы данных
            holder.itemName.text = list[p1].itemName
            holder.itemName.isChecked = list[p1].isCompleted

            holder.itemName.setOnClickListener {
                // reverse is completed
                list[p1].isCompleted = !list[p1].isCompleted
                dbHandler.updateToDoItem(list[p1])
            }
        }

        class ViewHolder(v : View): RecyclerView.ViewHolder(v) {
            val itemName: CheckBox = v.findViewById(R.id.cb_item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        // if true - toolbar button was pressed
        return if (item?.itemId == android.R.id.home) {
            // clean activity
            finish()
            true
        } else
            super.onOptionsItemSelected(item)
    }
}
