package com.dilara.artbookjava;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import com.dilara.artbookjava.databinding.ActivityArtBinding;
import com.dilara.artbookjava.databinding.ActivityMainBinding;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;

public class ArtActivity extends AppCompatActivity {
    private ActivityArtBinding binding;
    ActivityResultLauncher<Intent> activityResultLauncher;//görsel seçince izin verilince ne olacaği.aktiviteye gidip geri gelmek
    ActivityResultLauncher<String> permissionLauncher;//izni istemek için
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_art); silinir binding yapılırken
        binding = ActivityArtBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();//ne iş yapıcağımız burda belirtiliyor

        database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);//veri tabanı açıyoruz


        Intent intent =getIntent();
        String info=intent.getStringExtra("info");//ya old gelicek ya da new

        if(info.equals("new")){//yeni art
            binding.nameText.setText("");
            binding.artistText.setText("");//boş olduğundan emin oluyoruz
            binding.yearText.setText("");
            binding.button.setVisibility(View.VISIBLE);//görünür olsun
            binding.imageView.setImageResource(R.drawable.selectimage);

        }else{//yeni değilse kullanıcı id yolladı id yi çek

            int artId=intent.getIntExtra("artId",0);//default değer id bulamadıysa null gelmemesi için 0 deriz
            binding.button.setVisibility(View.INVISIBLE);//buton görünmez olsun

            try{//id çekme işlemi
                Cursor cursor=database.rawQuery("SELECT * FROM arts WHERE id=?" ,new String[]{String.valueOf(artId)});//artıd int olduğu için stringe çevirdik
                int artNameIx=cursor.getColumnIndex("artname");
                int painterNameIx=cursor.getColumnIndex("paintername");
                int yearIx=cursor.getColumnIndex("year"),
                int imageIx=cursor.getColumnIndex("image");

                while(cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(artNameIx));
                    binding.artistText.setText(cursor.getString(painterNameIx));
                    binding.yearText.setText(cursor.getString(yearIx));

                    byte[] bytes=cursor.getBlob(imageIx);
                    Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();


            }catch (Exception e){
                e.printStackTrace();

            }


        }
    }


    public void save(View view) {//kayıt
        String name=binding.nameText.getText().toString();
        String artistName=binding.artistText.getText().toString();
        String year=binding.yearText.getText().toString();
        Bitmap smallImage=makeSmallerImage(selectedImage,300);

        ByteArrayOutputStream outputStream=new ByteArrayOutputStream();//veri tabanına kayıt edebilmek için 1 ve 0 lara çevrilir
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray=outputStream.toByteArray();//byte dizisine çevrildi

        try{
            database=this.openOrCreateDatabase("Arts",MODE_PRIVATE,null);
            database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artname VARCHAR, paintername VARCHAR, year VARCHAR, image BLOB)");

            String sqlString="INSERT INTO arts(artname,paintername,year,image) VALUES(?, ?, ?, ?)";
            SQLiteStatement sqLiteStatement= database.compileStatement(sqlString); //sonradan bağlama işlemleri kolay olyor
            sqLiteStatement.bindString(1,name);//indexler 1 den başlıyor.1 i name e bağla
            sqLiteStatement.bindString(2,artistName);
            sqLiteStatement.bindString(3,year);
            sqLiteStatement.bindBlob(4,byteArray);
            sqLiteStatement.execute();

        }catch(Exception e){
            e.printStackTrace();

        }

        Intent intent=new Intent(ArtActivity.this,MainActivity.class);//kayıt olduktan sonra ana menüye dön
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//şu anda bulunduğum aktiviteler dahil her şeyi kapat
        startActivity(intent);
    }


    public Bitmap makeSmallerImage(Bitmap image,int maximumSize){//görsel küçültmek
        int width=image.getWidth();
        int height=image.getHeight();

        float bitmapRatio = (float)width / (float)height;

        if(bitmapRatio>1){//1 den büyükse yatay bir görsel
            width=maximumSize;
            height=(int)(width/bitmapRatio);
        }else{//dikey
            height=maximumSize;
            width=(int)(height*bitmapRatio);
        }

        return image.createScaledBitmap(image,width,height,true);//true filtre olsun demek

    }



    public void selectImage(View view) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){//bunu kontrol et demek
            //SOL TARAFTA readexternal izni var mı kontrol ediliyor,sağ tarafta cevabı package manager içinde veriliyor,
            // eşit değil diyerek izin verilmemiş mi kontrolü yapılıyor.
            //izin verilmemişse request permission isticez
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {//izin isteme mantığını kullanıcıya gösteriyimmi
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {//request permission
                        permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

                    }
                }).show();//ilk görünüm ister,neden izin istediği mesajı. herhangi bir butona tıklanana kadar mesajı gösterir
            } else {//request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);

            }

    }else{//izin verilmiş galeriye git
        Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//pick tutup almak gibi
            activityResultLauncher.launch(intentToGallery);


    }
}




private void registerLauncher(){  //activity result launcher işlemleri
        activityResultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if(result.getResultCode()==RESULT_OK) {
                    Intent intentFromResult=result.getData();//veriyi al
                    if(intentFromResult!=null){
                        Uri imageData=intentFromResult.getData();//url verir.seçilen görseli nerede olduğunu bilirz
                        //binding.imageView.setImageURI(imageData);
                        try{ //görseli bitmape çevirip veri tabanına kaydeder
                            if(Build.VERSION.SDK_INT >= 28){ //VERSİYON 28 ÜSTÜNDEYSE BUNU YAP
                            ImageDecoder.Source source=ImageDecoder.createSource(ArtActivity.this.getContentResolver(),imageData);
                            ImageDecoder.decodeBitmap(source);
                            binding.imageView.setImageBitmap(selectedImage);
                            }else{
                                selectedImage =MediaStore.Images.Media.getBitmap(ArtActivity.this.getContentResolver(),imageData);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                        } catch(Exception e){
                            e.printStackTrace();

                        }

                        }

                }
            }
        }); //yeni bir aktivite başlatıyorum ama bir sonuç için

    permissionLauncher=registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
    @Override
    public void onActivityResult(Boolean result) {//result true ise izin verildi

        if(result){//izin verildi
    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);//pick tutup almak gibi
    activityResultLauncher.launch(intentToGallery);

        }else{//izin verilmedi
    Toast.makeText(ArtActivity.this,"Permission needed",Toast.LENGTH_LONG).show();
}
    }
});//sonunda cevap alacağımız işlem
}

}
