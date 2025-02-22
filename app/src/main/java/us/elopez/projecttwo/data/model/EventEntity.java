package us.elopez.projecttwo.data.model;


import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "events",
        foreignKeys = @ForeignKey(
                entity = UserEntity.class,
                parentColumns = "username",
                childColumns = "username",
                onDelete = ForeignKey.CASCADE
        ),
        indices = {@Index(value = {"username"})}  // add index for faster queries
)
public class EventEntity {
    @PrimaryKey(autoGenerate = true)
    public int event_id;

    public String event_name;
    public String event_datetime;
    public String username; // reference users.username

    public EventEntity(String event_name, String event_datetime){
        this.event_name = event_name;
        this.event_datetime = event_datetime;
    }

    public EventEntity(){}

    public EventEntity(String eventName, String eventDateTime, String username) {
        event_name = eventName;
        event_datetime = eventDateTime;
        this.username = username;
    }

    public EventEntity(int eventId, String eventName, String eventDateTime, String username) {
        event_id = eventId;
        event_name = eventName;
        event_datetime = eventDateTime;
        this.username = username;
    }

    public int getId() {
        return event_id;
    }

    public String getName() {
        return event_name;
    }

    public String getDatetime() {
        return event_datetime;
    }
}
