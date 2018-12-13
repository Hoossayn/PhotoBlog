package com.example.android.photoblog;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class HomeFragment extends Fragment {

    private RecyclerView blogListView;
    private List<BlogPost> blog_list;
    FirebaseFirestore firebaseFirestore;
    private FirebaseAuth mAuth;
    private BlogRecyclerAdapter blogRecyclerAdapter;
    private DocumentSnapshot lastVisible;


    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        mAuth = FirebaseAuth.getInstance();
        blogListView = view.findViewById(R.id.blog_list_view);
        blog_list = new ArrayList<>();
        blogRecyclerAdapter = new BlogRecyclerAdapter(blog_list);
        blogListView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blogListView.setAdapter(blogRecyclerAdapter);

       if(mAuth.getCurrentUser() != null) {
           firebaseFirestore = FirebaseFirestore.getInstance();


           blogListView.addOnScrollListener(new RecyclerView.OnScrollListener() {
               @Override
               public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                   super.onScrolled(recyclerView, dx, dy);

                   Boolean reachedBottom  = !recyclerView.canScrollVertically(1);

                   if(reachedBottom){

                       String desc = lastVisible.getString("desc");
                       Toast.makeText(container.getContext(), "Reached:"+ desc, Toast.LENGTH_SHORT).show();
                       loadMorePost();
                   }
               }
           });

           Query firstQuery = firebaseFirestore.collection("Post").orderBy("timestamp",Query.Direction.DESCENDING).limit(3);
           firstQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
               @Override
               public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                   if (!documentSnapshots.isEmpty()) {
                       lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);
                       for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                           if (doc.getType() == DocumentChange.Type.ADDED) {
                               BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                               blog_list.add(blogPost);

                               blogRecyclerAdapter.notifyDataSetChanged();
                           }
                       }

                   }
               }
           });
       }

        return view;
    }

    public void loadMorePost(){

        Query nextQuery = firebaseFirestore.collection("Post").orderBy("timestamp",Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(3);
        nextQuery.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {


                if(!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments().get(documentSnapshots.size() - 1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class);
                            blog_list.add(blogPost);

                            blogRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        });

    }

}
