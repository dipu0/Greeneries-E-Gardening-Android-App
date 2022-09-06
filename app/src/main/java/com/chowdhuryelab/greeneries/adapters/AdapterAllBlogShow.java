package com.chowdhuryelab.greeneries.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.chowdhuryelab.greeneries.R;
import com.chowdhuryelab.greeneries.models.ModelBlog;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

public class AdapterAllBlogShow extends RecyclerView.Adapter<AdapterAllBlogShow.HolderAdapterAllBlogShow>{

    private final ArrayList<ModelBlog> blogList;
    private Context context;


    public AdapterAllBlogShow(Context context, ArrayList<ModelBlog> blogList) {
        this.context = context;
        this.blogList = blogList;
    }


    @NonNull
    @Override
    public HolderAdapterAllBlogShow onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.row_blogs, parent, false);
        return new HolderAdapterAllBlogShow(view);
    }

    class HolderAdapterAllBlogShow extends RecyclerView.ViewHolder{

        private ImageView blogIv, blogerIV, details;
        private TextView titleTv, blogerName,descriptionTv;

        public HolderAdapterAllBlogShow(@NonNull View itemView) {
            super(itemView);

            blogIv =  itemView.findViewById(R.id.blogIV);
            blogerIV = itemView.findViewById(R.id.blogerIV);
            blogerName = itemView.findViewById(R.id.name);
            titleTv =  itemView.findViewById(R.id.titleTV);
            descriptionTv =  itemView.findViewById(R.id.descriptionTV);
            details = itemView.findViewById(R.id.nextIV);
        }
    }


    private void showDetailsDialog(ModelBlog modelBlog) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_productdetails, null);

        ImageView blogIV = view.findViewById(R.id.blogIV);
        ImageView blogerIV = view.findViewById(R.id.blogerIV);
        TextView titleTV = view.findViewById(R.id.titleTV);
        TextView descriptionTV = view.findViewById(R.id.descriptionTV);
        Button continueBtn = view.findViewById(R.id.continueBtn);
        TextView blogerName = view.findViewById(R.id.name);


        String blogId = modelBlog.getBlogId();
        String title = modelBlog.getBlogTitle();
        String description = modelBlog.getBlogDescription();
        String image = modelBlog.getblogImg();
        String category = modelBlog.getPrductCategory();
        String userId = modelBlog.getUid();


//
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setView(view);
//
//        try {
//            Picasso.get().load(image).placeholder(R.drawable.ic_shopping_cart_black).into(blogIV);
//        }
//        catch (Exception e){
//            blogIV.setImageResource(R.drawable.ic_shopping_cart_black);
//        }
//        titleTV.setText(""+title);
//        descriptionTV.setText(""+description);
//
//
//        final AlertDialog dialog = builder.create();
//        dialog.show();

    }
    private void showCartDialog(ModelBlog modelBlog) {
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_quantity, null);

        ImageView BlogIV = view.findViewById(R.id.blogIV);
        final TextView titleTV = view.findViewById(R.id.titleTV);
        TextView descriptionTV = view.findViewById(R.id.descriptionTV);

        final String blogId = modelBlog.getBlogId();
        String title = modelBlog.getBlogTitle();

        String description = modelBlog.getBlogDescription();

        final String image = modelBlog.getblogImg();
        final String category = modelBlog.getPrductCategory();
        final String uid = modelBlog.getUid();

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(view);

        try {
            Picasso.get().load(image).placeholder(R.drawable.ic_category_gray).into(BlogIV);
        }
        catch (Exception e){
            BlogIV.setImageResource(R.drawable.ic_category_gray);
        }

        titleTV.setText(""+title);
        descriptionTV.setText(""+description);

        final AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onBindViewHolder(@NonNull HolderAdapterAllBlogShow holder, int position) {

        final ModelBlog modelBlog = blogList.get(position);

        String BlogCategory = modelBlog.getPrductCategory();
        String blogDescription = modelBlog.getBlogDescription();
        String BlogTitle = modelBlog.getBlogTitle();

        String blogId = modelBlog.getBlogId();
        String timestamp = modelBlog.getTimestamp();
        String blogImage = modelBlog.getblogImg();
        String uid = modelBlog.getUid();

        holder.titleTv.setText(BlogTitle);
        holder.descriptionTv.setText(blogDescription);

        try {
            Picasso.get().load(blogImage).placeholder(R.drawable.ic_category_gray).into(holder.blogIv);
        }
        catch (Exception e) {
            holder.blogIv.setImageResource(R.drawable.ic_category_gray);
        }

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        ref.child(uid)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        String name = ""+dataSnapshot.child("name").getValue();
                        String profileImage = ""+dataSnapshot.child("profileImage").getValue();

                        holder.blogerName.setText(name);
                        try {
                            Picasso.get().load(profileImage).placeholder(R.drawable.ic_store_gray).into(holder.blogerIV);
                        }
                        catch (Exception e) {
                            holder.blogerIV.setImageResource(R.drawable.ic_store_gray);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        holder.details.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return blogList.size();
    }

//    @Override
//    public Filter getFilter() {
//        if (filter==null) {
//            filter = new FilterBlogAll(this, filterList);
//        }
//        return filter;
//    }

}
