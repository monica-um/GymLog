package com.mum.gymlog.Database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.mum.gymlog.Database.entities.GymLog;

import java.util.ArrayList;

@Dao
public interface GymLogDAO {
    // Actions that can be performed on database

    // Add records to interface
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(GymLog gymlog);

    @Query("Select * from " + GymLogDatabase.GYM_LOG_TABLE)
    ArrayList<GymLog> getAllRecords();
}
