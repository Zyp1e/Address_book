package com.example.myaddressbook;

import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactDetailFragment extends Fragment {
    private TextView nameTextView;
    private TextView phoneTextView;
    private TextView nicknameTextView;
    private TextView groupNameTextView;
    private ImageView contactImageView;
    private int contactId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_detail, container, false);

        nameTextView = view.findViewById(R.id.name_text_view);
        phoneTextView = view.findViewById(R.id.phone_text_view);
        nicknameTextView = view.findViewById(R.id.nickname_text_view);
        groupNameTextView = view.findViewById(R.id.group_name_text_view);
        contactImageView = view.findViewById(R.id.contact_image_view);
        Button backButton = view.findViewById(R.id.back_button);

        if (getArguments() != null) {
            contactId = getArguments().getInt("CONTACT_ID", -1);
        }
        if (contactId != -1) {
            DatabaseHelper db = new DatabaseHelper(getContext());
            Contact contact = db.getContact(contactId);
            if (contact != null) {
                nameTextView.setText(contact.getName());
                phoneTextView.setText(contact.getPhoneNumber());
                nicknameTextView.setText(contact.getNickname());
                groupNameTextView.setText(contact.getGroupName());
                if (contact.getImagePath() != null && !contact.getImagePath().isEmpty()) {
                    contactImageView.setImageURI(Uri.parse(contact.getImagePath()));
                } else {
                    contactImageView.setImageResource(R.drawable.default_contact); // 设置默认图片
                }
            }
        }

        backButton.setOnClickListener(v -> getActivity().onBackPressed());

        return view;
    }

    public static ContactDetailFragment newInstance(int contactId) {
        ContactDetailFragment fragment = new ContactDetailFragment();
        Bundle args = new Bundle();
        args.putInt("CONTACT_ID", contactId);
        fragment.setArguments(args);
        return fragment;
    }
}