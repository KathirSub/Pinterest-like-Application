package com.android.assignment2B;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;


import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.Callable;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

// REFERENCE : https://www.youtube.com/watch?v=wViZsuCptt4
// Used For 2 column Recycler View

public class  MainActivity extends AppCompatActivity {

    FirebaseStorage storage;
    Uri imageUri;
    ImageView image;
    StorageReference storageRef;
    Button upload;
    EditText etSearch;
    ImageView ivSearch;
    ProgressBar topProgressBar;
    RecyclerView recyclerView;
    ImageRecyclerAdapter adapter;
    ArrayList<String> imageList = new ArrayList<>();
    int currentPage = 1;
    private boolean isLoading = false, isLastPage = false;

    private Menu menu ;
    private ViewType selectedViewType = ViewType.one_colomn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeView();

    }

    public void initializeView(){
        etSearch = findViewById(R.id.etSearchbar);
        ivSearch = findViewById(R.id.ivSearch);
        topProgressBar = findViewById(R.id.topProgressBar);
        recyclerView = findViewById(R.id.recyclerView);

        upload = findViewById(R.id.UploadImageBtn);
        image = findViewById(R.id.picture);


/*
        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //try {
                    //uploadImage();
                //}
                //catch (Exception e){
                    //Toast.makeText(MainActivity.this, "No Image Found to Upload", Toast.LENGTH_LONG).show();
                //}
            }
        });
*/

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        adapter = new ImageRecyclerAdapter(this, imageList,selectedViewType);
        recyclerView.setAdapter(adapter);

        RecyclerView.OnScrollListener recyclerViewOnScrollListener = new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = mLayoutManager.getChildCount();
                int totalItemCount = mLayoutManager.getItemCount();
                int firstVisibleItemPosition = mLayoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition + 1) >= totalItemCount) {
                        getPhotos(etSearch.getText().toString());
                    }
                }
            }
        };

        recyclerView.addOnScrollListener(recyclerViewOnScrollListener);

        ivSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentPage=1;
                isLastPage=false;
                getPhotos(etSearch.getText().toString());
            }
        });

    }

    private void uploadImage() {

        ByteArrayOutputStream Byte = new ByteArrayOutputStream();
        byte[] data = Byte.toByteArray();

        StorageReference ref = storageRef.child("images/"+ UUID.randomUUID().toString());

        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        image.setImageURI(null);
                        Toast.makeText(MainActivity.this,"Successfully Uploaded",Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this,"Couldn't Upload, Try Again",Toast.LENGTH_SHORT).show();
                    }
                });

        }


    private void getPhotos(final String searchString) {
        if(!searchString.trim().equalsIgnoreCase("")) {

            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);

            // Reactive Programming Implementation
/*
            InternetOperations io = new InternetOperations();
            Single<String> searchObservable = Single.fromCallable((Callable<? extends String>) io);
            searchObservable = searchObservable.subscribeOn(Schedulers.io());
            searchObservable = searchObservable.observeOn(AndroidSchedulers.mainThread());
            searchObservable.subscribe(new SingleObserver<String>() {
                @Override
                public void onSubscribe(@NonNull Disposable d) {

                }

                @Override
                public void onSuccess(@NonNull String s) {
                    Toast.makeText(MainActivity.this, "Works ! ", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(@NonNull Throwable e) {
                    Toast.makeText(MainActivity.this, "Error while Searching ! ", Toast.LENGTH_SHORT).show();
                }
            });
*/
            @SuppressLint("StaticFieldLeak") AsyncTask<Void, Void, String> myTask = new AsyncTask<Void, Void, String>() {

                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    topProgressBar.setVisibility(View.VISIBLE);
                    isLoading = true;
                }
                @Override
                protected String doInBackground(Void... params) {

                    String response;

                    try {

                        String url = InternetOperations.SERVER_URL+"?key="+InternetOperations.API_KEY+"&q="+searchString+"&image_type=photo&page="+currentPage;

                        response = InternetOperations.get(url);

                        return response;

                    } catch (Exception e) {
                        e.printStackTrace();

                    }
                    return null;
                }

                protected void onPostExecute(String s) {

                    topProgressBar.setVisibility(View.GONE);
                    isLoading = false;

                    if (s != null) {

                        try {

                            JSONObject object = new JSONObject(s);

                            JSONArray jsonArray = object.optJSONArray("hits");

                            if(currentPage==1)
                                imageList.clear();

                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.optJSONObject(i);
                                    imageList.add(jsonObject.optString("webformatURL"));
                                }

                                adapter.updateList(imageList);

                                currentPage++;

                                if(jsonArray.length()==0)
                                isLastPage = true;


                        } catch (Exception e) {
                            e.printStackTrace();

                        }

                    }


                }
            };
            myTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        this.menu = menu ;
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.ChangeView:
                changeSelectedViewType();
                changeMenuItemIcon();
                changeRecyclerViewType();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeSelectedViewType() {
        switch (selectedViewType) {
            case one_colomn:
                selectedViewType = ViewType.two_colomn;
                break;
            case two_colomn:
                selectedViewType = ViewType.one_colomn;
                break;
        }
    }

    private void changeMenuItemIcon() {
        switch (selectedViewType) {
            case one_colomn:
                menu.findItem(R.id.ChangeView).setIcon(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_two_colomn));
                break;
            case two_colomn:
                menu.findItem(R.id.ChangeView).setIcon(ContextCompat.getDrawable(MainActivity.this,R.drawable.ic_one_colomn));
                break;
        }
    }

    private void changeRecyclerViewType() {
        switch (selectedViewType) {
            case one_colomn:
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                adapter = new ImageRecyclerAdapter(this, imageList,selectedViewType);
                recyclerView.setAdapter(adapter);
                break;
            case two_colomn:
                recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL));
                adapter = new ImageRecyclerAdapter(this, imageList,selectedViewType);
                recyclerView.setAdapter(adapter);
                break;
        }
    }

    //  To show image in fullscreen
    public void openFullScreen(String url, Context mContext){

        Dialog dialog = new Dialog(mContext);
        dialog.setContentView(R.layout.dialog_full_screen);
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);

        ImageView imageView = dialog.findViewById(R.id.picture);

        Picasso.get().load(url).into(imageView);

        dialog.show();
    }
}
