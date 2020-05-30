package gleb.kalinin.myreminder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import gleb.kalinin.myreminder.model.dataBase.DBHandler
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_ID
import gleb.kalinin.myreminder.model.dataBase.INTENT_TODO_NAME
import gleb.kalinin.myreminder.model.dto.ToDo
import kotlinx.android.synthetic.main.activity_dashboard.*

class DashboardActivity : AppCompatActivity() {

    lateinit var dbHandler: DBHandler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        // Toolbar
        setSupportActionBar(dashboard_toolbar)
        title = "DashBoard"
        dbHandler = DBHandler(this)
        rv_dashboard.layoutManager = LinearLayoutManager(this)

        fab_dashboard.setOnClickListener {
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Добавление новой заметки")
            val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
            val todoName = view.findViewById<EditText>(R.id.ev_todo)
            dialog.setView(view)
            dialog.setPositiveButton("Добавить") { _: DialogInterface, _: Int ->
                if(todoName.text.isNotEmpty()){
                    val toDo = ToDo()
                    toDo.name = todoName.text.toString()
                    // Adding new task
                    dbHandler.addToDo(toDo)
                    // Refreshing
                    refreshList()
                }
            }
            dialog.setNegativeButton("Отменить") { _: DialogInterface, _: Int ->
                // negative button
            }
            dialog.show()
        }
    }

    fun updateToDo(toDo: ToDo){
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Обновление заметки")
        val view = layoutInflater.inflate(R.layout.dialog_dashboard, null)
        val todoName = view.findViewById<EditText>(R.id.ev_todo)
        todoName.setText(toDo.name)
        dialog.setView(view)
        dialog.setPositiveButton("Обновить") { _: DialogInterface, _: Int ->
            if(todoName.text.isNotEmpty()){
                toDo.name = todoName.text.toString()
                dbHandler.updateToDo(toDo)
                refreshList()
            }
        }
        dialog.setNegativeButton("Отменить") { _: DialogInterface, _: Int ->
            // negative button
        }
        dialog.show()
    }


    override fun onResume() {
        refreshList()
        super.onResume()
    }

    private fun refreshList () {
        rv_dashboard.adapter = DashboardAdapter(this, dbHandler.getToDos())
    }

    class DashboardAdapter(val activity: DashboardActivity, val list: MutableList<ToDo>) : RecyclerView.Adapter<DashboardAdapter.ViewHolder> () {

        override fun onCreateViewHolder(p0: ViewGroup, p1: Int): ViewHolder {
            return ViewHolder(LayoutInflater.from(activity).inflate(R.layout.rv_child_dashboard, p0, false))
        }

        override fun getItemCount(): Int {
            return list.size
        }

        override fun onBindViewHolder(holder: ViewHolder, p1: Int) {
            // значению toDoName -> даём 1-ое значение из базы данных
            holder.toDoName.text = list[p1].name

            holder.toDoName.setOnClickListener {
                val intent = Intent(activity, ItemActivity::class.java)
                intent.putExtra(INTENT_TODO_ID,list[p1].id)
                intent.putExtra(INTENT_TODO_NAME,list[p1].name)
                activity.startActivity(intent)
            }

            holder.menu.setOnClickListener {
                // При нажатии на 3 точки (IV), открывается меню
                val popup = PopupMenu(activity, holder.menu)
                popup.inflate(R.menu.dashboard_child)
                popup.setOnMenuItemClickListener {

                    when(it.itemId){
                        R.id.menu_edit -> {
                            activity.updateToDo(list[p1])
                        }
                        R.id.menu_delete -> {
                            activity.dbHandler.deleteToDo(list[p1].id)
                            activity.refreshList()
                        }
                        R.id.menu_mark_as_completed -> {
                            activity.dbHandler.updateToDoItemCompletedStatus(list[p1].id, true)
                        }
                        R.id.menu_reset -> {
                            activity.dbHandler.updateToDoItemCompletedStatus(list[p1].id, false)
                        }
                    }

                    true
                }
                popup.show()
            }
        }

        class ViewHolder(v : View): RecyclerView.ViewHolder(v) {
            val toDoName: TextView = v.findViewById(R.id.tv_todo_name)
            val menu : ImageView = v.findViewById(R.id.iv_menu)
        }
    }
}
