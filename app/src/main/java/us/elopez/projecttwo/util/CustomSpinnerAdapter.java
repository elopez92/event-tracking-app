package us.elopez.projecttwo.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import us.elopez.projecttwo.R;

public class CustomSpinnerAdapter extends BaseAdapter {

    private final Context context;
    private final String[] items;
    private final int dropdownIcon = R.drawable.ic_arrow_drop_down; // Your arrow icon

    public CustomSpinnerAdapter(Context context, String[] items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // View for the spinner (collapsed view)
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        TextView spinnerText = convertView.findViewById(R.id.spinnerText);
        ImageView spinnerArrow = convertView.findViewById(R.id.spinnerArrow);

        spinnerText.setText(items[position]);
        spinnerArrow.setVisibility(View.VISIBLE); // Show arrow only here

        return convertView;
    }

    // View for the dropdown items
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }

        TextView spinnerText = convertView.findViewById(R.id.spinnerText);
        ImageView spinnerArrow = convertView.findViewById(R.id.spinnerArrow);

        spinnerText.setText(items[position]);
        spinnerArrow.setVisibility(View.GONE); // Hide arrow in dropdown

        return convertView;
    }
}
