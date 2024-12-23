package com.awesome.awesome.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.awesome.awesome.R;
import com.awesome.awesome.Status;
import com.awesome.awesome.activity.AssignmentForm;
import com.awesome.awesome.entity.Assignment;
import com.awesome.awesome.sql.SQLiteHelper;
import com.awesome.awesome.tab.MyFragment;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class MyRecyclerAdapter extends RecyclerView.Adapter<MyRecyclerAdapter.MyViewHolder> {

    private List<Assignment> items;
    private MyFragment fragment;

    public MyRecyclerAdapter(List<Assignment> items, MyFragment fragment) {
        this.items = items;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler, parent, false);

        return new MyViewHolder(view, fragment);
    }
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.assignmentID.setText(Integer.toString(items.get(position).getID()));
        holder.assignmentName.setText(items.get(position).getName());
        // 임시로 string으로 출력
        String endDateTime = items.get(position).getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        holder.assignmentEndDateTime.setText(endDateTime);
        holder.assignmentSubject.setText(items.get(position).getSubject());
        holder.assignmentStatus.setText(items.get(position).getStatus().toString());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView assignmentID;
        TextView assignmentName;
        TextView assignmentEndDateTime;
        TextView assignmentStatus;
        TextView assignmentSubject;
        Button modifyBtn;
        SQLiteHelper sqLiteHelper;
        private Spinner assignmentStatusSpinner;
        private MyFragment innerFragment;

        public MyViewHolder(@NonNull View itemView, MyFragment fragment) {
            super(itemView);
            assignmentID = itemView.findViewById(R.id.assignmentID);
            assignmentName = itemView.findViewById(R.id.listAssignmentName);
            assignmentEndDateTime = itemView.findViewById(R.id.listAssignmentDate);
            assignmentStatus = itemView.findViewById(R.id.listAssignmentStatus);
            assignmentSubject = itemView.findViewById(R.id.listAssignmentSubject);
            modifyBtn = itemView.findViewById(R.id.modifyBtn);
            innerFragment = fragment;

            modifyBtn = itemView.findViewById(R.id.modifyBtn);
            modifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(view.getContext(), AssignmentForm.class);

                    int id = Integer.parseInt(assignmentID.getText().toString());

                    sqLiteHelper = new SQLiteHelper(view.getContext());
                    Assignment assignment = sqLiteHelper.getAssignment(id);

                    if (assignment == null) {
                        System.out.println("Select Error!!!!!!!!!!!");
                    }

                    intent.putExtra("Assignment", assignment);

                    ((Activity)view.getContext()).startActivity(intent);
                }
            });

            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    // 클릭된 아이템의 ID를 가져옴
                    int id = Integer.parseInt(assignmentID.getText().toString());

                    sqLiteHelper = new SQLiteHelper(view.getContext());
                    Assignment assignment = sqLiteHelper.getAssignment(id);

                    if (assignment == null) {
                        System.out.println("Assignment not found!");
                        return;
                    }

                    // 팝업 다이얼로그 생성
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(view.getContext());

                    // 팝업 내에 표시할 레이아웃 설정
                    View dialogView = LayoutInflater.from(view.getContext()).inflate(R.layout.assignment_item, null);

                    // 팝업에 데이터 설정
                    TextView nameTextView = dialogView.findViewById(R.id.popupAssignmentName);
                    nameTextView.setText(assignment.getName());

                    // EndDateTime 포맷팅 (T 제거)
                    String formattedDateTime = assignment.getEndDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

                    TextView dateTextView = dialogView.findViewById(R.id.popupAssignmentDate);
                    dateTextView.setText(formattedDateTime);  // 포맷된 날짜 시간 표시

                    // 우선순위가 null이면 기본값 '미디엄'으로 설정
                    TextView priorityTextView = dialogView.findViewById(R.id.popupAssignmentPriority);
                    String priority = (assignment.getPriority() != null) ? assignment.getPriority().toString() : "MEDIUM";
                    priorityTextView.setText(priority);

                    // Spinner에서 상태를 설정하는 코드
                    assignmentStatusSpinner = dialogView.findViewById(R.id.assignmentStatusSpinner);

                    List<Status> statusList = List.of(Status.WAITING, Status.ONGOING, Status.COMPLETE);
                    ArrayAdapter<Status> adapter = new ArrayAdapter<>(view.getContext(), android.R.layout.simple_spinner_dropdown_item, statusList);
                    assignmentStatusSpinner.setAdapter(adapter);

                    // Assignment의 상태에 맞게 Spinner의 선택 상태를 설정
                    assignmentStatusSpinner.setSelection(assignment.getStatus().ordinal());

                    // Spinner의 선택 항목이 변경되었을 때 Assignment 객체의 상태를 업데이트
                    assignmentStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                            // Spinner에서 선택된 상태를 가져와서 Assignment 객체의 상태를 업데이트
                            Status selectedStatus = (Status) parentView.getItemAtPosition(position);
                            assignment.setStatus(selectedStatus);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parentView) {
                            // 아무것도 선택되지 않은 경우 처리 (선택된 상태는 그대로 두기)
                        }
                    });

                    builder.setView(dialogView)
                            .setPositiveButton("Confirm", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {

                                    // 수정된 상태를 반영한 Assignment 객체를 DB에 업데이트
                                    sqLiteHelper.updateAssignmentStatus(assignment); // DB에 상태 반영

                                    innerFragment.updateList();

                                    dialog.dismiss(); // 팝업을 닫음
                                }
                            })
                            .setNegativeButton("Cancel", new android.content.DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(android.content.DialogInterface dialog, int which) {
                                    dialog.dismiss(); // 팝업을 닫음
                                }
                            })
                            .show(); // 팝업 다이얼로그 띄우기

                }
            });
        }
    }
}