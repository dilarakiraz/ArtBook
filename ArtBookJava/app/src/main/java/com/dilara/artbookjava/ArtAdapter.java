package com.dilara.artbookjava;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dilara.artbookjava.databinding.RecyclerRowBinding;

import java.util.ArrayList;


public class ArtAdapter extends RecyclerView.Adapter<ArtAdapter.ArtHolder> {

        ArrayList<Art> artArrayList;

        public ArtAdapter (ArrayList<Art> artArrayList){
            this.artArrayList=artArrayList;
        }


    @Override
    public ArtHolder onCreateViewHolder(@NonNull  ViewGroup parent, int viewType) {

            RecyclerRowBinding recyclerRowBinding=RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false) ;//layout infaleti kullanabiliriz
            return new ArtHolder(recyclerRowBinding);

    }

    @Override
    public void onBindViewHolder(@NonNull  ArtAdapter.ArtHolder holder, int position) {
            holder.binding.recyclerViewTextView.setText(artArrayList.get(position).name);//recyclerview texti görür
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent=new Intent(holder.itemView.getContext(),ArtActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("artId",artArrayList.get(position).id);
                holder.itemView.getContext().startActivity(intent);//aktiviteyi başlatabiliriz
            }
        });
    }

    @Override
    public int getItemCount() {
        return artArrayList.size();
    }//herhangi bir görünüm tutucu sınıf istiyor.her şeyin bağlanmasına yardımcı



    public class ArtHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;//görünüme eşitledi


        public ArtHolder(RecyclerRowBinding binding){
            super(binding.getRoot());//görünüm alındı
            this.binding=binding;
        }
    }



}
