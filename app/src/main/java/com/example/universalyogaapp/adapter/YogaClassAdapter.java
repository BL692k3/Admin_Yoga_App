package com.example.universalyogaapp.adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.widget.CheckBox;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.activity.MainActivity;
import com.example.universalyogaapp.model.YogaClass;

import java.util.ArrayList;
import java.util.List;

public class YogaClassAdapter extends RecyclerView.Adapter<YogaClassAdapter.YogaClassViewHolder> {

    private final Context context;
    private List<YogaClass> yogaClassList;
    private OnItemClickListener onItemClickListener;
    private boolean isSelectionMode = false;
    private final List<YogaClass> selectedClasses = new ArrayList<>();

    public YogaClassAdapter(Context context, List<YogaClass> yogaClassList) {
        this.context = context;
        this.yogaClassList = yogaClassList;
    }

    // Update data for the RecyclerView
    public void updateData(List<YogaClass> newYogaClasses) {
        this.yogaClassList = newYogaClasses;
        notifyDataSetChanged();
    }

    // Interface for item click events
    public interface OnItemClickListener {
        void onItemClick(YogaClass yogaClass);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    @NonNull
    @Override
    public YogaClassViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yoga_class, parent, false);
        return new YogaClassViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YogaClassViewHolder holder, int position) {
        YogaClass yogaClass = yogaClassList.get(position);
        holder.tvDay.setText(yogaClass.getDay());
        holder.tvTime.setText(yogaClass.getTime());
        holder.tvClassType.setText("Class type: " + yogaClass.getClassType());
        holder.tvCapacity.setText("Capacity: " + yogaClass.getCapacity());
        holder.tvDuration.setText("Duration: " + yogaClass.getDuration() + " minutes");
        holder.tvPrice.setText("Price: " + yogaClass.getPrice() + "$");
        holder.tvDescription.setText("Description: " + yogaClass.getDescription());
        // Set item click listener
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(yogaClass);
            }
        });
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                ((MainActivity) context).startSelectionMode();
            }
            toggleSelection(yogaClass);
            notifyDataSetChanged();
            return true;
        });

        // Checkbox visibility based on selection mode
        holder.checkBoxSelect.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
        holder.checkBoxSelect.setChecked(selectedClasses.contains(yogaClass));

        // Click on checkbox to select/deselect
        holder.checkBoxSelect.setOnClickListener(v -> toggleSelection(yogaClass));
    }

    public void toggleSelection(YogaClass yogaClass) {
        if (selectedClasses.contains(yogaClass)) {
            selectedClasses.remove(yogaClass);
        } else {
            selectedClasses.add(yogaClass);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedClasses.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public List<YogaClass> getSelectedClasses() {
        return selectedClasses;
    }

    public boolean isSelectionMode() {
        return isSelectionMode;
    }

    public void selectAll() {
        selectedClasses.clear();
        selectedClasses.addAll(yogaClassList);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return yogaClassList.size();
    }

    // ViewHolder class
    public static class YogaClassViewHolder extends RecyclerView.ViewHolder {

        TextView tvDay, tvTime, tvClassType, tvCapacity, tvDuration, tvPrice, tvDescription;
        CheckBox checkBoxSelect;

        public YogaClassViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay = itemView.findViewById(R.id.tvDay);
            tvTime = itemView.findViewById(R.id.tvTime);
            tvClassType = itemView.findViewById(R.id.tvClassType);
            tvCapacity = itemView.findViewById(R.id.tvCapacity);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            checkBoxSelect = itemView.findViewById(R.id.checkBoxSelect);
        }
    }
}
