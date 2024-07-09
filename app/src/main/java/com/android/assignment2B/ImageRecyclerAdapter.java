package com.android.assignment2B;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ImageRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private ArrayList<String> arrayList = new ArrayList<>();
    Context mContext;
    Activity mActivity;
    private ViewType viewType;

    public ImageRecyclerAdapter(Context mContext, ArrayList<String> arrayList, ViewType viewType) {
        this.arrayList = arrayList;
        this.mContext = mContext;
        this.viewType = viewType;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = null;
            switch (this.viewType) {
                case one_colomn:
                    v = LayoutInflater.from(mContext).inflate(R.layout.item_image_list, parent, false);
                    break;
                case two_colomn:
                    v = LayoutInflater.from(mContext).inflate(R.layout.item_image_list_2_colomn, parent, false);
                    break;
        }

        return new SimpleViewHolder(v);

    }


    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        ImageView ivPhoto;

        public SimpleViewHolder(View view) {
            super(view);
            ivPhoto = view.findViewById(R.id.ivPhoto);
        }
    }



    @Override public void onBindViewHolder(final RecyclerView.ViewHolder Holder, final int pos) {

        WindowManager windowManager = (WindowManager) mContext.getSystemService(mContext.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
//        int height = windowManager.getDefaultDisplay().getHeight();

        try {


            if (Holder instanceof SimpleViewHolder) {


                SimpleViewHolder holder = ((SimpleViewHolder) Holder);

                holder.ivPhoto.getLayoutParams().height = width/2;

                Picasso.get().load(arrayList.get(pos)).into(holder.ivPhoto);

                holder.ivPhoto.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        MainActivity m = new MainActivity();
                        m.openFullScreen(arrayList.get(pos),mContext);
                        //openFullScreen(arrayList.get(pos));
                    }
                });

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // Reference : https://stackoverflow.com/questions/50867570/how-to-limit-number-of-items-in-recyclerview
    @Override
    public int getItemCount() {

        final int limit = 15;

        if (arrayList.size() > limit) {
            return limit;
        }
        else {
            return arrayList.size();
        }

    }

    public void updateList(ArrayList<String> arraylist){
        this.arrayList = arraylist;
        notifyDataSetChanged();
    }


}

