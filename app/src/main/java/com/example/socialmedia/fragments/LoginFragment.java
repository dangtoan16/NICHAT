package com.example.socialmedia.fragments;

import static com.example.socialmedia.fragments.CreateAccountFragment.EMAIL_REGEX;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.socialmedia.MainActivity;
import com.example.socialmedia.R;
import com.example.socialmedia.ReplaceActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginFragment extends Fragment {


    private EditText emailEt;
    private TextInputEditText passwordEt;
    private TextView signUpTv, forgotPasswordTv;
    private Button loginBtn, googleSignInBtn;
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 1;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth auth;


    public LoginFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init(view);

        clickListener();
    }

    private void init(View view) {
        emailEt = view.findViewById(R.id.emailET);
        passwordEt = view.findViewById(R.id.passwordET);
        loginBtn = view.findViewById(R.id.loginBtn);
//        googleSignInBtn = view.findViewById(R.id.googleSignInBtn);
        signUpTv = view.findViewById(R.id.signUpTV);
        forgotPasswordTv = view.findViewById(R.id.forgotTV);
        progressBar = view.findViewById(R.id.progressBar);

        auth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail().build();
        mGoogleSignInClient = GoogleSignIn.getClient( getActivity(), gso);
    }

    private void clickListener() {
        forgotPasswordTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplaceActivity) getActivity()).setFragment(new ForgotPassword());
            }
        });
        loginBtn.setOnClickListener(v -> {

            String email = emailEt.getText().toString();
            String password = passwordEt.getText().toString();
            if (email.isEmpty() || !email.matches(EMAIL_REGEX)) {
                emailEt.setError("Input valid email");
                return;
            }
            if (password.isEmpty() || password.length() < 6) {
                passwordEt.setError("Input 6 digit valid password");
                return;
            }
            progressBar.setVisibility(View.VISIBLE);
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener((task) -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            if (!user.isEmailVerified()) {
                                Toast.makeText(getContext(), "Welcome to NICHAT", Toast.LENGTH_SHORT).show();
                            }
                            sendUserToMainActivity();
                        } else {
                            String exception = "Error: " + task.getException().getMessage();
                            Toast.makeText(getContext(), exception, Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    });

        });
//        googleSignInBtn.setOnClickListener(v -> {
//            signIn();
//        });
        signUpTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ReplaceActivity) getActivity()).setFragment(new CreateAccountFragment());
            }
        });
    }

    private void sendUserToMainActivity() {
        if (getActivity() == null)
            return;
        progressBar.setVisibility(View.GONE);
        startActivity(new Intent(getActivity().getApplicationContext(), MainActivity.class));
        getActivity().finish();
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();

        startActivityForResult(signInIntent, RC_SIGN_IN);

    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//        if (requestCode == RC_SIGN_IN && data.getData()!=null) {
//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            if (!task.isSuccessful()) {
//                Toast.makeText(getContext(), "google signin thất bại ", Toast.LENGTH_LONG).show();
//
//                    throw new RuntimeException("loi nhieu lam");
//
//            }
//            try {
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                if (account == null) {
//                    Toast.makeText(getContext(), "account == null", Toast.LENGTH_LONG).show();
//                    throw new RuntimeException("loi nhieu lam 2");
//                }
//                String idToken = account.getIdToken();
//                if( idToken != null) {
//                    auth.signInWithCustomToken(idToken);
//                    firebaseAuthWithGoogle(idToken);
//                    sendUserToMainActivity();
//                }
//                else {
//                    Toast.makeText(getContext(),"id token == null neeeee ",Toast.LENGTH_LONG).show();
//                }
//                // When sign in account is not equal to null initialize auth credential
//                AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
//                // Check credential
//                auth.signInWithCredential(authCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> ctask) {
//                        // Check condition
//                        if (ctask.isSuccessful()) {
//                            // When task is successful redirect to profile activity display Toast
//                            sendUserToMainActivity();
//                        }
//                    }
//                });
//
//            } catch (ApiException e) {
//                e.printStackTrace();
//            }
//        }
//    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN && data.getData() != null) {
            // Check if the request code matches the one used for Google Sign-In
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (!task.isSuccessful()) {
                Toast.makeText(getContext(), "Google sign-in failed", Toast.LENGTH_LONG).show();
                throw new RuntimeException("Multiple errors occurred during Google sign-in");
            }
            try {
                // Retrieve the GoogleSignInAccount from the task
                GoogleSignInAccount account = task.getResult(ApiException.class);
                if (account == null) {
                    Toast.makeText(getContext(), "Account is null", Toast.LENGTH_LONG).show();
                    throw new RuntimeException("Account is null after Google sign-in");
                }
                // Get ID token for Firebase authentication
                String idToken = account.getIdToken();
                if (idToken != null) {
                    // Sign in with custom token and Firebase authentication
                    auth.signInWithCustomToken(idToken);
                    firebaseAuthWithGoogle(idToken);
                    sendUserToMainActivity();
                } else {
                    Toast.makeText(getContext(), "ID token is null", Toast.LENGTH_LONG).show();
                }
                // Create authentication credential for Firebase
                AuthCredential authCredential = GoogleAuthProvider.getCredential(account.getIdToken(), null);
                // Sign in with Firebase authentication credential
                auth.signInWithCredential(authCredential).addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> ctask) {
                        if (ctask.isSuccessful()) {
                            sendUserToMainActivity();
                        }
                    }
                });

            } catch (ApiException e) {
                e.printStackTrace();
            }
        }
    }


    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = auth.getCurrentUser();
                            updateUi(user);
                        } else {
                            Log.w("TAG", "ignInWithCredential:failure", task.getException());
                        }
                    }
                });
    }

    private void updateUi(FirebaseUser user) {
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        Map<String, Object> map = new HashMap<>();
        map.put("name", account.getDisplayName());
        map.put("email", account.getEmail());
        map.put("profileImage", String.valueOf(account.getPhotoUrl()));
        map.put("uid", user.getUid());
        map.put("status", " ");
        map.put("online", false);
        map.put("isAdmin",false);

        FirebaseFirestore.getInstance().collection("Users").document(user.getUid())
                .set(map)
                .addOnCompleteListener((task) -> {
                    if (task.isSuccessful()) {
                        assert getActivity() != null;
                        progressBar.setVisibility(View.GONE);
                        sendUserToMainActivity();
                    } else {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(getContext(), "Error " + task.getException().getMessage()
                                , Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
