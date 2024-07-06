package com.example.myaddressbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private static final int REQUEST_IMAGE_PICK = 1;
    private ImageView contactImageView;
    private String currentImagePath;
    private SettingsFragment settingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply theme before setting content view
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int theme = prefs.getInt("THEME", R.id.radio_light);
        if (theme == R.id.radio_dark) {
            setTheme(R.style.AppTheme_Dark);
        } else if (theme == R.id.radio_elegant) {
            setTheme(R.style.AppTheme_Elegant);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back); // 确保有一个返回图标资源 ic_back
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            ContactListFragment fragment = new ContactListFragment();
            transaction.replace(R.id.fragment_container, fragment);
            transaction.commit();
        }

        db = new DatabaseHelper(this);

        FloatingActionButton fab = findViewById(R.id.fab_add_contact);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddEditContactPopup(-1);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyDisplayMode();
    }

    private void applyDisplayMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int displayMode = prefs.getInt("DISPLAY_MODE", R.id.radio_list);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
        if (currentFragment instanceof ContactListFragment) {
            ContactListFragment fragment = (ContactListFragment) currentFragment;
            fragment.setDisplayMode(displayMode);
            fragment.refreshContacts();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                ContactListFragment fragment = (ContactListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    fragment.filterContacts(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                ContactListFragment fragment = (ContactListFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
                if (fragment != null) {
                    fragment.filterContacts(newText);
                }
                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.action_settings) {
            loadSettingsFragment();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private void loadSettingsFragment() {
        showFloatingActionButton(false);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        settingsFragment = new SettingsFragment();
        transaction.replace(R.id.fragment_container, settingsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void openContactDetail(int contactId) {
        ContactDetailFragment fragment = ContactDetailFragment.newInstance(contactId);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void showAddEditContactPopup(int contactId) {
        View popupView = LayoutInflater.from(this).inflate(R.layout.popup_add_edit_contact, null);
        PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);

        EditText nameEditText = popupView.findViewById(R.id.name_edit_text);
        EditText phoneEditText = popupView.findViewById(R.id.phone_edit_text);
        EditText groupNameEditText = popupView.findViewById(R.id.group_name_edit_text);
        EditText nicknameEditText = popupView.findViewById(R.id.nickname_edit_text);
        contactImageView = popupView.findViewById(R.id.contact_image_view);
        Button selectImageButton = popupView.findViewById(R.id.select_image_button);
        Button saveContactButton = popupView.findViewById(R.id.save_contact_button);

        if (contactId != -1) {
            Contact contact = db.getContact(contactId);
            if (contact != null) {
                nameEditText.setText(contact.getName());
                phoneEditText.setText(contact.getPhoneNumber());
                groupNameEditText.setText(contact.getGroupName());
                nicknameEditText.setText(contact.getNickname());
                currentImagePath = contact.getImagePath();
                if (currentImagePath != null && !currentImagePath.isEmpty()) {
                    contactImageView.setImageURI(Uri.parse(currentImagePath));
                }
            }
        } else {
            // 设置默认图片路径
            currentImagePath = "android.resource://" + getPackageName() + "/" + R.drawable.default_contact;
            contactImageView.setImageResource(R.drawable.default_contact);
        }

        selectImageButton.setOnClickListener(v -> {
            Intent pickPhoto = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(pickPhoto, REQUEST_IMAGE_PICK);
        });

        saveContactButton.setOnClickListener(v -> {
            String name = nameEditText.getText().toString();
            String phone = phoneEditText.getText().toString();
            String groupName = groupNameEditText.getText().toString();
            String nickname = nicknameEditText.getText().toString();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
                Toast.makeText(MainActivity.this, "Please enter name and phone number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (contactId == -1) {
                // 添加
                Contact newContact = new Contact(name, phone);
                newContact.setGroupName(groupName);
                newContact.setNickname(nickname);
                newContact.setImagePath(currentImagePath);
                db.addContact(newContact);
                if (!db.getAllGroups().contains(groupName)) {
                    db.addGroup(groupName);

                    if (settingsFragment != null) {
                        settingsFragment.reloadGroupList();
                    }
                }
            } else {
                // 更新
                Contact contact = new Contact(name, phone);
                contact.setId(contactId);
                contact.setGroupName(groupName);
                contact.setNickname(nickname);
                contact.setImagePath(currentImagePath);
                db.updateContact(contact);
            }

            popupWindow.dismiss();
            // Refresh contact list
            ContactListFragment fragment = (ContactListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.fragment_container);
            if (fragment != null) {
                fragment.refreshContacts();
                fragment.setupGroupSpinner();
            }
        });

        popupWindow.showAtLocation(findViewById(R.id.fragment_container), android.view.Gravity.CENTER, 0, 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_IMAGE_PICK && data != null && data.getData() != null) {
                Uri selectedImage = data.getData();
                currentImagePath = selectedImage.toString();
                contactImageView.setImageURI(selectedImage);
            }
        }
    }

    public void showFloatingActionButton(boolean show) {
        FloatingActionButton fab = findViewById(R.id.fab_add_contact);
        if (show) {
            fab.show();
        } else {
            fab.hide();
        }
    }
    public void deleteContact(int contactId) {
        db.deleteContact(contactId);
        ContactListFragment fragment = (ContactListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_container);
        if (fragment != null) {
            fragment.refreshContacts();
        }
    }
}
