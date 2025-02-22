package us.elopez.projecttwo;


import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

import us.elopez.projecttwo.data.model.EventEntity;

@Dao
public interface EventDao {

    /**
     * Inserts a new event into the database. Replaces on conflict.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvent(EventEntity event);

    /**
     * Retrieves all events for a specific user
     */
    @Query("SELECT * FROM events WHERE username = :username")
    LiveData<List<EventEntity>> getEventsForUser(String username);

    /**
     * Retrieves all events for a specific user
     */
    @Query("SELECT * FROM events WHERE username = :username")
    List<EventEntity> getEventsDirectlyForUser(String username);

    /**
     * Deletes an event from the database.
     */
    @Delete
    void deleteEvent(EventEntity event);

    /**
     * Batch insert for multiple events (if needed in the future).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertEvents(List<EventEntity> events);

    /**
     * Update an event
     */
    @Update
    void updateEvent(EventEntity event);

    @Query("SELECT * FROM events WHERE event_id = :eventId")
    LiveData<EventEntity> getEventById(int eventId);
}
