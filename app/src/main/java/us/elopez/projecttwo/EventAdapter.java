package us.elopez.projecttwo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import us.elopez.projecttwo.data.model.EventEntity;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<EventEntity> eventList = new ArrayList<>();
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        onItemClickListener = listener;
    }

    public EventAdapter() {
    }

    public void setEvents(List<EventEntity> events){
        if(eventList == null)
            return;
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                if(eventList == null)
                    return 0;
                return eventList.size();
            }

            @Override
            public int getNewListSize() {
                return events.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return eventList.get(oldItemPosition).getId() == events.get(newItemPosition).getId();
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                return eventList.get(oldItemPosition).equals(events.get(newItemPosition));
            }
        });

        eventList.clear();
        eventList.addAll(events);
        diffResult.dispatchUpdatesTo(this);
    }

    public EventEntity getEventAt(int position){
        return eventList.get(position);
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(itemView, onItemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventEntity currentEvent = eventList.get(position);
        holder.eventNameTextView.setText(currentEvent.getName());
        holder.eventDateTimeTextView.setText(currentEvent.getDatetime());
    }

    public void removeEventAt(int position) {
        eventList.remove(position);
        notifyItemRemoved(position);
    }

    public void addEvent(EventEntity event){
        eventList.add(event);
        notifyItemInserted(eventList.size() - 1);
    }

    @Override
    public int getItemCount() {
        if(eventList == null)
            return 0;

        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        public TextView eventNameTextView;
        public TextView eventDateTimeTextView;
        public Button deleteButton;

        public EventViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            eventNameTextView = itemView.findViewById(R.id.eventName);
            eventDateTimeTextView = itemView.findViewById(R.id.eventDate);

        }
    }
}
