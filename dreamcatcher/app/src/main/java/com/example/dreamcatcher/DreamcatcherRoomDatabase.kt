package com.example.dreamcatcher
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase


@Database(entities = [User::class, Dream::class], version = 3)
abstract class DreamcatcherRoomDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun dreamDao(): DreamDao

    companion object {
        private var INSTANCE: DreamcatcherRoomDatabase? = null

        fun getInstance(context: Context): DreamcatcherRoomDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        DreamcatcherRoomDatabase::class.java,
                        "dreamcatcher_database"
                    ).fallbackToDestructiveMigration()
                        .build()

                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}
