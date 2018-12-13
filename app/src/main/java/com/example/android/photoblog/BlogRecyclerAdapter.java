package com.example.android.photoblog;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogRecyclerAdapter extends RecyclerView.Adapter<BlogRecyclerAdapter.ViewHolder>{


    public List<BlogPost> blog_list;
    public Context context;
    private FirebaseFirestore firestore;


    public BlogRecyclerAdapter(List<BlogPost> blog_list){

        this.blog_list = blog_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.blog_list_view,viewGroup,false);
        context = viewGroup.getContext();
        firestore = FirebaseFirestore.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {

        String postDesc = blog_list.get(i).getDesc();
        viewHolder.setBlogDesc(postDesc);

        String imageUrl = blog_list.get(i).getImage_url();
        viewHolder.setBlogPhoto(imageUrl);

        long millisecond = blog_list.get(i).getTimeStamp().getTime();

        SimpleDateFormat sm  = new SimpleDateFormat("mm-dd-yyyy");
        String dateFormat = sm.format(millisecond);
        viewHolder.setBlogTime(dateFormat);

        String user_id = blog_list.get(i).getUser_id();
        firestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if(task.isSuccessful()){
                    String userName = task.getResult().getString("name");
                    String userImage = task.getResult().getString("image");
                    viewHolder.setUserData(userName, userImage);

                }else{

                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return blog_list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private View mView;
        private TextView blog_desc;
        private ImageView blog_image;
        private TextView blog_time;
        private TextView blog_username;
        private CircleImageView blog_userImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setBlogDesc(String blogDesc){
            blog_desc = mView.findViewById(R.id.blog_desc);
            blog_desc.setText(blogDesc);
        }

        public void setBlogPhoto(String downloadUrl){
            blog_image = mView.findViewById(R.id.blogImage);

            //Add a placeHolder Image for the blogpost here
            Glide.with(context).load(downloadUrl).into(blog_image);
        }
        public void setBlogTime(String time){
            blog_time = mView.findViewById(R.id.blog_post_date);
            blog_time.setText(time);
        }
        public void setUserData(String name, String image){
            blog_username =mView.findViewById(R.id.blog_username);
            blog_userImage = mView.findViewById(R.id.blog_user_image);

            blog_username.setText(name);

            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.com_facebook_profile_picture_blank_portrait);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(image).into(blog_userImage);

        }
    }

}
