package us.matthewhoward.myapplication;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {
    Cursor todoCursor;
    ListView noteList;
    NoteAdapter adapter;
    SwipeRefreshLayout mySwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        noteList = (ListView) findViewById(R.id.note_list);
        loadNotesFromDatabase();
        mySwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
        noteList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent openNote = new Intent(MainActivity.this, NoteActivity.class);
                openNote.putExtra("noteId", id);
//                Log.w("first", Long.toString(id));
                startActivity(openNote);
            }
        });
        noteList.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {

                        Cursor note = new NoteTakingDatabase(getApplicationContext()).getReadableDatabase()
                                .rawQuery("SELECT * FROM notes WHERE _id = " + id, null);



//                        System.exit(-1);
                        if(note.moveToFirst()){
                            Log.w("sql", note.getString(0));
                        }
//                        Log.w("what", Integer.toString(note.getColumnIndex("noteImage")));
//                        Log.w("picture?", note.getString(note.getColumnIndex("noteImage")));
                        String path = note.getString(note.getColumnIndexOrThrow("noteImage"));
//                        Bitmap bitmap = BitmapFactory.decodeFile(path);
                        if(!path.equals("default")){
                            Log.d("PATH", path);
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            if(bitmap == null){
                                Toast.makeText(getApplicationContext(), "Image not found", Toast.LENGTH_LONG).show();
                            } else{
                                showImage(bitmap);
                            }
                        }else{
                            showImage(null);
                        }
//                        showImage(bitmap);
                        return true;
                    }
                }
        );
        mySwipeRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
//                        Log.d("Refresh test", "test");
                        loadNotesFromDatabase();
                        mySwipeRefresh.setRefreshing(false);
                    }
                }
        );

        //Load preference.  If none found, we default to showing welcome screen
        SharedPreferences settings = getSharedPreferences(getString(R.string.pref_file_name), 0);
        String showWelcome = settings.getString(getString(R.string.pref_show), "true");

        //Load welcome screen if pref_show is set to true, or no value has been set
        if(showWelcome.equals("true")){
            showLocationDialog();
        }
    }

    public void showImage(Bitmap bitmap) {
        Dialog builder = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
        builder.requestWindowFeature(Window.FEATURE_NO_TITLE);
        builder.getWindow().setBackgroundDrawable(
                new ColorDrawable(android.graphics.Color.TRANSPARENT));
        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                //nothing;
            }
        });

        ImageView imageView = new ImageView(this);
        if(bitmap == null){
            imageView.setImageResource(R.drawable.default_image);
        } else {
            imageView.setImageBitmap(bitmap);
        }

        builder.addContentView(imageView, new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT));
        builder.show();
    }

    public boolean userHasPermission() {
        return ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
    }

    public void loadNotesFromDatabase() {
        if(! userHasPermission()){
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    0);
        }
        // Create a new instance of the NoteTakingDatabase
        NoteTakingDatabase handler = new NoteTakingDatabase(getApplicationContext());
        // Get the writable database
        SQLiteDatabase db;
        db = handler.getWritableDatabase();
        //Get all notes from the database
        todoCursor = db.rawQuery("SELECT * FROM notes", null);

        // Create an instance of the NoteAdapter with our cursor
        adapter = new NoteAdapter(this, todoCursor, 0);

        // Set the NoteAdapter to the ListView (display all notes from DB)
        noteList.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                    loadNotesFromDatabase();
                } else {
                    // TODO tell the user we need permission for our app to work
                }
                break;
            //hi
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Close database cursor
        if (todoCursor != null) {
            todoCursor.close();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_note:
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void showLocationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Welcome to NoteTaker");
        builder.setMessage("Here are a few tips to get you started!\n\n" +
                "1.  To add a note locate the Add Note button on the top right\n\n" +
                "2.  If you want to edit your note, simply click on it to make changes\n\n" +
                "3.  Swipe down to refresh your changes\n\n" +
                "4.  Enjoy!");

        String showAgain = getString(android.R.string.ok);
        builder.setPositiveButton(showAgain,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePreference("true");
                    }
                });

        String doNotShowAgain = getString(R.string.welcome_do_not_show_again);
        builder.setNegativeButton(doNotShowAgain,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        savePreference("false");
                    }
                });

        AlertDialog dialog = builder.create();
        // display dialog
        dialog.show();
    }

    //Saves preference to show the welcome message on startup
    private void savePreference(String value){
        SharedPreferences settings = getSharedPreferences(getString(R.string.pref_file_name), 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("show", value);
        editor.commit();
    }

}
