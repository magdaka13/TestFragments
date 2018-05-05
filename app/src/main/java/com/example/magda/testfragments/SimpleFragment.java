package com.example.magda.testfragments;



import android.annotation.SuppressLint;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.net.ConnectivityManager;
import android.widget.Toast;

import java.net.URL;
import java.util.ArrayList;
import java.util.Random;

import static android.content.Context.CONNECTIVITY_SERVICE;

/**
 * A simple {@link Fragment} subclass.
 */
public class SimpleFragment extends Fragment {

    //private static final int YES = 0;
    //private static final int NO = 1;

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static final String ITEM_QUERY_URL = "query";
    Bundle sourceBundle = new Bundle();
    private static final int ID_LOADER = 445;
    NetworkInfo info;
    private RecyclerView mRecyclerView;
    private  ArrayList<Item> items;
    private MyAdapter mAdapter;

    public SimpleFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        final View rootView =
                inflater.inflate(R.layout.fragment_simple, container, false);

        mRecyclerView =  (RecyclerView)rootView.findViewById(R.id.recyclerview_items);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        getActivity().getSupportLoaderManager().initLoader(ID_LOADER, sourceBundle, new ItemsLoader());
        restartLoader();

        // Return the View for the fragment's UI.
        return rootView;
    }


    private boolean isConnected(){
        ConnectivityManager cm = (ConnectivityManager) getActivity().getSystemService(CONNECTIVITY_SERVICE);

        try {
            info = cm.getActiveNetworkInfo();
        }catch(Exception e)
        {
            Log.e(TAG,"Cannot retrieve ActiveNetworkInfo"+e.getMessage());
        }

        return info != null && info.isConnectedOrConnecting();
    }

    private int anyRandomInt(Random random) {
        return random.nextInt();
    }

    private void restartLoader() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    URL ItemUrl = NetworkUtils.buildUrlItems();
                    sourceBundle.putString(ITEM_QUERY_URL, ItemUrl.toString());

                    Random random = new Random();
                    int uniqueId = anyRandomInt(random); //Generates a new ID for each loader call;


                    LoaderManager loaderManager = getActivity().getSupportLoaderManager();

                    if (loaderManager.getLoader(ID_LOADER) == null) {
                        loaderManager.initLoader(uniqueId, sourceBundle, new ItemsLoader());
                    } else {
                        loaderManager.restartLoader(ID_LOADER, sourceBundle, new
                                ItemsLoader());
                    }
                }catch (Exception e)
                {
                    Log.e(TAG,"Cannot set URL for items"+e.getMessage());
                }
            }
        }, 5000);
    }

    private void showErrorScreen(){
        Toast.makeText(getActivity(),"error loading data",Toast.LENGTH_LONG).show();
    }



    public class ItemsLoader implements LoaderManager.LoaderCallbacks<ArrayList<Item>> {
        @SuppressLint("StaticFieldLeak")
        @Override
        public Loader<ArrayList<Item>> onCreateLoader(int id, final Bundle args) {
            if (isConnected()){
                mRecyclerView.setVisibility(View.VISIBLE);
                return new AsyncTaskLoader<ArrayList<Item>>(getActivity()) {
                    ArrayList<Item> mItemsData;

                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();
                        if (mItemsData != null){
                            deliverResult(mItemsData);
                        }else{
                            forceLoad();
                        }
                    }

                    @Override
                    public ArrayList<Item> loadInBackground() {
                        ArrayList<Item> items1 = NetworkUtils.parseJSON();
                        return items1;
                    }

                    public void deliverResult(ArrayList<Item> data) {
                        mItemsData = data;
                        super.deliverResult(data);
                    }
                };
            }else{
                showErrorScreen();
                return null;
            }
        }

        @Override
        public void onLoadFinished(Loader<ArrayList<Item>> loader, ArrayList<Item> data) {
            if (null == data) {
                showErrorScreen();
            } else {
                if (items != null) {
                    items.clear();
                    items.addAll(data);
                    mAdapter = new MyAdapter(items);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    mAdapter.notifyDataSetChanged();
                } else {
                    items = data;
                    mAdapter = new MyAdapter(items);
                    mRecyclerView.setAdapter(mAdapter);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false));
                    mAdapter.notifyDataSetChanged();

                }
                Log.i( "  this is the data", data.toString());
// Array of objects shows in the log
            }
        }

        @Override
        public void onLoaderReset(Loader<ArrayList<Item>> loader) {
            loader.forceLoad();
        }

    }
/*
    public static interface ClickListener{
        public void onClick(View view,int position);
        public void onLongClick(View view,int position);
        public void onFocus(View view,int position);
    }
    class RecyclerTouchListener implements RecyclerView.OnItemTouchListener{

        private ClickListener clicklistener;
        private GestureDetector gestureDetector;

        public RecyclerTouchListener(Context context, final RecyclerView recycleView, final ClickListener clicklistener){

            this.clicklistener=clicklistener;
            gestureDetector=new GestureDetector(context,new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onSingleTapUp(MotionEvent e) {
                    return true;
                }

                @Override
                public void onLongPress(MotionEvent e) {
                    View child=recycleView.findChildViewUnder(e.getX(),e.getY());
                    if(child!=null && clicklistener!=null){
                        clicklistener.onLongClick(child,recycleView.getChildAdapterPosition(child));
                    }
                }

            });
        }

        @Override
        public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
            View child=rv.findChildViewUnder(e.getX(),e.getY());
            if(child!=null && clicklistener!=null && gestureDetector.onTouchEvent(e)){
                clicklistener.onClick(child,rv.getChildAdapterPosition(child));
            }

            return false;
        }

        @Override
        public void onTouchEvent(RecyclerView rv, MotionEvent e) {

        }

        @Override
        public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

        }
    }
*/
}
