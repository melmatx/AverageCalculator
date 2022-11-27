package com.example.averagecalculator;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String PRESETS_FILE = "Presets.txt";
    public static final String myPREFERENCES = "UserPrefs";
    public static final String PREF_preset_count = "presetCount";
    public static final String PREF_subjects = "subjects";

    List<String> presetNames = new ArrayList<>();
    List<String[]> presetSubjects = new ArrayList<>();
    List<RadioButton> rbList = new ArrayList<>();
    int presetCount;

    SwitchMaterial sw_darkMode;
    ScrollView container;
    RadioGroup rg_allPresets;
    Button btn_addPreset, btn_editPreset, btn_removePresets, btn_continue;
    ActionBar actionBar;

    SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sw_darkMode = findViewById(R.id.sw_darkMode);
        container = findViewById(R.id.container);
        rg_allPresets = findViewById(R.id.rg_allPresets);
        btn_addPreset = findViewById(R.id.btn_addPreset);
        btn_editPreset = findViewById(R.id.btn_editPreset);
        btn_removePresets = findViewById(R.id.btn_removePresets);
        btn_continue = findViewById(R.id.btn_continue);
        actionBar = getSupportActionBar();

        sp = getSharedPreferences(myPREFERENCES, MODE_PRIVATE);
        presetCount = sp.getInt(PREF_preset_count, 0);

        switch (getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
            case Configuration.UI_MODE_NIGHT_YES:
                sw_darkMode.setChecked(true);
                break;
            case Configuration.UI_MODE_NIGHT_NO:
                sw_darkMode.setChecked(false);
                break;
        }

        sw_darkMode.setOnClickListener(v -> {
            if (sw_darkMode.isChecked()) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        if(actionBar != null) {
            actionBar.hide();
        }

        if(presetCount != 0) {
            container.setVisibility(View.VISIBLE);
            loadRadios();
        }

        btn_addPreset.setOnClickListener(v -> addPreset(null, null));

        btn_editPreset.setOnClickListener(v -> editPreset());

        btn_removePresets.setOnClickListener(v -> removePresets());

        btn_continue.setOnClickListener(v -> {
            if (rg_allPresets.getCheckedRadioButtonId() == -1) {
                Toast.makeText(this, "Please select a subject preset!", Toast.LENGTH_SHORT).show();
            }
            else {
                continueActivity();
            }
        });
    }

    public void restartMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public String readPresetsFile() {
        String fileContents = "";
        try {
            FileInputStream fis = openFileInput(PRESETS_FILE);
            int ctr;
            StringBuffer buffer = new StringBuffer();

            while ((ctr = fis.read()) != -1) {
                buffer = buffer.append((char)ctr);
            }
            fileContents = buffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileContents;
    }

    public void editPresetsFile(String data, int mode) {
        try {
            FileOutputStream fos = openFileOutput(PRESETS_FILE, mode);
            fos.write(data.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void loadRadios() {
        String fileContents = readPresetsFile();
        String[] lines = fileContents.split("/\n");

        for (String line : lines) {
            RadioButton rb_preset = new RadioButton(this);
            rb_preset.setText(line.split("%=")[0]);
            rb_preset.setOnClickListener(v -> btn_editPreset.setVisibility(View.VISIBLE));
            rg_allPresets.addView(rb_preset);
            rbList.add(rb_preset);

            presetNames.add(line.split("%=")[0]);
            presetSubjects.add(line.split("%=")[1].split("!@"));
        }
    }

    public void addPreset(String editPreset, @Nullable String[] subjects) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LinearLayout llContainer = new LinearLayout(this);
        llContainer.setOrientation(LinearLayout.VERTICAL);

        ScrollView scroll = new ScrollView(this);
        LinearLayout layout1 = new LinearLayout(this);
        layout1.setOrientation(LinearLayout.VERTICAL);
        layout1.setPadding(19, 0, 15, 10);

        LinearLayout layout2 = new LinearLayout(this);
        layout2.setOrientation(LinearLayout.VERTICAL);
        layout2.setPadding(19, 0, 15, 15);

        LinearLayout layout3 = new LinearLayout(this);
        layout3.setOrientation(LinearLayout.VERTICAL);
        layout3.setPadding(19, 10, 15, 15);

        final EditText presetBox = new EditText(this);
        presetBox.setHint("Enter Preset Name");
        presetBox.setHintTextColor(Color.GRAY);
        presetBox.setText(editPreset);
        presetBox.setSingleLine(true);
        presetBox.setMaxLines(1);
        presetBox.setLines(1);
        layout3.addView(presetBox);

        final TextView tv_noSubj = new TextView(this);
        tv_noSubj.setText("No subjects added. Please add using the button above.");
        tv_noSubj.setGravity(Gravity.CENTER);
        layout1.addView(tv_noSubj);

        List<EditText> etList = new ArrayList<>();
        if (subjects != null) {
            tv_noSubj.setVisibility(View.GONE);

            for (String subject : subjects) {
                createSubjectView(layout1, etList, tv_noSubj, subject);
            }
        }

        final Button addSubject = new Button(this);
        addSubject.setText("+ Add 1 Subject");
        addSubject.setOnClickListener(v -> {
            tv_noSubj.setVisibility(View.GONE);

            createSubjectView(layout1, etList, tv_noSubj, null);
        });
        layout2.addView(addSubject);

        scroll.addView(layout1);
        llContainer.addView(layout3);
        llContainer.addView(layout2);
        llContainer.addView(scroll);

        builder.setView(llContainer);
        builder.setTitle("Add New Subject Preset:");
        builder.setPositiveButton("Save", (dialog, which) -> {
            String presetName = presetBox.getText().toString();

            if (etList.size() == 0) {
                Toast.makeText(this, "Please enter at least one subject!", Toast.LENGTH_SHORT).show();
            }
            else {
                savePreset(presetName, editPreset, etList);
            }
        });
        builder.setNegativeButton("Cancel", null);
        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public void createSubjectView(LinearLayout layout1, List<EditText> etList, TextView tv_noSubj, String subject) {
        final LinearLayout subjectLayout = new LinearLayout(this);
        subjectLayout.setOrientation(LinearLayout.HORIZONTAL);
        subjectLayout.setPadding(19, 10, 15, 20);
        layout1.addView(subjectLayout);

        final TextView subjectLabel = new TextView(this);
        subjectLabel.setText("Subject :");
        subjectLayout.addView(subjectLabel);

        final EditText subjectBox = new EditText(this);
        subjectBox.setHint("Enter Subject Name");
        subjectBox.setHintTextColor(Color.GRAY);
        subjectBox.setText(subject);
        subjectBox.setSingleLine(true);
        subjectBox.setMaxLines(1);
        subjectBox.setLines(1);
        subjectBox.setWidth(200);
        subjectBox.requestFocus();
        subjectLayout.addView(subjectBox);

        final Button subjectRemove = new Button(MainActivity.this);
        subjectRemove.setText("Remove");
        subjectRemove.setOnClickListener(v -> {
            layout1.removeView(subjectLayout);
            etList.remove(subjectBox);
            if (etList.size() == 0) {
                tv_noSubj.setVisibility(View.VISIBLE);
            }
        });
        subjectLayout.addView(subjectRemove);

        etList.add(subjectBox);
    }

    public void savePreset(String presetName, String editPreset, List<EditText> etList) {
        StringBuilder sb = new StringBuilder();
        for(EditText newBox : etList) {
            String text = newBox.getText().toString();
            if(!text.equals("")) {
                sb.append(text).append("!@");
            }
        }
        String data = presetName + "%=" + sb.toString() + "/\n";
        if (editPreset != null) {
            String update = readPresetsFile();
            String[] lines = update.split("/\n");

            for (String line : lines) {
                if (line.split("%=")[0].equals(editPreset)) {
                    update = update.replace(line + "/\n", data);
                }
            }
            editPresetsFile(update, 0);
            Toast.makeText(this, "Preset Updated!", Toast.LENGTH_SHORT).show();
        }
        else {
            editPresetsFile(data, 32768);
            presetCount++;
            savePresentCount();
            Toast.makeText(this, "Preset Saved!", Toast.LENGTH_SHORT).show();
        }
        restartMain();
    }

    public void savePresentCount() {
        sp = getSharedPreferences(myPREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(PREF_preset_count, presetCount);
        editor.apply();
    }

    public void editPreset() {
        for(RadioButton rb_preset : rbList) {
            if(rb_preset.isChecked()) {
                String editPresetName = rb_preset.getText().toString();
                String[] subjects = getSubjects(rb_preset);

                addPreset(editPresetName, subjects);
            }
        }
    }

    public void removePresets() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(19, 20, 15, 20);

        List<CheckBox> cbList = new ArrayList<>();
        if(presetNames.size() == 0) {
            final TextView noPresets = new TextView(this);
            noPresets.setText("No subject presets available.");
            layout.addView(noPresets);
        }
        else {
            for (String presetName : presetNames) {
                final CheckBox newCb = new CheckBox(this);
                newCb.setText(presetName);
                layout.addView(newCb);

                cbList.add(newCb);
            }
        }
        builder.setView(layout);
        builder.setTitle("Please select presets to delete:");
        builder.setPositiveButton("OK", (dialog, which) -> {
            int cbChecked = 0;
            List<String> toDelete = new ArrayList<>();

            for(CheckBox newCb : cbList) {
                if(newCb.isChecked()) {
                    toDelete.add(newCb.getText().toString());
                    cbChecked++;
                }
            }

            if(presetNames.size() == 0) { //location could be revised?
                Toast.makeText(this, "Please add a preset!", Toast.LENGTH_SHORT).show();
            }

            else if (cbChecked == 0) {
                Toast.makeText(this, "No checkbox is checked!", Toast.LENGTH_SHORT).show();
            }

            else {
                removePresetsOnFile(toDelete);
            }
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void removePresetsOnFile(List<String> toDelete) {
        //Removing presets and saving changes to a file (data)
        String data = readPresetsFile();
        String[] lines = data.split("/\n");

        for (String line : lines) {
            for (String presets : toDelete) {
                if (line.split("%=")[0].equals(presets)) {
                    data = data.replace(line + "/\n", "");
                }
            }
        }
        //Applying changes
        editPresetsFile(data, 0);
        presetCount -= toDelete.size();
        for (String presets : toDelete) {
            int index = presetNames.indexOf(presets);
            presetNames.remove(index);
            presetSubjects.remove(index);
        }
        savePresentCount();
        restartMain();
    }

    public void continueActivity() {
        for(RadioButton rb_preset : rbList) {
            if(rb_preset.isChecked()) {
                String[] subjects = getSubjects(rb_preset);

                StringBuilder sb = new StringBuilder();
                for (String subj : subjects) {
                    sb.append(subj).append("!@");
                }

                sp = getSharedPreferences(myPREFERENCES, MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(PREF_subjects, sb.toString());
                editor.apply();

                enterQuarters();
            }
        }
    }

    public void enterQuarters() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(19, 10, 15, 20);

        final EditText qBox = new EditText(this);
        qBox.setHint("Enter quarters");
        qBox.setHintTextColor(Color.GRAY);
        qBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_NORMAL);
        layout.addView(qBox);

        final LinearLayout quarterLayout = new LinearLayout(this);
        quarterLayout.setOrientation(LinearLayout.HORIZONTAL);
        quarterLayout.setGravity(Gravity.CENTER);
        layout.addView(quarterLayout);

        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(5, 10, 5, 5);

        int value = 1;
        while (value <= 5) {
            createBtnNumber(qBox, quarterLayout, value + "");
            value++;
        }

        builder.setView(layout);
        builder.setTitle("How many quarters does each subject have?");
        builder.setPositiveButton("OK", (dialog, which) -> {
            int quarters = Integer.parseInt(qBox.getText().toString());
            openGradesActivity(quarters);
        });
        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public String[] getSubjects(RadioButton rb_preset) {
        String name = rb_preset.getText().toString();
        int index = presetNames.indexOf(name);

        return presetSubjects.get(index);
    }

    public void createBtnNumber(EditText qBox, LinearLayout quarterLayout, String value) {
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        buttonParams.setMargins(5, 10, 5, 5);

        final Button btn = new Button(this);
        btn.setText(value);
        btn.setOnClickListener(v -> qBox.setText(value));
        btn.setLayoutParams(buttonParams);
        quarterLayout.addView(btn);
    }

    public void openGradesActivity(int quarters) {
        Intent intent = new Intent(this, GradesActivity.class);
        intent.putExtra("quarters", quarters);
        startActivity(intent);
    }
}