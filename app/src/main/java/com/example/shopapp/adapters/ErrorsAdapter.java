package com.example.shopapp.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.shopapp.R;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.rxjava3.subjects.PublishSubject;

public class ErrorsAdapter extends RecyclerView.Adapter<ErrorsAdapter.ViewHolder>{
    private final List<String> errors = new ArrayList<>();
    private final LayoutInflater layoutInflater;

    public ErrorsAdapter(Context context, List<String> errors, PublishSubject<String> publishSubject){
        this.layoutInflater = LayoutInflater.from(context);
        this.errors.addAll(errors);

        publishSubject.subscribe(v -> {
            int lastIndex = this.errors.size();
            this.errors.add(lastIndex, v);
            this.notifyItemChanged(lastIndex);
        });
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = this.layoutInflater.inflate(R.layout.error, parent);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        String error = this.errors.get(position);
        holder.textView.setText(error);
    }

    @Override
    public int getItemCount() {
        return this.errors.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{
        private TextView textView;

        public ViewHolder(@NonNull @NotNull View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.error_txt);
        }
    }
}
