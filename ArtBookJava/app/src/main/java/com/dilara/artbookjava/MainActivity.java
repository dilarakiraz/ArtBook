package com.dilara.artbookjava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.dilara.artbookjava.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    ArrayList<Art> artArrayList;
    ArtAdapter artAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);//oluşturduğun görünümü buraya verilir

        artArrayList=new ArrayList<>();
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this)); //alt alta gözükür
        artAdapter =new ArtAdapter(artArrayList);
        binding.recyclerView.setAdapter(artAdapter);
        getData();

    }

    private void getData(){
        try{
            SQLiteDatabase sqLiteDatabase =this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            Cursor cursor=sqLiteDatabase.rawQuery("SELECT * FROM arts", null);
            int nameIx=cursor.getColumnIndex("artname");
            int idIx=cursor.getColumnIndex("id");

            while(cursor.moveToNext()) {//imleç hareket ettiği sürece kaydet
                String name=cursor.getString(nameIx);
                int id=cursor.getInt(idIx);
                Art art=new Art(name,id);
                artArrayList.add(art);

            }
            artAdapter.notifyDataSetChanged();//veri seti değşti haberin olsun demek

            cursor.close();

            } catch(Exception e) {
            e.printStackTrace();

        }
        }


    @Override
   public boolean onCreateOptionsMenu(Menu menu){//menüyü bağlamak için gerekli.options menü oluşturulduğunda ne olacak
        MenuInflater menuInflater=getMenuInflater();
        menuInflater.inflate(R.menu.art_menu,menu);//2 şey sorar: hangi menüyü bağlamak istediğin,hangi menüyle bağlanacağın


        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {//menüye tıklandığında ne olacağını söyler
        if(item.getItemId()==R.id.add_art){//bu seçildiyse ne olacağı
            Intent intent =new Intent(this,ArtActivity.class);//bağlanmak için intent
            intent.putExtra("info","new");
            startActivity(intent);
        }


        return super.onOptionsItemSelected(item);
    }
}