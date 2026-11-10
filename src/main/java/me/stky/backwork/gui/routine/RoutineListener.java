// backwork/src/main/java/me/stky/backwork/gui/task/RoutineListener.java
package me.stky.backwork.gui.routine;


public interface RoutineListener {
    void onSelectRoutine(RoutineComponent routineComponent);

    void onEditRoutine(RoutineComponent routineComponent);

    void onArchiveRoutine(RoutineComponent routineComponent);

    void onDeleteRoutine(RoutineComponent routineComponent);
}