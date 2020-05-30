package gleb.kalinin.myreminder.activity

import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gleb.kalinin.myreminder.R
import gleb.kalinin.myreminder.model.dataBase.DBHandler
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_ID
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_NAME
import gleb.kalinin.myreminder.model.dto.ToDoItem
import kotlinx.android.synthetic.main.activity_item.*
import kotlinx.android.synthetic.main.rv_child_dashboard.*

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
            dialog.setTitle("Добавление мини-задачи")
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val todoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Добавить") { _: DialogInterface, _: Int ->
                if (todoName.text.isNotEmpty()) {
                    val item = ToDoItem()
                    item.itemName = todoName.text.toString()
                    item.toDoId = todoId
                    item.isCompleted = false
                    dbHandler.addToDoItem(item) // тут ошибка
                    refreshList() // или тут
                }
            }
            dialog.setNegativeButton("Отменить") {_: DialogInterface, _:Int ->

            }
            dialog.show()
        }
    }

    fun updateItem(item: ToDoItem){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Обновление мини-задачи")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val toDoName = view.findViewById<EditText>(R.id.ev_todo)
        toDoName.setText(item.itemName)
        dialog.setView(view)
        dialog.setPositiveButton("Обновить") { _: DialogInterface, _: Int ->
            if (toDoName.text.isNotEmpty()) {
                item.itemName = toDoName.text.toString()
                item.toDoId = todoId
                item.isCompleted = false
                dbHandler.updateToDoItem(item)
                refreshList()
            }
        }
        dialog.setNegativeButton("Отменить") {_: DialogInterface, _:Int ->

        }
        dialog.show()
    }

    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList() {
        rv_item.adapter = ItemAdapter(this, dbHandler.getToDoItems(todoId))
    }

    class ItemAdapter(val activity: ItemActivity, val list: MutableList<ToDoItem>) : RecyclerView.Adapter<ItemAdapter.ViewHolder> () {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(activity).inflate(
                    R.layout.rv_child_item,
                    p0,
                    false
                )
            )
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
                activity.dbHandler.updateToDoItem(list[p1])
            }

            holder.delete.setOnClickListener {
                val dialog = AlertDialog.Builder(activity)
                dialog.setTitle("Подтвердите действия")
                dialog.setMessage("Вы действительно хотите удалить это мини-задание?")
                dialog.setPositiveButton("Удалить") { _: DialogInterface, _: Int ->
                    activity.dbHandler.deleteToDoItem(list[p1].id)
                    activity.refreshList()
                }
                dialog.setNegativeButton("Отменить") { _: DialogInterface, _: Int ->

                }
                dialog.show()
            }

            holder.edit.setOnClickListener {
                activity.updateItem(list[p1])
            }
        }

        class ViewHolder(v : View): RecyclerView.ViewHolder(v) {
            val itemName: CheckBox = v.findViewById(R.id.cb_item)
            val edit : ImageView = v.findViewById(R.id.iv_edit)
            val delete : ImageView = v.findViewById(R.id.iv_delete)
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
