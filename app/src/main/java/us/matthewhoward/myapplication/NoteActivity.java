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
    Button deleteNote;
    TextView noteTitle;
    TextView noteDescription;
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
        deleteNote = findViewById(R.id.delete_note);
        noteTitle = findViewById(R.id.note_title);
        noteImage = findViewById(R.id.note_image);
        noteDescription = findViewById(R.id.note_description);


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
                if(category.equals("Select Category") && noteTitle.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Title and Category missing", Toast.LENGTH_SHORT).show();
                }
                else if(category.equals("Select Category")){
                    Toast.makeText(getApplicationContext(), "Please select a category", Toast.LENGTH_SHORT).show();
                }
                else if(noteTitle.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Title is missing", Toast.LENGTH_SHORT).show();
                }
                else{
                    if(imagePath.isEmpty()){
                        imagePath ="default";
                    }
                    if (!isUpdate) {
                        storeNote(imagePath, noteTitle.getText().toString(), noteDescription.getText().toString(), category);
                    } else {
                        updateNote(noteId, imagePath, noteTitle.getText().toString(), noteDescription.getText().toString(), category);
                    }
                    finish();
                }
            }
        });

        deleteNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteNote(noteId);
                finish();
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

        String noteMessage = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));
        noteDescription.setText(noteMessage);

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
    public void deleteNote(int id){
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        SQLiteDatabase db = handler.getWritableDatabase();
        boolean success = handler.deleteNote(db,id);
        if(!success){
            Toast.makeText(getApplicationContext(), "Cannot delete a new entry", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getApplicationContext(), "Successfully deleted entry", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        EasyImage.handleActivityResult(requestCode, resultCode, data, this, new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                // TODO error stuff
            }

            @Override
            public void onImagePicked(File imageFile, EasyImage.ImageSource source, int type) {
                imagePath = imageFile.getAbsolutePath();
                Bitmap imageBitmap = BitmapFactory.decodeFile(imagePath);
                noteImage.setImageBitmap(imageBitmap);
            }
        });
    }
}
