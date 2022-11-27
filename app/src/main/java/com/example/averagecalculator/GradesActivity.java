package com.example.averagecalculator;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class GradesActivity extends AppCompatActivity {

    List<List<EditText>> allEtList = new ArrayList<>();

    String[] subjects;
    int quarters;

    TableLayout gradesTable;
    Button btn_subjAve, btn_allAve;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grades);

        gradesTable = findViewById(R.id.gradesTable);
        btn_subjAve = findViewById(R.id.btn_subjAve);
        btn_allAve = findViewById(R.id.btn_allAve);

        sp = getSharedPreferences(MainActivity.myPREFERENCES, MODE_PRIVATE);
        subjects = (sp.getString(MainActivity.PREF_subjects, "")).split("!@");

        quarters = getIntent().getIntExtra("quarters", 0);

        loadHeadings();
        loadSubjects();

        btn_subjAve.setOnClickListener(v -> subjAve());

        btn_allAve.setOnClickListener(v -> allAve());
    }

    public void loadHeadings() {
        TableRow headingRow = new TableRow(this);
        headingRow.setPadding(20, 20, 20, 20);

        TextView subjectHead = new TextView(this);
        subjectHead.setText("Subjects:");
        subjectHead.setTypeface(null, Typeface.BOLD);
        subjectHead.setPadding(0,0,10,0);
        headingRow.addView(subjectHead);

        for (int i = 1; i <= quarters; i++) {
            TextView quarterHead = new TextView(this);
            quarterHead.setText("Q" + i);
            quarterHead.setTypeface(null, Typeface.BOLD);
            quarterHead.setGravity(Gravity.CENTER);
            headingRow.addView(quarterHead);
        }
        gradesTable.addView(headingRow);
    }

    public void loadSubjects() {
        for (String subject : subjects) {
            TableRow subjectRow = new TableRow(this);
            subjectRow.setPadding(20, 20, 20, 20);

            TextView subjectLabel = new TextView(this);
            subjectLabel.setText(subject);
            subjectLabel.setPadding(0,0,50,0);
            subjectRow.addView(subjectLabel);

            loadQuarters(subjectRow);
            gradesTable.addView(subjectRow);
        }
    }

    public void loadQuarters(TableRow subjectRow) {
        List<EditText> etList = new ArrayList<>();
        for (int i = 0; i < quarters; i++) {
            EditText gradeText = new EditText(this);
            gradeText.setSingleLine(true);
            gradeText.setMaxLines(1);
            gradeText.setLines(1);
            gradeText.setWidth(150);
            gradeText.setGravity(Gravity.CENTER);
            gradeText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

            subjectRow.addView(gradeText);
            etList.add(gradeText);
        }
        allEtList.add(etList);
        allEtList.get(0).get(0).requestFocus();
    }

    public double calculateSubjAve(List<EditText> etList) throws Exception {
        double sum = 0;
        for (EditText et : etList) {
            if (et.getText().toString().equals("")) {
                Toast.makeText(this, "Empty field detected!", Toast.LENGTH_SHORT).show();
                throw new Exception();
            }
            else {
                double grade = Double.parseDouble(et.getText().toString());
                sum += grade;
            }
        }

        return sum / etList.size();
    }

    public void subjAve() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a subject:");
        builder.setItems(subjects, (dialog, which) -> {
            List<EditText> etList = allEtList.get(which);
            try {
                double average = calculateSubjAve(etList);  //will throw an exception when empty field detected
                Toast.makeText(this, "Your average in " + subjects[which] + " is " + Math.round(average) + " (" + average + ")", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.create().show();
    }

    public List<Double> calculateAllAve() throws Exception {
        List<Double> allAve = new ArrayList<>();
        double allSum = 0;

        //Add each subject average to list
        for (List<EditText> etList : allEtList) {
            double subjAverage = calculateSubjAve(etList);
            allAve.add(subjAverage);
            allSum += subjAverage;
        }

        //Compute Final Average
        double finalAve = allSum / allEtList.size();
        allAve.add(finalAve);

        return allAve;
    }

    public void allAve() {
        List<Double> allAve = new ArrayList<>();
        double finalAve = 0;
        try {
            allAve = calculateAllAve(); //will throw an exception when empty field detected
            finalAve = allAve.get(allAve.size() - 1);
            allAve.remove(allAve.size() - 1);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(19, 10, 15, 10);

        //Show each subject average
        int subjCount = 0;
        for (Double average : allAve) {
            final TextView tv_subjAve = new TextView(this);
            tv_subjAve.setText(subjects[subjCount] + " - " + average);
            layout.addView(tv_subjAve);
            subjCount++;
        }

        //Show final average
        final TextView tv_final = new TextView(this);
        tv_final.setText("Your final average is " + Math.round(finalAve) + " (" + finalAve + ")");
        tv_final.setPadding(0,10,0,0);
        layout.addView(tv_final);

        builder.setView(layout);
        builder.setTitle("Here are all the averages:");
        builder.setPositiveButton("OK", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}