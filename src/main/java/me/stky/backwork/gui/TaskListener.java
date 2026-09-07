package me.stky.backwork.gui;

public interface TaskListener {
    void onSelectTask(TaskComponent task);

    void onEditTask(TaskComponent task);

    void onArchiveTask(TaskComponent task);

    void onDelete(TaskComponent task);

}