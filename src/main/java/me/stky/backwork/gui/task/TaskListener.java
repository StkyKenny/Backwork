package me.stky.backwork.gui.task;

public interface TaskListener {
    void onSelectTask(TaskComponent taskComponent);

    void onEditTask(TaskComponent taskComponent);

    void onArchiveTask(TaskComponent taskComponent);

    void onDelete(TaskComponent taskComponent);

    void onUncompleteTask(TaskComponent taskComponent);

}