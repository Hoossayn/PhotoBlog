package com.example.android.photoblog;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private String user_id;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private Toolbar toolbar;
    private FloatingActionButton addPostBtn;
    private BottomNavigationView mainBottomNav;


    //fragment
    private HomeFragment homeFragment;
    private NoticationFragment noticationFragment;
    private AccountFragment accountFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        mAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        toolbar = (Toolbar)findViewById(R.id.main_tollbar);
//        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Mon blog");

        addPostBtn= (FloatingActionButton) findViewById(R.id.add_post_btn);
        mainBottomNav= (BottomNavigationView)findViewById(R.id.main_btm_nav);


        //fragments
        if(mAuth.getCurrentUser() != null) {
            homeFragment = new HomeFragment();
            noticationFragment = new NoticationFragment();
            accountFragment = new AccountFragment();

            initializeFragment();

            mainBottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case R.id.item_home:
                            replaceFragment(homeFragment);
                            return true;
                        case R.id.item_notif:
                            replaceFragment(noticationFragment);
                            return true;
                        case R.id.item_account:
                            replaceFragment(accountFragment);
                            return true;
                        default:
                            return false;
                    }
                }
            });


            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent it = new Intent(MainActivity.this, NewPostActivity.class);
                    startActivity(it);
                }
            });
        }
    }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = mAuth.getCurrentUser();
        if(user==null){
            sendToLogin();
        }else {

            user_id = mAuth.getUid();
            firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()){

                        if(!task.getResult().exists()){

                            Intent it = new Intent(MainActivity.this,SetupActivity.class);
                            startActivity(it);
                            finish();

                        }

                    }else {

                        Toast.makeText(MainActivity.this,"Error: "+task.getException().getMessage(),Toast.LENGTH_LONG).show();
                    }

                }
            });
        }
    }




    private void sendToLogin() {
        Intent i = new Intent(MainActivity.this,LoginActivity.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout_btn:
                logOut();
                return true;
            case R.id.action_search_btn:

                return true;
            case R.id.action_set_btn:
                Intent intent = new Intent(MainActivity.this,SetupActivity.class);
                startActivity(intent);
                return true;
            default:
                return false;
        }
    }

    private void logOut() {
        mAuth.signOut();
        sendToLogin();
    }

    private void initializeFragment(){

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_container, homeFragment);
        fragmentTransaction.add(R.id.main_container, noticationFragment);
        fragmentTransaction.add(R.id.main_container, accountFragment);

        fragmentTransaction.hide(noticationFragment);
        fragmentTransaction.hide(accountFragment);

        fragmentTransaction.commit();

    }

    private void replaceFragment(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_container,fragment);
        fragmentTransaction.commit();
    }
}