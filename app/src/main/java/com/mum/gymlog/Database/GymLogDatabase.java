package com.mum.gymlog.Database;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.mum.gymlog.Database.entities.GymLog;
import com.mum.gymlog.MainActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// must be false
@Database(entities = {GymLog.class}, version = 1, exportSchema = false)
public abstract class GymLogDatabase extends RoomDatabase {
    private static final String DATABASE_NAME = "GymLog_ database";

    public static final String GYM_LOG_TABLE = "gymLogTable";

    private static volatile GymLogDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;

    // runs child processes in the background
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    // static method that returns an instance of database
    static GymLogDatabase getDatabase (final Context context) {
        if (INSTANCE == null) {
            synchronized (GymLogDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            GymLogDatabase.class, DATABASE_NAME)
                            // app will crash without this line
                            .fallbackToDestructiveMigration()
                            .addCallback(addDefaultValues)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
    private static final RoomDatabase.Callback addDefaultValues = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);
            Log.i(MainActivity.TAG, "DATABASE CREATED!");
            // TODO: add databaseWriteExecutor.execute() -> {...}
        }
    };

    // Room handles code creation for us here
    public abstract GymLogDAO gymLogDAO();
}

