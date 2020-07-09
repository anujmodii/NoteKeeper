package com.example.notekeeper.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.notekeeper.Model.Notes;
import com.example.notekeeper.NoteViewer;
import com.example.notekeeper.Notepad;
import com.example.notekeeper.R;

import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {
    Context mContext;
    List<Notes> mNotes;
    Boolean layoutType;

    public NotesAdapter(Context mContext, List<Notes> mNotes,boolean layoutType) {
        this.mContext = mContext;
        this.mNotes = mNotes;
        this.layoutType=layoutType;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(layoutType) {
            view = LayoutInflater.from(mContext).inflate(R.layout.note_item, parent, false);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.note_item_grid, parent, false);
        }
        return new NotesAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Notes note = mNotes.get(position);
        holder.titleText.setText(note.getTitle());
        holder.date.setText(note.getDate());
        holder.importance.setText(note.getImportance());
        holder.priority.setText(note.getPriority());
        holder.itemView.setBackgroundColor(Color.parseColor(note.getColor()));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, NoteViewer.class);
                intent.addFlags(FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("noteID",note.getnoteid());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNotes.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder{

        public TextView titleText, date, importance,priority;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText=itemView.findViewById(R.id.titleTextNoteItem);
            date=itemView.findViewById(R.id.dateNoteItem);
            importance=itemView.findViewById(R.id.importanceLevelNoteItem);
            priority=itemView.findViewById(R.id.priorityLevelNoteItem);
        }
    }
}
