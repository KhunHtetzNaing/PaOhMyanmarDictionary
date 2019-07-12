package com.htetznaing.paohmyanmardictionary.Adapter;

import android.app.AlertDialog;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.amulyakhare.textdrawable.TextDrawable;
import com.htetznaing.paohmyanmardictionary.Constants;
import com.htetznaing.paohmyanmardictionary.DetailsActivity;
import com.htetznaing.paohmyanmardictionary.Model.Model;
import com.htetznaing.paohmyanmardictionary.R;

import java.util.ArrayList;

import static android.content.Context.CLIPBOARD_SERVICE;

public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {
    ArrayList<Model> data;
    Context context;
    public Adapter(ArrayList<Model> data) {
        this.data = data;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        TextView mm,paoh;
        ImageView icon;

        private ViewHolder (View v){
            super(v);
            mm = v.findViewById(R.id.mm);
            paoh = v.findViewById(R.id.paoh);
            icon = v.findViewById(R.id.icon);
        }
    }

    @NonNull
    @Override
    public Adapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, final int i) {
        context = viewGroup.getContext();
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.items, viewGroup, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull Adapter.ViewHolder viewHolder, final int i) {
        viewHolder.paoh.setText(data.get(i).getPaoh());
        viewHolder.mm.setText(data.get(i).getMm());
        viewHolder.icon.setImageDrawable(TextDrawable.builder().buildRound(data.get(i).getMm().substring(0, 1), Constants.getColor()));
        viewHolder.mm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(data.get(i));
            }
        });
        viewHolder.icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(data.get(i));
            }
        });
        viewHolder.paoh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDetail(data.get(i));
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    private void showDetail(Model model){
        final String message = "(မြန်မာ)\n"+model.getMm()+" \n\n"+
                "(ပအိုဝ်ႏ)\n"+model.getPaoh();
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_details,null);
        TextView text = view.findViewById(R.id.text);
        text.setText(message);
        AlertDialog.Builder builder = new AlertDialog.Builder(context)
                .setView(view)
                .setPositiveButton("Share", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String shareBody = message;
                        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Subject Here");
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                        context.startActivity(Intent.createChooser(sharingIntent, context.getResources().getString(R.string.app_name)));
                    }
                })
                .setNegativeButton("Copy", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ClipboardManager clipboardManager = (ClipboardManager) context.getSystemService(CLIPBOARD_SERVICE);
                        clipboardManager.setText(message);
                        if (clipboardManager.hasText()){
                            Toast.makeText(context, "Copied", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        builder.show();
    }
}
