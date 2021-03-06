package com.example.sujit.celeganceapp.ContestantData;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.example.sujit.celeganceapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class Qualify extends Fragment implements View.OnClickListener {


    RecyclerView recyclerView;

    ContestantAdapter adapter;
    List<ContestantData> dataList= new ArrayList<>();;
    List<ContestantData> selection_list = new ArrayList<ContestantData>();
    Participant participant;
    int Type = 1;
    FirebaseAuth mAuth;
    FirebaseDatabase database;
    String eventTest;
    Iterator<DataSnapshot> dataSnapshotIterator;
    ContestantData contestantData;
    Context context;
    ProgressDialog dialog;
    FirebaseAuth getmAuth;
    String currentUserPhone;
    Button Qualify, disQualify;
    public Qualify() {
        mAuth = FirebaseAuth.getInstance();
        context = getContext();


        participant = (Participant) getContext();


        database = FirebaseDatabase.getInstance();
       getmAuth = FirebaseAuth.getInstance();
       FirebaseUser currentUser = getmAuth.getCurrentUser();
       currentUserPhone = currentUser.getPhoneNumber();
     //  Log.e("Current User PHone",currentUserPhone);
       // currentUserPhone="+917749836725";
        refresh();




    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {



        View rootView = inflater.inflate(R.layout.fragment_qualify, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        recyclerView = rootView.findViewById(R.id.recyclerView);

         Qualify = rootView.findViewById(R.id.qualify);
         disQualify = rootView.findViewById(R.id.diqualify);
        Qualify.setOnClickListener(this);
        disQualify.setOnClickListener(this);


        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        adapter = new ContestantAdapter(getContext(), dataList, Type);


        recyclerView.setAdapter(adapter);
        participant = (Participant) getContext();

        disable();

        return rootView;
    }

    public void prepareSelection(View view, int position) {
        if (((CheckBox) view).isChecked()) {
            selection_list.add(dataList.get(position));


            participant.UpdateCounter(1);

        } else {
            selection_list.remove(dataList.get(position));


            participant.UpdateCounter(0);
        }

    }

    public void showCandidateInfo() {
//        String phoneNum = mAuth.getCurrentUser().getPhoneNumber();
        final DatabaseReference databaseReference = database.getReference("Admins");
        Query query = databaseReference.orderByChild("phone").equalTo(currentUserPhone);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                while (dataSnapshotIterator.hasNext()) {
                    DataSnapshot admin = dataSnapshotIterator.next();

                    //Log.e("Event",admin.child("event").getValue().toString());
                    eventTest = admin.child("event").getValue().toString();
                    final DatabaseReference databaseReference1 = database.getReference("Events").child(admin.child("event").getValue().toString());
                    //  Query query1 = databaseReference1.orderByChild(admin.child("event").getValue().toString());
                    databaseReference1.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.e("Inside","Data Change");
                            Iterator<DataSnapshot> dataSnapshotIterator1 = dataSnapshot.getChildren().iterator();
                            while (dataSnapshotIterator1.hasNext())
                            {
                                DataSnapshot candidates = dataSnapshotIterator1.next();
                                if(candidates.child("qualify").getValue().toString().equals("1")&& search(candidates.child("regId").getValue().toString())) {
                                    contestantData = new ContestantData(candidates.child("name").getValue().toString(), candidates.child("regId").getValue().toString(), candidates.child("branch").getValue().toString(), candidates.child("phone").getValue().toString(), candidates.child("qualify").getValue().toString());
                                    dataList.add(contestantData);
                                    adapter.notifyDataSetChanged();
                                }

                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });


                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public boolean search(String reg) {
        for (ContestantData data : dataList) {
            if (data.getReg().equals(reg)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public void onClick(View view) {

                switch (view.getId()) {

                    case R.id.diqualify: {

                        dataList.clear();
                        adapter.notifyDataSetChanged();

                        Iterator<ContestantData> contestantDataIterator = selection_list.listIterator();
                        while (contestantDataIterator.hasNext()) {
                            DatabaseReference reference = database.getReference("Events").child(eventTest);
                            Query query = reference.orderByChild("regId").equalTo(contestantDataIterator.next().getReg());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.e("msg", "Update");
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        dataSnapshot1.getRef().child("qualify").setValue("0");

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                        participant.counter=0;
                        participant.UpdateCounter(4);
                        selection_list.clear();
                        disable();



                        break;

                    }
                    case R.id.qualify: {
                        dataList.removeAll(selection_list);
                        selection_list.clear();
                        selection_list.addAll(dataList);
                        dataList.clear();

                        adapter.notifyDataSetChanged();
                        Iterator<ContestantData> contestantDataIterator = selection_list.listIterator();
                        while (contestantDataIterator.hasNext()) {
                            DatabaseReference reference = database.getReference("Events").child(eventTest);
                            Query query = reference.orderByChild("regId").equalTo(contestantDataIterator.next().getReg());
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Log.e("msg", "Update");
                                    for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                        dataSnapshot1.getRef().child("qualify").setValue("0");

                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });


                        }
                        participant.counter=0;
                        selection_list.clear();
                        participant.UpdateCounter(3);
                        showCandidateInfo();

                        disable();




                        break;

                    }


                }


            }

    public void refresh()
    {
        final DatabaseReference databaseReference = database.getReference("Admins");
        Query query = databaseReference.orderByChild("phone").equalTo(currentUserPhone);

        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                dataSnapshotIterator = dataSnapshot.getChildren().iterator();
                while (dataSnapshotIterator.hasNext()) {
                    DataSnapshot admin = dataSnapshotIterator.next();


                    eventTest = admin.child("event").getValue().toString();
                    final DatabaseReference databaseReference1 = database.getReference("Events").child(admin.child("event").getValue().toString());
                    databaseReference1.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            showCandidateInfo();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }


    public void disable()
    {
        if(selection_list.size()==0)
        {
            Qualify.setVisibility(View.GONE);
            disQualify.setVisibility(View.GONE);
        }
        else
        {
            Qualify.setVisibility(View.VISIBLE);
            disQualify.setVisibility(View.VISIBLE);
        }
    }

    public void searchFilter(String newText)
    {
        List<ContestantData> newList = new ArrayList<>();

        for (ContestantData data : dataList) {
            String name = data.getName().toLowerCase();
            String branch = data.getBranch().toLowerCase();
            String reg =data.getReg().toLowerCase();
            if (name.contains(newText)||branch.contains(newText)||reg.contains(newText))
                newList.add(data);
        }
        adapter.setFilter(newList);
    }


}