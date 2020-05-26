package gleb.kalinin.myreminder.model.dataBase

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import gleb.kalinin.myreminder.model.dataBase.*
import gleb.kalinin.myreminder.model.dto.ToDo
import gleb.kalinin.myreminder.model.dto.ToDoItem

class DBHandler (val context: Context) : SQLiteOpenHelper (context,
    DB_NAME, null,
    DB_VERSION
) {
    override fun onCreate(db: SQLiteDatabase) {
        val createToDoTable = "CREATE TABLE ToDo (" +
                "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COL_CREATED_AT datetime DEFAULT CURRENT_TIMESTAMP," +
                "$COL_NAME varchar);"
        val createToDoItemTable =
                "CREATE TABLE ToDoListItem (" +
                "$COL_ID integer PRIMARY KEY AUTOINCREMENT," +
                "$COL_CREATED_AT datetime DEFAULT CURRENT_TIMESTAMP," +
                "$COL_TO_DO_ID integer," +
                "$COL_ITEM_NAME varchar," +
                "$COL_IS_COMPLETED integer);"
        // execute DB
        db.execSQL(createToDoTable)
        db.execSQL(createToDoItemTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }


    fun addToDo (toDo: ToDo) : Boolean {
        val db = writableDatabase
        // cv = ContentValues
        val cv = ContentValues()
        cv.put(COL_NAME, toDo.name)
        val result = db.insert(TABLE_TODO, null, cv)
        // If result doesn't equals -1 => True. У нас в (dto.ToDo) -> Таблица SQL начинается с Id(long) -1.
        return result != (-1).toLong()
    }

    fun getToDos() : MutableList<ToDo> {
        // Storing toDo items
        val result: MutableList<ToDo> = ArrayList()
        // read the data
        val db = readableDatabase
        // getting all the records from TABLE_TODO
        val queryResult = db.rawQuery("SELECT * FROM $TABLE_TODO", null)
        // Если сработает -> У нас есть какие-то данные в таблице.
        if(queryResult.moveToFirst()) {
            do{
                // Storing the data inside todo
                val todo = ToDo()
                todo.id = queryResult.getLong(queryResult.getColumnIndex(COL_ID))
                todo.name = queryResult.getString(queryResult.getColumnIndex(COL_NAME))
                result.add(todo)

            } while (queryResult.moveToNext())
        }
        queryResult.close()
        return result
    }
}