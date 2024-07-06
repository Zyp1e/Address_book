package com.example.myaddressbook;

import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactListFragment extends Fragment {
    private RecyclerView recyclerView;
    private ContactAdapter contactAdapter;
    private List<Contact> contactList;
    private List<Contact> fullContactList;  // 完整的联系人列表
    private DatabaseHelper dbHelper;
    private GroupItemDecoration groupItemDecoration;
    private Spinner groupSpinner;
    private List<String> groupList;
    private LetterNavigationView letterNavigationView;
    private int displayMode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contact_list, container, false);
        recyclerView = view.findViewById(R.id.recycler_view);
        letterNavigationView = view.findViewById(R.id.letter_navigation_view);
        groupSpinner = view.findViewById(R.id.group_spinner);
        dbHelper = new DatabaseHelper(getContext());

        fullContactList = dbHelper.getSortedContacts();  // 获取已排序的联系人列表
        contactList = new ArrayList<>(fullContactList);
        contactAdapter = new ContactAdapter(getContext(), contactList, contact -> {
            // 在这里处理点击事件，比如打开联系人详情
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).openContactDetail(contact.getId());
            }
        });

        recyclerView.setAdapter(contactAdapter);

        groupItemDecoration = new GroupItemDecoration();
        recyclerView.addItemDecoration(groupItemDecoration);

        List<String> letters = new ArrayList<>();
        for (char c = 'A'; c <= 'Z'; c++) {
            letters.add(String.valueOf(c));
        }

        letterNavigationView.setLetters(letters);
        letterNavigationView.setOnLetterSelectedListener(letter -> {
            int position = getPositionForSection(letter.charAt(0));
            if (position != -1) {
                recyclerView.scrollToPosition(position);
            }
        });

        setupGroupSpinner();   // 初始化分组下拉菜单

        return view;
    }

    public void setupGroupSpinner() {
        groupList = new ArrayList<>();
        groupList.add("All Groups"); // 添加默认选项
        Map<String, Integer> groupPositionMap = new HashMap<>();

        for (int i = 0; i < fullContactList.size(); i++) {
            String group = fullContactList.get(i).getGroupName();
            if (!groupPositionMap.containsKey(group)) {
                groupPositionMap.put(group, i);
                groupList.add(group);
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, groupList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        groupSpinner.setAdapter(adapter);

        groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGroup = groupList.get(position);
                filterContactsByGroup(selectedGroup);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });
    }

    private void filterContactsByGroup(String group) {
        if (group.equals("All Groups")) {
            contactList.clear();
            contactList.addAll(dbHelper.getAllContactsSortedByName()); // 直接按名字排序获取所有联系人
            if (displayMode == R.id.radio_list) {
                updateGroupHeaders(true); // 仅在列表视图模式下使用姓名首字母作为分组头
            } else {
                updateGroupHeaders(false); // 在卡片视图模式下不使用分组头
            }
        } else {
            contactList.clear();
            for (Contact contact : fullContactList) {
                if (contact.getGroupName().equals(group)) {
                    contactList.add(contact);
                }
            }
            updateGroupHeaders(false); // 使用组名作为分组头
        }
        contactAdapter.notifyDataSetChanged();
    }

    private int getPositionForSection(char section) {
        for (int i = 0; i < contactList.size(); i++) {
            if (contactList.get(i).getName().toUpperCase().charAt(0) == section) {
                return i;
            }
        }
        return -1;
    }

    public void refreshContacts() {
        fullContactList.clear();
        fullContactList.addAll(dbHelper.getSortedContacts());
        filterContactsByGroup(groupSpinner.getSelectedItem().toString());  // 按当前分组过滤
    }

    private void updateGroupHeaders(boolean isAllGroups) {
        Map<Integer, String> headers = new HashMap<>();
        String lastHeader = null;
        for (int i = 0; i < contactList.size(); i++) {
            String header = isAllGroups ? contactList.get(i).getName().substring(0, 1).toUpperCase() : contactList.get(i).getGroupName();
            if (!header.equals(lastHeader)) {
                headers.put(i, header);
                lastHeader = header;
            }
        }
        groupItemDecoration.setGroupHeaders(headers);
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public void setDisplayMode(int mode) {
        this.displayMode = mode;  // 保存当前显示模式
        RecyclerView.LayoutManager layoutManager;
        if (mode == R.id.radio_card) {
            layoutManager = new GridLayoutManager(getContext(), 2);
            recyclerView.removeItemDecoration(groupItemDecoration); // 移除分组装饰
        } else {
            layoutManager = new LinearLayoutManager(getContext());
            recyclerView.addItemDecoration(groupItemDecoration); // 添加分组装饰
        }
        recyclerView.setLayoutManager(layoutManager);
        filterContactsByGroup(groupSpinner.getSelectedItem().toString()); // 更新分组显示
    }

    public void filterContacts(String query) {
        contactAdapter.getFilter().filter(query);
    }

    public void applyDisplayMode() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int displayMode = prefs.getInt("DISPLAY_MODE", R.id.radio_list);  // 默认为列表视图
        setDisplayMode(displayMode);  // 应用当前显示模式
    }

    @Override
    public void onResume() {
        super.onResume();
        applyDisplayMode();  // 确保从设置返回时更新布局
    }
}
