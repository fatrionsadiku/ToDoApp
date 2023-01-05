package com.todolist.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.todolist.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 1)
abstract class TasksDatabase : RoomDatabase(){

    abstract fun taskDao() : TaskDao


    class CallBack@Inject constructor(
        private val database : Provider<TasksDatabase>,
        @ApplicationScope
        private val applicationScope: CoroutineScope
    ) : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Wash the dishes"))
                dao.insert(Task("66661"))
                dao.insert(Task("Watch Anime",important = true))
                dao.insert(Task("Watch Tv", completed = true))
                dao.insert(Task("Hello",completed = true))
                dao.insert(Task("123"))
                dao.insert(Task("56"))
                dao.insert(Task("441"))
            }


        }
    }

}