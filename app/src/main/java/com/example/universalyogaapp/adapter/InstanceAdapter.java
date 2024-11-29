package com.example.universalyogaapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.universalyogaapp.R;
import com.example.universalyogaapp.activity.YogaAddEditActivity;
import com.example.universalyogaapp.model.YogaClassInstance;

import java.util.ArrayList;
import java.util.List;

public class InstanceAdapter extends RecyclerView.Adapter<InstanceAdapter.InstanceViewHolder> {
    private final Context context;
    private List<YogaClassInstance> instanceList;
    private OnInstanceClickListener listener;
    private boolean isSelectionMode = false;
    private final List<YogaClassInstance> selectedInstances = new ArrayList<>();

    public InstanceAdapter(Context context, List<YogaClassInstance> instances, OnInstanceClickListener listener) {
        this.context = context;
        this.instanceList = instances;
        this.listener = listener;
    }

    public InstanceAdapter(Context context, List<YogaClassInstance> instances) {
        this.context = context;
        this.instanceList = instances;
    }

    public interface OnInstanceClickListener {
        void onInstanceClick(YogaClassInstance instance);
        void onInstanceLongClick(YogaClassInstance instance);  // Add long-click callback
    }

    // Update data for the RecyclerView
    public void updateData(List<YogaClassInstance> instanceList) {
        this.instanceList = instanceList;
        notifyDataSetChanged();
    }

    public void toggleSelection(YogaClassInstance instance) {
        if (selectedInstances.contains(instance)) {
            selectedInstances.remove(instance);
        } else {
            selectedInstances.add(instance);
        }
        notifyDataSetChanged();
    }

    public void selectAll() {
        selectedInstances.clear();
        selectedInstances.addAll(instanceList);
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedInstances.clear();
        isSelectionMode = false;
        notifyDataSetChanged();
    }

    public List<YogaClassInstance> getSelectedInstances() {
        return selectedInstances;
    }

    @Override
    public InstanceViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_yoga_instance, parent, false);
        return new InstanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InstanceViewHolder holder, int position) {
        YogaClassInstance instance = instanceList.get(position);
        holder.tvInstanceDate.setText(instance.getDate());
        holder.tvInstanceTeacher.setText(instance.getTeacher());
        holder.tvInstanceComments.setText(instance.getComments());

        // Set click listener to toggle selection mode or navigate to instance details
        holder.itemView.setOnClickListener(v -> {
            if (isSelectionMode) {
                toggleSelection(instance);  // Toggle selection for the instance
            } else {
                listener.onInstanceClick(instance);  // Navigate to instance details or edit
            }
        });

        // Set long-click listener to enable selection mode
        holder.itemView.setOnLongClickListener(v -> {
            if (!isSelectionMode) {
                isSelectionMode = true;
                listener.onInstanceLongClick(instance);  // Notify the listener of the long-click
                ((YogaAddEditActivity) context).startSelectionMode();
            }
            toggleSelection(instance);
            notifyDataSetChanged();
            return true;
        });

        // Show or hide the checkbox based on selection mode
        holder.cbSelected.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);  // Show checkbox when in selection mode
        holder.cbSelected.setChecked(selectedInstances.contains(instance));

        holder.cbSelected.setOnClickListener(v -> toggleSelection(instance));
    }

    @Override
    public int getItemCount() {
        return instanceList.size();
    }

    public class InstanceViewHolder extends RecyclerView.ViewHolder {
        TextView tvInstanceDate, tvInstanceTeacher, tvInstanceComments;
        CheckBox cbSelected;  // Checkbox to select an instance

        public InstanceViewHolder(@NonNull View itemView) {
            super(itemView);
            tvInstanceDate = itemView.findViewById(R.id.tvInstanceDate);
            tvInstanceTeacher = itemView.findViewById(R.id.tvInstanceTeacher);
            tvInstanceComments = itemView.findViewById(R.id.tvInstanceComments);
            cbSelected = itemView.findViewById(R.id.cbSelected);
        }
    }
}
