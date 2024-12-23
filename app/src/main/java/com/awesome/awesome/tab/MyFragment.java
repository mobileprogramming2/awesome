package com.awesome.awesome.tab;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.awesome.awesome.adapter.MyRecyclerAdapter;
import com.awesome.awesome.entity.Assignment;
import com.awesome.awesome.sql.SQLiteHelper;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public abstract class MyFragment extends Fragment {

    SQLiteHelper sqLiteHelper;
    FloatingActionButton fab;
    RecyclerView recyclerView;
    MyRecyclerAdapter adapter;
    ArrayList<Assignment> assignmentList;

    public abstract void updateList();

}
