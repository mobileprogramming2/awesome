package com.awesome.awesome.tab;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.awesome.awesome.R;
import com.awesome.awesome.activity.AssignmentForm;
import com.awesome.awesome.adapter.MyRecyclerAdapter;
import com.awesome.awesome.entity.Assignment;
import com.awesome.awesome.sql.SQLiteHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Complete extends MyFragment {
    private Spinner sortSpinner;

    @Override
    public void onResume() {
        super.onResume();

        updateList();
    }

    @Override
    public void updateList() {
        assignmentList.clear();
        assignmentList.addAll(sqLiteHelper.getCompleteAssignments());
        Collections.sort(assignmentList, new Comparator<Assignment>() {
            @Override
            public int compare(Assignment a1, Assignment a2) {
                return a1.getEndDateTime().compareTo(a2.getEndDateTime());
            }
        }.reversed());
        adapter.notifyDataSetChanged();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.incomplete_fragment, container, false);

        if (v == null) {
            Log.e("IncompleteFragment", "Layout inflation 실패!");
        } else {
            Log.d("IncompleteFragment", "Layout inflation 성공!");
        }

        sqLiteHelper = new SQLiteHelper(getContext());
        recyclerView = v.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        assignmentList = new ArrayList<>();
        adapter = new MyRecyclerAdapter(assignmentList, this);
        recyclerView.setAdapter(adapter);


        // FloatingActionButton 설정
        fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AssignmentForm.class);

                startActivity(intent);
            }
        });

        // 스피너 설정, arrays.xml의 sort_array
        sortSpinner = v.findViewById(R.id.sort);
        ArrayAdapter<CharSequence> priorityAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_array, android.R.layout.simple_spinner_item);
        priorityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(priorityAdapter);

        sortSpinner.setSelection(1);

        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                sortAssignments(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // 아무 것도 선택되지 않았을 때
            }
        });

        return v;
    }

    private void sortAssignments(int position) {
        if (position == 0) { // '마감일 빠른순'
            Collections.sort(assignmentList, new Comparator<Assignment>() {
                @Override
                public int compare(Assignment a1, Assignment a2) {
                    return a1.getEndDateTime().compareTo(a2.getEndDateTime()); // 빠른 날짜 -> 느린 날짜
                }
            });
        } else if (position == 1) { // '마감일 느린순'
            Collections.sort(assignmentList, new Comparator<Assignment>() {
                @Override
                public int compare(Assignment a1, Assignment a2) {
                    return a2.getEndDateTime().compareTo(a1.getEndDateTime()); // 느린 날짜 -> 빠른 날짜
                }
            });
        } else if (position == 2) { // '우선순위 높은순'
            Collections.sort(assignmentList, new Comparator<Assignment>() {
                @Override
                public int compare(Assignment a1, Assignment a2) {
                    return a2.getPriority().compareTo(a1.getPriority()); // High -> Low
                }
            });
        }  else { // '우선순위 낮은순'
            Collections.sort(assignmentList, new Comparator<Assignment>() {
                @Override
                public int compare(Assignment a1, Assignment a2) {
                    return a1.getPriority().compareTo(a2.getPriority()); // Low -> High
                }
            });
        }
        adapter.notifyDataSetChanged();
    }
}
