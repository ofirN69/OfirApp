package com.ofir.ofirapp.screens;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ofir.ofirapp.R;
import com.ofir.ofirapp.screens.AfterLogPage;
import com.ofir.ofirapp.screens.MyEvents;
import com.ofir.ofirapp.screens.AccPage;
import com.ofir.ofirapp.screens.addEVENT;

public class BaseActivity extends AppCompatActivity {

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_home) {
            startActivity(new Intent(this, AfterLogPage.class));
            return true;
        } else if (itemId == R.id.nav_my_events) {
            startActivity(new Intent(this, MyEvents.class));
            return true;
        } else if (itemId == R.id.nav_add_event) {
            startActivity(new Intent(this, addEVENT.class));
            return true;
        } else if (itemId == R.id.nav_account) {
            startActivity(new Intent(this, AccPage.class));
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }

    protected void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }
} 