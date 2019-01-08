package com.example.android.photoblog;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.annotations.Nullable;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;



import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder>{

    public Context context;
    public List<BlogPost> blog_lists;
    public List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    public BlogRecyclerAdapter(List<BlogPost> blog_lists,List<User> user_list){
        this.blog_lists=blog_lists;
        this.user_list = user_list;

    }

    @Override
    public ViewHolder onCreateViewHolder( ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item,parent,false);
        context = parent.getContext();
        firebaseFirestore= FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @RequiresApi(api = Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, final int i) {
        viewHolder.setIsRecyclable(false);

        String desc_data = blog_lists.get(i).getDesc();
        String image_url = blog_lists.get(i).getImage_url();
        String blog_user_id = blog_lists.get(i).getUser_id();
        final String blogPostId = blog_lists.get(i).BlogPostId;
        final String current_user_id = firebaseAuth.getCurrentUser().getUid();

        //user data;

        if(blog_user_id.equals(current_user_id)){
            viewHolder.delPostbtn.setEnabled(true);
            viewHolder.delPostbtn.setVisibility(View.VISIBLE);
        }

        String useName = user_list.get(i).getName();
        final String im_url = user_list.get(i).getImage();

        viewHolder.setUserData(useName,im_url);


        try {
            long currentTime = System.currentTimeMillis();
            long millisecond = blog_lists.get(i).getTimestamp().getTime();
            long diff = currentTime-millisecond;

            long days  = TimeUnit.MILLISECONDS.toDays(diff);


            if(days>3){
                String dateString = DateFormat.format("MM/dd/yyyy", new Date(millisecond)).toString();
                viewHolder.setTime(dateString);

            }else if(days<=3&&days>=1){
                viewHolder.setTime(days+ "days");
            }else {
                long hour = TimeUnit.MILLISECONDS.toHours(diff);
                if (hour >= 1) {
                    viewHolder.setTime(hour + "hours");
                } else {
                    long min = TimeUnit.MILLISECONDS.toMinutes(diff);
                    if (min >= 1) {
                        viewHolder.setTime(min + "min");
                    } else  {
                        viewHolder.setTime("just now");
                    }
                }
            }

        } catch (Exception e) {

            Toast.makeText(context, "Exception : " + e.getMessage(), Toast.LENGTH_SHORT).show();

        }

        //count like
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    viewHolder.updatelike(count);
                }else {
                    viewHolder.updatelike(0);
                }
            }
        });

        // count comments
        firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if(!queryDocumentSnapshots.isEmpty()){
                    int count = queryDocumentSnapshots.size();
                    viewHolder.updateCommentsCount(count);
                }else {
                    viewHolder.updateCommentsCount(0);
                }
            }
        });

        viewHolder.setBlogImage(image_url);
        viewHolder.setDescText(desc_data);

        //btn like cliked
        firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                if(documentSnapshot.exists()){
                    viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.action_like_accent));
                }else {
                    viewHolder.blogLikeBtn.setImageDrawable(context.getDrawable(R.drawable.ic_favorite_black_24dp));

                }
            }
        });

        viewHolder.blogCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent commentIntent = new Intent(context, CommentsActivity.class);
                commentIntent.putExtra("blog_post_id", blogPostId);
                context.startActivity(commentIntent);

            }
        });

        viewHolder.blogLikeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //like
                firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String,Object> likeMap = new HashMap<>();
                            likeMap.put("timestamp",FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).set(likeMap);
                        }else {

                            firebaseFirestore.collection("Posts/"+blogPostId+"/Likes").document(current_user_id).delete();

                        }
                    }
                });

            }
        });

        viewHolder.delPostbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts").document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        blog_lists.remove(i);
                        user_list.remove(i);
                    }
                });
            }
        });

        viewHolder.blogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              Intent fullImageIntent = new Intent(context, FullBlogImage.class);
              fullImageIntent.putExtra("image_url",im_url);
              context.startActivity(fullImageIntent);

            }
        });

    }

    @Override
    public int getItemCount() {
        if (blog_lists!=null){
            return blog_lists.size();
        }else {
            return 0;
        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;

        private TextView descView;
        private ImageView blogImageView;
        private CircleImageView userImageView;
        private TextView userNameView;
        private TextView blogDate;
        private ImageView blogLikeBtn;
        private TextView BlogLikeCount;
        private ImageView blogCommentBtn;
        private TextView commentsCountView;
        private ImageView delPostbtn;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            blogLikeBtn = mView.findViewById(R.id.blog_like_btn);
            delPostbtn = mView.findViewById(R.id.post_delete_btn);
            blogCommentBtn = mView.findViewById(R.id.blog_comment_icon);
        }

        public void setDescText(String desctext) {
            this.descView = mView.findViewById(R.id.blog_desc);
            descView.setText(desctext);
        }

        public void setBlogImage(String DownloadUri) {
            this.blogImageView = mView.findViewById(R.id.blog_imge);
            Glide.with(context).load(DownloadUri).into(blogImageView);
        }

        public void setUserData(String userName,String posts_url){
            this.userImageView = mView.findViewById(R.id.blog_user_img);

            Glide.with(context.getApplicationContext()).load(posts_url).into(userImageView);

            this.userNameView = mView.findViewById(R.id.blog_user_name);
            userNameView.setText(userName);
        }

        public void setTime(String date) {

            blogDate = mView.findViewById(R.id.blog_post_date);
            blogDate.setText(date);

        }

        public void updatelike(int count){
            BlogLikeCount = mView.findViewById(R.id.blog_like_count);
            BlogLikeCount.setText(count+" Likes");
        }

        public void updateCommentsCount(int count){
            commentsCountView = mView.findViewById(R.id.blog_comment_count);
            commentsCountView.setText(count+" comments");
        }

    }
}