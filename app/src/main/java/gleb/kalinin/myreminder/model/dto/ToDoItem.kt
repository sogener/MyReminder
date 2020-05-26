package gleb.kalinin.myreminder.model.dto

class ToDoItem {

    val id: Long = -1 // primary key
    var toDoId : Long =  -1 // parent task id
    var itemName = "" // sub task name
    var isCompleted : MutableList<ToDoItem> = ArrayList() // Giving the class name and assigning it with empty ArrayList
}