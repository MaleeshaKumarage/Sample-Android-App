package com.example.sampleapplication;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.sampleapplication.databinding.ActivityMainBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opencsv.CSVWriter;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@RequiresApi(api = Build.VERSION_CODES.R)
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    EditText param1_Value;
    EditText param2_Value;
    EditText param3_Value;
    EditText param4_Value;
    EditText param5_Value;
    EditText param6_Value;
    String formattedDate;
    Random random = new Random();
    int ListLength;
            //String csv = "/storage/emulated/0/Download/A/MyCsvFile.csv";
    String csv = Environment.getExternalStorageDirectory().getAbsolutePath()+"/Download/A/MyCsvFile.csv";
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        getCurrentDate();
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //Adding all Permission to Application (Will only work with SKD 30 +)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(permissionIntent);
            }
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.navView, navController);

        //Initialize the Buttons
        //Save Data Button
        Button sendbutton = (Button) findViewById(R.id.button_send);
        //generate file Button
        Button filegeneratebutton = (Button) findViewById(R.id.FileGenerate_Button);

        param1_Value = findViewById(R.id.Param_1);
        param2_Value = findViewById(R.id.Param_2);
        param3_Value = findViewById(R.id.Param_3);
        param4_Value = findViewById(R.id.Param_4);
        param5_Value = findViewById(R.id.Param_5);
        param6_Value = findViewById(R.id.Param_6);
        //Set Random values to keep the process easy - Remove once the actual implementation is done
        setRandomValues();
        //set file path to text view to find the file location
        TextView file_path_Value = findViewById(R.id.file_Path);
        file_path_Value.setText("File will be stored at "+csv);

        //set onclick listner to save data button
        sendbutton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(View v)
            {//once that button click data in those files will be assign to string variables
                String param1_Value_str = param1_Value.getText().toString();
                String param2_Value_str = param2_Value.getText().toString();
                String param3_Value_str = param3_Value.getText().toString();
                String param4_Value_str = param4_Value.getText().toString();
                String param5_Value_str = param5_Value.getText().toString();
                String param6_Value_str = param6_Value.getText().toString();

                //Firebase connection
                //Initial Connection Shoculd be done via Tool > Firebase
                //Database Instance
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                //Referance to data set
                //Data is stored like this
                // XXXXXX
                //    - ResearchData
                //          - 21-10-2021 --------> Child(formattedDate)
                //               - Parameter 1   --------> Child(Parameter 1)
                //               - Parameter 2   --------> Child(Parameter 2)
                //               - Parameter 3
                //               - Parameter 4
                //               - Parameter 5
                //               - Parameter 6
                //          - 22-10-2021 --------> Child(formattedDate)
                //               - Parameter 1  --------> Child(Parameter 1)
                //               - Parameter 2  --------> Child(Parameter 2)
                //               - Parameter 3
                //               - Parameter 4
                //               - Parameter 5
                //               - Parameter 6

                DatabaseReference myRef = database.getReference("ResearchData");
                try {
                    myRef.child(formattedDate).child("Parameter 1").push().setValue(param1_Value_str);
                    myRef.child(formattedDate).child("Parameter 2").push().setValue(param2_Value_str);
                    myRef.child(formattedDate).child("Parameter 3").push().setValue(param3_Value_str);
                    myRef.child(formattedDate).child("Parameter 4").push().setValue(param4_Value_str);
                    myRef.child(formattedDate).child("Parameter 5").push().setValue(param5_Value_str);
                    myRef.child(formattedDate).child("Parameter 6").push().setValue(param6_Value_str);
                    setRandomValues();

                }catch(Exception e){
                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
                }

            }
        });
        //Set onClick Listner to Generate File Button
        filegeneratebutton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                FirebaseDatabase database = FirebaseDatabase.getInstance();
                database.getReference("ResearchData/").getRef().addValueEventListener(

                        new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                                List<String> param1 = new ArrayList<String>();
                                List<String> param2 = new ArrayList<String>();
                                List<String> param3 = new ArrayList<String>();
                                List<String> param4 = new ArrayList<String>();
                                List<String> param5 = new ArrayList<String>();
                                List<String> param6 = new ArrayList<String>();
                                List<String> datelist = new ArrayList<String>();
                                //Adding headers to be printed in CSV FILE
                                param1.add("Parameter 1");
                                param2.add("Parameter 2");
                                param3.add("Parameter 3");
                                param4.add("Parameter 4");
                                param5.add("Parameter 5");
                                param6.add("Parameter 6");
                                datelist.add("Date");


                                List<String[]> data = new ArrayList<String[]>();
                                //Snapshot valiable contain all the child data under "ResearchData" branch
                                for (DataSnapshot dateSnapshot : snapshot.getChildren()) {
                                    //This is used to get the inner child of each child - This will give "Date" List Child Component

                                    for (DataSnapshot postSnapshot : dateSnapshot.getChildren()) {
                                        //This is used to get the inner child of each child - This will give "Parameter- XX" List Child Component
                                        for (DataSnapshot childValue : postSnapshot.getChildren()) {
                                            //This is used to get the inner child of each child - This will give each data inside each parameter

                                            switch (postSnapshot.getKey()) {

                                                case "Parameter 1":
                                                    //    Toast.makeText(getApplicationContext(), "Param 1"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param1.add(childValue.getValue().toString());
                                                    //Toast.makeText(getApplicationContext(), dateSnapshot.getKey(), Toast.LENGTH_SHORT).show();
                                                    datelist.add(dateSnapshot.getKey());
                                                    break;
                                                case "Parameter 2":
                                                    //    Toast.makeText(getApplicationContext(), "Param 2"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param2.add(childValue.getValue().toString());

                                                    break;
                                                case "Parameter 3":
                                                    //    Toast.makeText(getApplicationContext(), "Param 2"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param3.add(childValue.getValue().toString());

                                                    break;
                                                case "Parameter 4":
                                                    //    Toast.makeText(getApplicationContext(), "Param 2"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param4.add(childValue.getValue().toString());

                                                    break;
                                                case "Parameter 5":
                                                    //    Toast.makeText(getApplicationContext(), "Param 2"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param5.add(childValue.getValue().toString());

                                                    break;
                                                case "Parameter 6":
                                                    //    Toast.makeText(getApplicationContext(), "Param 2"+" "+childValue.getValue().toString(), Toast.LENGTH_SHORT).show();
                                                    param6.add(childValue.getValue().toString());

                                                    break;

                                                default:
                                                    Toast.makeText(getApplicationContext(), "Unknown", Toast.LENGTH_SHORT).show();
                                                    break;

                                            }
                                        }
                                    }
                                }


                                try{
                                    ListLength = param6.size();
                                //Data will be stored to 7 dimention arry to write in CSV File -7 dimentions (6 for 6 parameters and 1 for date)
                                for (int i = 0; i < ListLength; i++) {
                                      data.add(new String[]{datelist.get(i),param1.get(i),param2.get(i),param3.get(i),param4.get(i),param5.get(i),param6.get(i)});
                                }

                                }catch(Exception e){
                                    ListLength = param6.size();
                                    Toast.makeText(getApplicationContext(),"PLEASE WAIT UNTIL SAVING OPERATION FINISHED", Toast.LENGTH_SHORT).show();
                                }
                                    try{
                                CSVWriter writer = null;

                                    //Creating CSV FILE
                                    writer = new CSVWriter(new FileWriter(csv));

                                    writer.writeAll(data); // data is adding to csv
                                    data.clear();

                                    writer.close();




                                } catch (IOException e) {
                                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                                }catch (Exception e){
                                    Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull @NotNull DatabaseError error) {

                            }
                        }
                );

                Toast.makeText(getApplicationContext(),"CSV FILE SUCCESSFULLY CREATED",Toast.LENGTH_SHORT).show();


            }


        });
    }

    protected void getCurrentDate(){
        Date c = Calendar.getInstance().getTime();
        System.out.println("Current time => " + c);

        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        formattedDate = df.format(c);

    }
    protected void setRandomValues(){
        param1_Value.setText(Float.toString(random.nextFloat()));
        param2_Value.setText(Float.toString(random.nextFloat()));
        param3_Value.setText(Float.toString(random.nextFloat()));
        param4_Value.setText(Float.toString(random.nextFloat()));
        param5_Value.setText(Float.toString(random.nextFloat()));
        param6_Value.setText(Float.toString(random.nextFloat()));
    }

    protected void sendEmail() {
        Log.i("Send email", "");

        String[] TO = {"contactmaleesha93@gmail.com"};
        String[] CC = {"maleesha.msk@gmail.com"};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");


        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Your subject");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Email message goes here");
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        File file = new File(csv);
        //Uri uri = FileProvider.getUriForFile(this,BuildConfig.APPLICATION_ID + "." + getLocalClassName() + ".provider",file);
        Uri uri = Uri.fromFile(file);
        emailIntent.setDataAndType(uri, "text/csv");
        emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
            finish();

        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(MainActivity.this,
                    "There is no email client installed.", Toast.LENGTH_SHORT).show();
        }
    }
}