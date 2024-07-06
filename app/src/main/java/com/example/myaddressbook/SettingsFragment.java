package com.example.myaddressbook;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private RadioGroup displayModeGroup;
    private RadioGroup themeGroup;
    private ListView groupListView;
    private Button addGroupButton;
    private ArrayAdapter<String> groupListAdapter;
    private DatabaseHelper dbHelper;
    private List<String> groupList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        displayModeGroup = view.findViewById(R.id.display_mode_group);
        themeGroup = view.findViewById(R.id.theme_group);
        groupListView = view.findViewById(R.id.group_list_view);
        addGroupButton = view.findViewById(R.id.add_group_button);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        int displayMode = prefs.getInt("DISPLAY_MODE", R.id.radio_list);
        int theme = prefs.getInt("THEME", R.id.radio_light);

        displayModeGroup.check(displayMode);
        themeGroup.check(theme);

        displayModeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("DISPLAY_MODE", checkedId);
            editor.apply();
            getActivity().onBackPressed(); // 返回上一个界面
        });

        themeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("THEME", checkedId);
            editor.apply();
            getActivity().recreate(); // 重新创建活动以应用新主题
        });

        dbHelper = new DatabaseHelper(getContext());
        groupList = new ArrayList<>(); // 初始化groupList

        groupListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, groupList);
        groupListView.setAdapter(groupListAdapter);

        addGroupButton.setOnClickListener(v -> showAddGroupDialog());

        groupListView.setOnItemClickListener((parent, view1, position, id) -> {
            String groupName = groupList.get(position);
            showEditDeleteGroupDialog(groupName);
        });

        reloadGroupList();  // 初始化组信息

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFloatingActionButton(false); // 隐藏浮动按钮
        }
        // 重新加载组信息
        reloadGroupList();
    }

    void reloadGroupList() {
        if (groupList != null) {
            groupList.clear();
            groupList.addAll(dbHelper.getAllGroups());
            Collections.sort(groupList);  // 对组列表进行排序
            if (groupListAdapter != null) {
                groupListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).showFloatingActionButton(true); // 显示浮动按钮
        }
    }

    private void showAddGroupDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Group");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_group, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        Button okButton = viewInflated.findViewById(R.id.ok_button);
        Button cancelButton = viewInflated.findViewById(R.id.cancel_button);
        builder.setView(viewInflated);

        AlertDialog dialog = builder.create();

        okButton.setOnClickListener(v -> {
            String groupName = input.getText().toString();
            if (!groupName.isEmpty()) {
                dbHelper.addGroup(groupName);
                groupList.add(groupName);
                Collections.sort(groupList);  // 添加新组后进行排序
                groupListAdapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });

        cancelButton.setOnClickListener(v -> dialog.cancel());

        dialog.show();
    }

    private void showEditDeleteGroupDialog(String groupName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Edit/Delete Group");

        View viewInflated = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_group, (ViewGroup) getView(), false);
        final EditText input = viewInflated.findViewById(R.id.input);
        input.setText(groupName);
        Button okButton = viewInflated.findViewById(R.id.ok_button);
        Button deleteButton = viewInflated.findViewById(R.id.cancel_button);  // Reuse cancel button for delete
        builder.setView(viewInflated);

        AlertDialog dialog = builder.create();

        okButton.setText("Edit");
        okButton.setOnClickListener(v -> {
            String newGroupName = input.getText().toString();
            if (!newGroupName.isEmpty()) {
                dbHelper.updateGroupName(groupName, newGroupName);
                groupList.set(groupList.indexOf(groupName), newGroupName);
                Collections.sort(groupList);  // 编辑组名后进行排序
                groupListAdapter.notifyDataSetChanged();
            }
            dialog.dismiss();
        });

        deleteButton.setText("Delete");
        deleteButton.setOnClickListener(v -> {
            dbHelper.deleteGroup(groupName);
            groupList.remove(groupName);
            groupListAdapter.notifyDataSetChanged();
            dialog.dismiss();
        });

        dialog.show();
    }
}
