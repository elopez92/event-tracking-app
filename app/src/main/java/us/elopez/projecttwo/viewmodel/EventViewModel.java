package us.elopez.projecttwo.viewmodel;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import us.elopez.projecttwo.AppDatabase;
import us.elopez.projecttwo.EventDao;
import us.elopez.projecttwo.data.model.EventEntity;

/**
 * ViewModel for managing event-related operations.
 */
public class EventViewModel extends ViewModel {

    public enum SortType {
        BY_DATE, BY_NAME
    }

    final private String TAG = "EventViewModel";
    private final EventDao eventDao;
    private final MutableLiveData<String> currentUsername = new MutableLiveData<>();
    private final LiveData<List<EventEntity>> events;
    private final MutableLiveData<String> filterQuery = new MutableLiveData<>("");
    private final MutableLiveData<SortType> sortType = new MutableLiveData<>(SortType.BY_DATE);

    // Cached to store fetched events
    private List<EventEntity> cachedEvents = null;

    /**
     * Constructor that initializes the ViewModel with event data.
     *
     * @param eventDao  The eventDao.
     * @param username  The currently logged-in user's username.
     */
    public EventViewModel(EventDao eventDao, String username) {
        this.eventDao = eventDao;

        // Combine filtering and sorting with LiveData transformations
        events = Transformations.switchMap(currentUsername, user ->
                Transformations.map(eventDao.getEventsForUser(user), eventList -> {
                    eventList = filterEvents(eventList, filterQuery.getValue());
                    return sortEvents(eventList, sortType.getValue());
                })
        );

        currentUsername.setValue(username);
    }

    /**
     * Returns a LiveData list of events for the user.
     */
    public LiveData<List<EventEntity>> getEvents() {
        return events;
    }

    /**
     * Inserts an event into the database asynchronously.
     */
    public void insertEvent(EventEntity event) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                eventDao.insertEvent(event);
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "Error inserting event", e);
            }
        });
    }

    /**
     * Deletes an event from the database asynchronously.
     */
    public void deleteEvent(EventEntity event) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try{
                eventDao.deleteEvent(event);
            } catch (Exception e){
                e.printStackTrace();
                Log.e(TAG, "Error deleting event", e);
            }
        });
    }

    public void insertEvents(List<EventEntity> events) {
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.insertEvents(events));
    }

    /**
     * Updates the current username to trigger data refresh if needed.
     */
    public void setUsername(String username) {
        currentUsername.postValue(username);
    }

    public void setFilterQuery(String query) {
        filterQuery.setValue(query);
        refreshEvents();
    }

    public void setSortType(SortType type) {
        sortType.setValue(type);
        refreshEvents();
    }

    private void refreshEvents() {
        if (currentUsername.getValue() != null) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                List<EventEntity> events = eventDao.getEventsDirectlyForUser(currentUsername.getValue());

                if (events != null) {
                    // Apply filtering
                    String query = filterQuery.getValue();
                    if (query != null && !query.isEmpty()) {
                        events = filterEvents(events, query);
                    }

                    // Apply sorting based on selected sort type
                    if (sortType.getValue() == SortType.BY_NAME) {
                        events.sort(Comparator.comparing(event -> event.event_name.toLowerCase()));
                    } else {
                        events.sort(Comparator.comparing(event -> event.event_datetime));
                    }

                    // Update LiveData on the main thread
                    List<EventEntity> finalEvents = events;
                    new Handler(Looper.getMainLooper()).post(() -> {
                        ((MutableLiveData<List<EventEntity>>) this.events).setValue(finalEvents);
                    });
                }
            });
        }
    }

    public LiveData<EventEntity> getEventById(int eventId) {
        return eventDao.getEventById(eventId);
    }

    public void updateEvent(EventEntity event){
        AppDatabase.databaseWriteExecutor.execute(() -> eventDao.updateEvent(event));
    }

    private List<EventEntity> filterEvents(List<EventEntity> events, String query) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return events.stream()
                    .filter(event -> event.event_name.toLowerCase().contains(query.toLowerCase()))
                    .toList();
        } else{
            List<EventEntity> filteredEvents = new ArrayList<>();
            for (EventEntity event : events) {
                if (event.event_name.toLowerCase().contains(query.toLowerCase())) {
                    filteredEvents.add(event);
                }
            }
            return filteredEvents;
        }
    }

    private List<EventEntity> sortEvents(List<EventEntity> events, SortType type) {
        if (type == SortType.BY_NAME) {
            events.sort(Comparator.comparing(event -> event.event_name.toLowerCase()));
        } else {
            events.sort(Comparator.comparing(event -> event.event_datetime)); // Sort by date
        }
        return events;
    }

    /**
     * Factory class for ViewModel instantiation.
     */
    public static class Factory implements ViewModelProvider.Factory {
        private final EventDao eventDao;
        private final String username;

        public Factory(EventDao dao, String user) {
            this.eventDao = dao;
            this.username = user;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new EventViewModel(eventDao, username);
        }
    }
}
