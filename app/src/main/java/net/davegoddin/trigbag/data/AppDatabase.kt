package net.davegoddin.trigbag.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import net.davegoddin.trigbag.model.TrigPoint
import net.davegoddin.trigbag.model.Visit
import java.io.File

@Database(entities = [TrigPoint::class, Visit::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trigPointDao() : TrigPointDao
    abstract fun visitDao() : VisitDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
//             dev code only - deletes existing DB
//            val databasesDir = File(context.dataDir.toString()+"/databases");
//            File(databasesDir, "trigpoint-db").delete();

//            return buildDatabase(context)
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "trigpoint-db")
                .createFromAsset("database/trigpoint-db.db")
                .build()


        }


    }



}