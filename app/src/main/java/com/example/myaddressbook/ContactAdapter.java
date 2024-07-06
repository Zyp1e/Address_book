package com.example.myaddressbook;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> implements Filterable {
    private Context context;
    private List<Contact> contactList;
    private List<Contact> contactListFull;
    private OnItemClickListener listener;

    public ContactAdapter(Context context, List<Contact> contactList, OnItemClickListener listener) {
        this.context = context;
        this.contactList = contactList;
        this.contactListFull = new ArrayList<>(contactList);
        this.listener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(Contact contact);
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contact = contactList.get(position);
        holder.nameTextView.setText(contact.getName());
        holder.phoneTextView.setText(contact.getPhoneNumber());

        if (contact.getImagePath() != null && !contact.getImagePath().isEmpty()) {
            holder.contactImageView.setImageURI(Uri.parse(contact.getImagePath()));
        } else {
            holder.contactImageView.setImageResource(R.drawable.default_contact); // 设置默认图片
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(contact);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(context, v);
            popupMenu.inflate(R.menu.contact_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int currentPosition = holder.getAdapterPosition();
                if (currentPosition == RecyclerView.NO_POSITION) {
                    return false;
                }
                Contact currentContact = contactList.get(currentPosition);
                int itemId = item.getItemId();
                if (itemId == R.id.edit_contact) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).showAddEditContactPopup(currentContact.getId());
                    }
                    return true;
                } else if (itemId == R.id.delete_contact) {
                    if (context instanceof MainActivity) {
                        ((MainActivity) context).deleteContact(currentContact.getId());
                    }
                    return true;
                }
                return false;
            });
            popupMenu.show();
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return contactList.size();
    }

    public void updateContactList(List<Contact> newContactList) {
        contactList.clear();
        contactList.addAll(newContactList);
        contactListFull = new ArrayList<>(newContactList);
        notifyDataSetChanged();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                List<Contact> filteredList = new ArrayList<>();
                if (constraint == null || constraint.length() == 0) {
                    filteredList.addAll(contactListFull);
                } else {
                    String filterPattern = constraint.toString().toLowerCase().trim();
                    for (Contact contact : contactListFull) {
                        if (contact.getName().toLowerCase().contains(filterPattern)) {
                            filteredList.add(contact);
                        }
                    }
                }
                FilterResults results = new FilterResults();
                results.values = filteredList;
                return results;
            }
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                contactList.clear();
                contactList.addAll((List) results.values);
                notifyDataSetChanged();
            }
        };
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView phoneTextView;
        ImageView contactImageView;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            phoneTextView = itemView.findViewById(R.id.phone_text_view);
            contactImageView = itemView.findViewById(R.id.contact_image_view);
        }
    }
}
