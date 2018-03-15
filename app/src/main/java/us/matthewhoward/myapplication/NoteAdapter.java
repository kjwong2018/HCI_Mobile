package us.matthewhoward.myapplication;


import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class NoteAdapter extends CursorAdapter {
    public NoteAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    //Tell the adapter we want to use the note_list_item.xmll layout
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.note_list_item, viewGroup, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        ImageView noteImage = view.findViewById(R.id.noteImage);
        TextView noteText = view.findViewById(R.id.noteText);
        TextView noteCategory = view.findViewById(R.id.noteCategory);

        // Get the image from the database as a Bitmap
        String path = cursor.getString(cursor.getColumnIndexOrThrow("noteImage"));
        if(!path.equals("default")){
            Bitmap bitmap = BitmapFactory.decodeFile(path);
            if(bitmap == null){
                Toast.makeText(view.getContext(), "Image not found", Toast.LENGTH_SHORT).show();
            } else{
                noteImage.setImageBitmap(bitmap);
            }
        }else{
            noteImage.setImageResource(R.drawable.default_image);
        }


        // Get the note text from the database as a String
        String title = cursor.getString(cursor.getColumnIndexOrThrow("noteText"));

//        String description = cursor.getString(cursor.getColumnIndexOrThrow("noteDescription"));

        String category = cursor.getString(cursor.getColumnIndexOrThrow("noteCategory"));

        // Populate fields with properties from database
        noteText.setText(title);
        noteCategory.setText("Category: "+category);
    }
}