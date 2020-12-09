package com.example.todolist;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewHolder extends RecyclerView.ViewHolder {
    View mview;
    public RecyclerViewHolder(@NonNull View itemView) {
        super(itemView);
        mview = itemView;

    }

    public void setTask(String task){
        TextView taskTextView = mview.findViewById(R.id.taskTv);
        taskTextView.setText(task);
    }

    public void setDescription(String desc){
        TextView descTextView = mview.findViewById(R.id.descriptionTv);
        descTextView.setText(desc);
    }

    public void setDate(String date){
        TextView dateTextView = mview.findViewById(R.id.dateTv);
        dateTextView.setText(date);
    }
}
