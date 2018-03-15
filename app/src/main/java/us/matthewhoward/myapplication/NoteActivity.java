package us.matthewhoward.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class NoteActivity extends AppCompatActivity {

    String imagePath = "";
    String category = "";
    Button saveNote;
    TextView noteTitle;
    ImageView noteImage;

    SQLiteDatabase db;

    boolean isUpdate = false;
    int noteId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        db = handler.getReadableDatabase();
        saveNote = findViewById(R.id.create_note);
        noteTitle = findViewById(R.id.note_title);
        noteImage = findViewById(R.id.note_image);


        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isUpdate = true;
            noteId = (int) extras.getLong("noteId");
            setNote(noteId);
        }
        else{
            noteImage.setImageResource(R.drawable.default_image);
        }
        Spinner spinner = (Spinner) findViewById(R.id.category_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.category_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        if(!category.isEmpty()){
            int spinnerPos = adapter.getPosition(category);
            spinner.setSelection(spinnerPos);
        }

        saveNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!category.equals("Select Category")){
                    if(imagePath.isEmpty()){
                        imagePath ="default";
                    }
                    if (!isUpdate) {
                        storeNote(imagePath, noteTitle.getText().toString(), "Description", category);
                    } else {
                        updateNote(noteId, imagePath, noteTitle.getText().toString(), "Description", category);
                    }
                    finish();
                }
                else{
                    Toast.makeText(getApplicationContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                }
            }
        });

        noteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Use the EasyImage library to open up a chooser to pick an image.
                EasyImage.openChooserWithGallery(NoteActivity.this, "Choose an Image", 0);
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long l) {
                category = parent.getItemAtPosition(i).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }


    private void updateNote(int noteId, String imagePath, String title, String description, String category) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.updateNote(db, noteId, imagePath, title, description, category);
    }

    private void setNote(Integer noteId) {
        // Get note by id
        Cursor cursor = db.rawQuery("SELECT * FROM notes WHERE _id = " + noteId, null);
        cursor.moveToFirst();

        // Set note details to view
        String path = cursor.getString(cursor.getColumnIndexOrThrow("noteImage"));
        if(!path.equals("default")){
            Log.d("PATH", path);
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if(bitmap == null){
                Toast.makeText(getApplicationContext(), "Image not found", Toast.LENGTH_LONG).show();
            } else{
                noteImage.setImageBitmap(bitmap);
                imagePath = path;
            }
        }else{
            noteImage.setImageResource(R.drawable.default_image);
        }
        // Get the note text from the database as a String
        String noteText = cursor.getString(cursor.getColumnIndexOrThrow("noteText"));
        noteTitle.setText(noteText);

        String noteDescription = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));
        category = cursor.getString(cursor.getColumnIndexOrThrow("noteCategory"));



        cursor.close();
    }

    public void storeNote(String path, String title, String description, String category) {
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db = handler.getWritableDatabase();
        // Store the note in the database
        handler.storeNote(db, path, title, description, category);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                // TODO error stuff
                Log.d("Image Error", "Error handling suck");
                e.printStackTrace();
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                imagePath = imageFile.getAbsolutePath();
                Log.d("PATHpick", imagePath);
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
                noteImage.setImageBitmap(imageBitmap);
            }
        });
    }
}
