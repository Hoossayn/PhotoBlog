package com.example.android.photoblog;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    public List<Comments> commentsList;
    public Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    public List<User> userList;
    public String blogPostId;
    public CommentsRecyclerAdapter(List<Comments> commentsList,List<User> userList,String blogPostId){

        this.commentsList = commentsList;
        this.userList = userList;
        this.blogPostId=blogPostId;

    }

    @Override
    public CommentsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
        context = parent.getContext();
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();


        return new CommentsRecyclerAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final CommentsRecyclerAdapter.ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        final String current_user_id = firebaseAuth.getCurrentUser().getUid();
        String commentMessage = commentsList.get(position).getMessage();
        final String commenId = commentsList.get(position).getComment_id();
        String user_comment_id = userList.get(position).getUser_id();

        String userName = userList.get(position).getName();
        String downloadUri = userList.get(position).getImage();

        holder.setUserCommentData(userName,downloadUri);
        holder.setComment_message(commentMessage);



        if(user_comment_id.equals(current_user_id)){
            holder.onEditMenu = new MenuItem.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()){
                        case 1:
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").document(commenId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        String text = task.getResult().getString("message");
                                        holder.EditCommentView.setText(text);
                                        holder.EditCommentView.setVisibility(View.VISIBLE);
                                        holder.cancelBtn.setVisibility(View.VISIBLE);
                                        holder.updateBtn.setVisibility(View.VISIBLE);
                                        holder.comment_message.setVisibility(View.INVISIBLE);
                                        holder.userNameView.setVisibility(View.INVISIBLE);

                                    }
                                }
                            });

                            return true;
                        case 2:
                            firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").document(commenId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    commentsList.remove(position);
                                    userList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                            return true;
                        default:
                            return false;
                    }
                }


            };

            holder.updateBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Map<String,Object> messageUpdate = new HashMap<>();
                    final String message = holder.EditCommentView.getText().toString();
                    holder.EditCommentView.setVisibility(View.INVISIBLE);
                    holder.cancelBtn.setVisibility(View.INVISIBLE);
                    holder.updateBtn.setVisibility(View.INVISIBLE);
                    holder.comment_message.setVisibility(View.VISIBLE);
                    holder.userNameView.setVisibility(View.VISIBLE);
                    if(!TextUtils.isEmpty(message)){
                        messageUpdate.put("message",message);
                        final Comments comment = commentsList.get(position);


                        firebaseFirestore.collection("Posts/"+blogPostId+"/Comments").document(commenId)
                                .set(messageUpdate,SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                comment.setMessage(message);
                                commentsList.set(position,comment);

                                notifyDataSetChanged();
                            }
                        });
                    }


                }
            });

            holder.cancelBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    holder.EditCommentView.setVisibility(View.INVISIBLE);
                    holder.cancelBtn.setVisibility(View.INVISIBLE);
                    holder.updateBtn.setVisibility(View.INVISIBLE);
                    holder.comment_message.setVisibility(View.VISIBLE);
                    holder.userNameView.setVisibility(View.VISIBLE);
                }
            });
        }



    }


    @Override
    public int getItemCount() {

        if(commentsList != null) {

            return commentsList.size();

        } else {

            return 0;

        }

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnCreateContextMenuListener {

        private View mView;
        private TextView userNameView;
        private CircleImageView userCommentImageView;

        private EditText EditCommentView;
        private Button updateBtn;
        private Button cancelBtn;
        private TextView comment_message;
        String edit_message;
        private  MenuItem.OnMenuItemClickListener onEditMenu;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            EditCommentView = mView.findViewById(R.id.edit_comment_msg);
            updateBtn = mView.findViewById(R.id.cmnt_update_btn);
            cancelBtn = mView.findViewById(R.id.cmnt_annule_btn);
            mView.setOnCreateContextMenuListener(this);

        }

        public void setComment_message(String message){

            comment_message = mView.findViewById(R.id.comment_message);
            comment_message.setText(message);

        }

        public void setUserCommentData(String userName,String dowwloadUri){
            userNameView = mView.findViewById(R.id.comment_username);
            userCommentImageView = mView.findViewById(R.id.comment_image);
            userNameView.setText(userName);
            Glide.with(context.getApplicationContext()).load(dowwloadUri).into(userCommentImageView);
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

            MenuItem edit = menu.add(Menu.NONE,1,1,"Editer");
            MenuItem supp = menu.add(Menu.NONE,2,2,"supprimer");
            edit.setOnMenuItemClickListener(onEditMenu);
            supp.setOnMenuItemClickListener(onEditMenu);

        }
    }


}
