package com.example.socialmedia.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialmedia.ReplaceActivity;
import com.example.socialmedia.MainActivity;
import com.example.socialmedia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CreateAccountFragment extends Fragment {

    private EditText nameEt, emailEt;
    private TextInputEditText passwordEt, confirmPasswordEt;;
    private ProgressBar progressBar;
    private TextView loginTV;
    private AppCompatButton signUpBtn;
    private FirebaseAuth auth;
    public static final String EMAIL_REGEX = "^(.+)@(.+)$";

    public CreateAccountFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);
        clickListener();
    }
    private void init(@NonNull View view){
        nameEt= view.findViewById(R.id.nameET);
        emailEt= view.findViewById(R.id.emailET);
        passwordEt= view.findViewById(R.id.passwordET);
        confirmPasswordEt= view.findViewById(R.id.confirmPassET);
        loginTV= view.findViewById(R.id.loginTV);
        signUpBtn= view.findViewById(R.id.signUpBtn);
        progressBar= view.findViewById(R.id.progressBar);
        auth= FirebaseAuth.getInstance();
    }
    private void clickListener(){
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplaceActivity)getActivity()).setFragment(new LoginFragment());
            }
        });
        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name= nameEt.getText().toString();
                String email= emailEt.getText().toString();
                String password= passwordEt.getText().toString();
                String confirmPassword= confirmPasswordEt.getText().toString();
                if(name.isEmpty() || name.equals(" ")){
                    nameEt.setError("Please input valid name");
                    return;
                }
                if(email.isEmpty() || !email.matches(EMAIL_REGEX)){
                    emailEt.setError("Please input valid email");
                    return;
                }
                if(password.isEmpty() || password.length()<6){
                    passwordEt.setError("Please input valid password");
                    return;
                }
                if(!password.equals(confirmPassword)){
                    confirmPasswordEt.setError("Password not match");
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                createAccount(name,email,password);
            }
        });
    }
    private void createAccount(String name, String email, String password)
    {
        auth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            FirebaseUser user= auth.getCurrentUser();
                            UserProfileChangeRequest.Builder request= new UserProfileChangeRequest.Builder();
                            request.setDisplayName(name);
                            user.updateProfile(request.build());

                            user.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if(task.isSuccessful()){
                                                        Toast.makeText(getContext(),"Email verification link send,",Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                            uploadUser(user,name,email);
                        }else {
                            progressBar.setVisibility(View.GONE);
                            String exception = task.getException().getMessage();
                            Toast.makeText(getContext(),"Error: "+exception,Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void  uploadUser(FirebaseUser user,String name, String email){
        Map<String,Object> map= new HashMap<>();
        List<String> list = new ArrayList<>();
        List<String> list1= new ArrayList<>();
        map.put("uid",user.getUid());
        map.put("profileImageUrl","data:image/jpeg;base64,/9j/4AAQSkZJRgABAQAAAQABAAD/2wCEAAkGBwgHBgYJBwkGCAgHCBYGCAgICBsIFQYWIB0WFiARHx8kHDQsJCYlJx8TLTEhMTUrLi4uIx8zODMsNygtLisBCgoKBQUFDgUFDisZExkrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrKysrK//AABEIAMAAwAMBIgACEQEDEQH/xAAbAAEBAAMBAQEAAAAAAAAAAAAABAIFBgMHAf/EADIQAQACAQEEBwYGAwAAAAAAAAABAgMEBREhMRITIjJBUXFCYYGR0fAjM1JTweEUobH/xAAWAQEBAQAAAAAAAAAAAAAAAAAAAgH/xAAUEQEAAAAAAAAAAAAAAAAAAAAA/9oADAMBAAIRAxEAPwD7SA1IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAn1WqxaWm/JO+092lecms1FdLgted02nhSvnLncuS2a9r5Jm1rTvmZBXn2nny7+hMYq+EU5/P6JLXvfv2vafO1plgAzpe9J7Fr1nzraYV6faefFu6cxlp4xf6/VCA6XSarFqq78c7pjvUtzoocriyWw3rfHM1tWeEw6LRaiuqw1vHCY4Xr+iQUAAAAAAAAAAAAAAAA0G183W6q1Y7uH8OI9/j9+5CzyW6eS9p52vN5+LAAAAABdsjN1WqrWe5m/Dn1++HxQs8duhkpaOdLxeAdUAAAAAAAAAAAAAAADlclehkvWedbzSfgwX7Xw9VqrW9nN+JHr4/fvQAAAAAM8denkpWOd7xSGC7Y+HrdXW893D259fvj8Ab8AAAAAAAAAAAAAAAE+s01dVgtSeExxpb9DncuO2G9qZImtqzumJdUn1Wlxaqu7JG6Y7t686A5oXZ9mZ8e/oRGWnnT6fRJal6d+t6z5WrMAwGdKXvPYre0+VazKvT7Mz5d3TiMVfGb/AEBJix2zXrTHE2taeEQ6PRaaulw1pHGZ43t+uTS6XFpa7scb5nvXtzu9wAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAASazXYtNExPbyeFKzy9Z8AVp82t0+Hf08lZtHs07f8AxpdRr8+ffE2mlZ9inD5pAbfLtiOPU45nyte38f2lvtPVX5XrSPKlI/tEA9r6nPfvZc0+6by85tM85tPrLEBlEzHKZj0l6V1Oandy5o90Xnc8QFuPaeqpzvW8eV6QqxbZjh12OY87Ut/H9tQA6XBrdPm3dDJWLT7F+wockr02vz4N0Rab1j2L8fl5A6ISaPXYtTuiOxk8aWnn6SrAAAAAAAABr9p63/HiMeL820b5n9qGjmZmZmZmZmd8zM956ajJObPlvPGb3mY9PD/TyAAAAAAAAAAAAB+xMxMTG+Jid8TE8at5szW9fHV5fzaRvi37kNE9NNknDnxZI4TS8T8PL5A6kAAAAAAAHJAAAAAAAAAAAAAAAA60AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH/9k=");
        map.put("email",email);
        map.put("name",name);
        map.put("online",true);
        map.put("isAdmin",false);
        map.put("status","");

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener((task)-> {
                        if(task.isSuccessful()){
                            assert getActivity() != null;
                            progressBar.setVisibility(View.GONE);
                            startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
                            getActivity().finish();
                        }else {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(getContext(),"Error "+task.getException().getMessage()
                                    ,Toast.LENGTH_SHORT).show();
                        }
                });
    }
}