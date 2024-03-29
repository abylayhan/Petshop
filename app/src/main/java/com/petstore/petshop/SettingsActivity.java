package com.petstore.petshop;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.petstore.petshop.Prevalent.Prevalent;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.net.URI;
import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {

    private CircleImageView profileImageView;
    private EditText fullNameEditText, userPhoneEditText, addressEditText, passwordEditText;
    private TextView profileChangeTextBtn, closeTextBtn, saveTextButton;

    private Button securityQuestionBtn;


    private Uri imageUri;
    private String myUrl = "";
    private StorageTask uploadTask;
    private StorageReference storageProfilePictureRef;
    private String checker = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        storageProfilePictureRef = FirebaseStorage.getInstance().getReference().child("Profile Pics");
        profileImageView = (CircleImageView) findViewById(R.id.settings_profile_image);
        fullNameEditText = (EditText) findViewById(R.id.settings_full_name);
        userPhoneEditText = (EditText) findViewById(R.id.settings_phone_number);
        addressEditText = (EditText) findViewById(R.id.settings_addres);
        profileChangeTextBtn = (TextView)findViewById(R.id.profile_image_change_btn);
        closeTextBtn = (TextView)findViewById(R.id.close_settings);
        saveTextButton = (TextView)findViewById(R.id.update_account_settings);
        passwordEditText = (EditText) findViewById(R.id.settings_password);
        securityQuestionBtn = findViewById(R.id.security_questions_btn);


        userInfoDisplay(profileImageView,fullNameEditText,userPhoneEditText,addressEditText,passwordEditText);

            closeTextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });



        securityQuestionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(SettingsActivity.this, ResetPasswordActivity.class);
                intent.putExtra("check", "settings");
                startActivity(intent);
            }
        });

            saveTextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (checker.equals("clicked"))
                    {
                        userInfoSaved();
                    }
                    else
                        {
                            updateOnlyUserInfo();
                        }

                }

            });


            profileChangeTextBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v)
                {
                   checker = "clicked";
                    CropImage.activity(imageUri)
                            .setAspectRatio(1, 1)
                            .start(SettingsActivity.this);
                }

            });





    }

    private void updateOnlyUserInfo()
    {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", fullNameEditText.getText().toString());
        userMap.put("address", addressEditText.getText().toString());
        userMap.put("phoneOrder", userPhoneEditText.getText().toString());
        userMap.put("password", passwordEditText.getText().toString());

        ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

        startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
        Toast.makeText(SettingsActivity.this, "Обновление сохранены успешно", Toast.LENGTH_SHORT).show();
        finish();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data!=null)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            profileImageView.setImageURI(imageUri);

        }
        else{
            Toast.makeText(this, "Ошибка поробуйте еще раз", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SettingsActivity.this, SettingsActivity.class));
            finish();
        }
    }

    private void userInfoSaved()
    {
        if (TextUtils.isEmpty(fullNameEditText.getText().toString()))
        {
            Toast.makeText(this, "Недопустимое имя", Toast.LENGTH_SHORT).show();

        }
        else if (TextUtils.isEmpty(addressEditText.getText().toString()))
        {
            Toast.makeText(this, "Заполните адрес", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(userPhoneEditText.getText().toString()))
        {
            Toast.makeText(this, "Недопустимое номер телефона", Toast.LENGTH_SHORT).show();
        }
        else if (TextUtils.isEmpty(passwordEditText.getText().toString()))
        {
            Toast.makeText(this, "Недопустимый формат пароля", Toast.LENGTH_SHORT).show();
        } else if (checker.equals("clicked"))
        {
            uploadImage();
        }
    }

    private void uploadImage() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Изменить Профиль");
        progressDialog.setMessage("Подождите, пока мы обновляем ваш профиль");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        if(imageUri != null)
        {
             final StorageReference fileRef = storageProfilePictureRef.child(Prevalent.currentOnlineUser.getPhone() + ".jpg");
             uploadTask = fileRef.putFile(imageUri);

             uploadTask.continueWithTask(new Continuation() {
                 @Override
                 public Object then(@NonNull Task task) throws Exception
                 {
                     if(!task.isSuccessful())
                     {
                        throw task.getException();
                     }
                     return fileRef.getDownloadUrl();
                 }
             })
             .addOnCompleteListener(new OnCompleteListener<Uri>() {
                 @Override
                 public void onComplete(@NonNull Task<Uri> task)
                 {
                     if (task.isSuccessful())
                     {
                         Uri downloadUri = task.getResult();
                         myUrl = downloadUri.toString();

                         DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users");
                         HashMap<String, Object> userMap = new HashMap<>();
                         userMap.put("name", fullNameEditText.getText().toString());
                         userMap.put("address", addressEditText.getText().toString());
                         userMap.put("phoneOrder", userPhoneEditText.getText().toString());
                         userMap.put("password", passwordEditText.getText().toString());
                         userMap.put("image", myUrl);

                         ref.child(Prevalent.currentOnlineUser.getPhone()).updateChildren(userMap);

                         progressDialog.dismiss();

                         startActivity(new Intent(SettingsActivity.this, HomeActivity.class));
                         Toast.makeText(SettingsActivity.this, "Обновление сохранены успешно", Toast.LENGTH_SHORT).show();
                         finish();
                     } else
                         {
                             progressDialog.dismiss();
                         Toast.makeText(SettingsActivity.this, "Ошибка", Toast.LENGTH_SHORT).show();
                     }
                 }
             });





        }else {
            Toast.makeText(SettingsActivity.this, "ВЫ не выбрали фотографию", Toast.LENGTH_SHORT).show();
        }

    }

    private void userInfoDisplay(CircleImageView profileImageView, EditText fullNameEditText, EditText userPhoneEditText, EditText addressEditText, EditText passwordEditText)
    {
        DatabaseReference UserRef = FirebaseDatabase.getInstance().getReference().child("Users").child(Prevalent.currentOnlineUser.getPhone());

        UserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                 if(dataSnapshot.child("image").exists()){
                     String image = dataSnapshot.child("image").getValue().toString();
                     String name = dataSnapshot.child("name").getValue().toString();
                     String password = dataSnapshot.child("password").getValue().toString();
                     String phone = dataSnapshot.child("phone").getValue().toString();

                     String address = dataSnapshot.child("address").getValue().toString();

                     Picasso.get().load(image).into(profileImageView);
                     fullNameEditText.setText(name);
                     userPhoneEditText.setText(phone);
                     addressEditText.setText(address);
                     passwordEditText.setText(password);


                 }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error)
            {

            }
        });
    }
}