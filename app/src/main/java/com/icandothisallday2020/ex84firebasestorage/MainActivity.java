package com.icandothisallday2020.ex84firebasestorage;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.SimpleFormatter;

public class MainActivity extends AppCompatActivity {
    ImageView iv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        iv=findViewById(R.id.iv);

        String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(permissions[0])== PackageManager.PERMISSION_DENIED)
                requestPermissions(permissions,100);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100 && grantResults[0]==PackageManager.PERMISSION_DENIED)
            Toast.makeText(this, "IMPOSSIBLE UPLOAD", Toast.LENGTH_SHORT).show();
    }

    public void clickLoad(View view) {

        //fireStorage 에 있는 이미지 보여주기
        //저장된 이미지의 URL 얻어와 이미지뷰에 보여주기

        //FirebaseStorage 관리객체 소환
        FirebaseStorage firebaseStorage=FirebaseStorage.getInstance();
        //최상위폴더 참조객체 얻어오기
        StorageReference root=firebaseStorage.getReference();
        //읽어오길 원하는 파일의 참조객체 얻어오기
        StorageReference imgRef=root.child("photos/mango.png");

        //이 파일 참조객체로부터 이미지의 URL 얻어오기
        //액세스 토큰
        //5e9e2f6f-71b6-4af6-b378-ca64bb31bef4  <---클릭시 얻어지는 URL
        if(imgRef!=null){
            //참조객체로부터 URL을 얻어오는 작업이 성공되었다는 리스너 실행
            imgRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Glide.with(MainActivity.this).load(uri).into(iv);
                }
            });
        }


    }

    public void clickSelect(View view) {
        Intent intent=new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent,10);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==10 && resultCode==RESULT_OK){
            Uri uri=data.getData();
            Glide.with(this).load(uri).into(iv);

            imgUri=uri;
        }
    }

    //field(멤버변수)
    Uri imgUri;

    public void clickUpload(View view) {
        //FirebaseStorage 관리자 소환
        FirebaseStorage storage=FirebaseStorage.getInstance();

        //업로드해서 저장될, 날짜를 이용한 겹치지 않는 파일명 지정
        SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddhhmmss");
        String fileName=sdf.format(new Date())+".png";

        //업로드할 파일의 참조객체 생성
        StorageReference imgRef=storage.getReference("uploads/"+fileName);//rootRef 거치지 않고->바로 변환
        //파일 Data 는 없고 파일명만 만들어진 상태
        //위 위치의 참조객체에 이미지파일데이터 보내기
        imgRef.putFile(imgUri);

        //업로드의 성공결과를 알고 싶다면 [ Task : 이미 만들어져 있는 별도 Thread ](Runnable interface 를 구현하여 만들어진 객체)
        UploadTask task =imgRef.putFile(imgUri);
        task.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(MainActivity.this, "UPLOAD SUCCESS", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
