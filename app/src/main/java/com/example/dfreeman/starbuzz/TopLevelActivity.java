package com.example.dfreeman.starbuzz;

import android.content.Intent;
import android.database.Cursor;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteException;
import net.sqlcipher.database.SQLiteOpenHelper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.view.View;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import java.io.File;

public class TopLevelActivity extends AppCompatActivity {

    private SQLiteDatabase db;
    private Cursor favoritesCursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //this.deleteDatabase("starbuzz");
        SQLiteDatabase.loadLibs(this);
        File databaseFile = getDatabasePath("starbuzz");
        databaseFile.mkdirs();
        db = SQLiteDatabase.openOrCreateDatabase(databaseFile, "password", null);
        db.close();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_level);

        AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
                if(position == 0) {
                    Intent intent = new Intent(TopLevelActivity.this, DrinkCategoryActivity.class);
                    startActivity(intent);
                }
            }
        };
        ListView listView = (ListView) findViewById(R.id.list_options);
        listView.setOnItemClickListener(itemClickListener);

        //Populate list_fav
        ListView listFavorites = (ListView)findViewById(R.id.list_favorites);
        try {
            SQLiteOpenHelper starbuzzDatabaseHelper = new StarbuzzDatabaseHelper(this);
            db = starbuzzDatabaseHelper.getReadableDatabase("password");
            favoritesCursor = db.query("DRINK",
                    new String[] { "_id", "NAME"},
                    "FAVORITE = 1",
                    null, null, null, null);
            CursorAdapter favoriteAdapter = new SimpleCursorAdapter(TopLevelActivity.this,
                    android.R.layout.simple_list_item_1,
                    favoritesCursor,
                    new String[]{"NAME"},
                    new int[]{android.R.id.text1}, 0);
            listFavorites.setAdapter(favoriteAdapter);
            db.close();
        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }

        //Navigate to drink activity if a drink is clicked
        listFavorites.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> listView, View v, int position, long id) {
                Intent intent = new Intent(TopLevelActivity.this, DrinkActivity.class);
                intent.putExtra(DrinkActivity.EXTRA_DRINKNO, (int)id);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        favoritesCursor.close();
        db.close();
    }

    public void onRestart() {
        super.onRestart();
        try {
            StarbuzzDatabaseHelper starbuzzDatabaseHelper = new StarbuzzDatabaseHelper(this);
            db = starbuzzDatabaseHelper.getReadableDatabase("password");
            Cursor newCursor = db.query("DRINK",
                    new String[] {"_id", "NAME"},
                    "FAVORITE = 1",
                    null, null, null, null);
            ListView listFavorites = (ListView)findViewById(R.id.list_favorites);
            CursorAdapter adapter = (CursorAdapter)listFavorites.getAdapter();
            adapter.changeCursor(newCursor);
            favoritesCursor = newCursor;
            db.close();
        } catch(SQLiteException e) {
            Toast toast = Toast.makeText(this, "Database unavailable", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
