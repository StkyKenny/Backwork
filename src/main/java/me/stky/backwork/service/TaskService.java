package me.stky.backwork.service;

import lombok.extern.slf4j.Slf4j;
import me.stky.backwork.dao.TaskDao;
import me.stky.backwork.model.Task;
import me.stky.backwork.model.TaskStatus;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * For the GUI to CRUD with MongoDB
 * Given the Swing app isn't always with in a Spring context, this service is used to interact with MongoDB in a low-level.
 * I am using SQLException to indicate error with MongoDB
 *
 * @author stky
 */
@Service
@Slf4j
public class TaskService {

    private TaskDao taskDao;

    private TaskService(TaskDao taskDao) {
        this.taskDao = taskDao;
    }

    /**
     * Refresh routine, set status to todo when recurrent task is ready again
     */
    public List<Task> reset(List<Task> tasks) {
        var now = Instant.now().atZone(ZoneId.systemDefault());
        List<Task> updatedTasks = new ArrayList<>();
        for (Task task : tasks) {
            var recurringInfo = task.getRecurringTaskInfo();
            if (recurringInfo != null) {
                if (now.isAfter(recurringInfo.getNextScheduledDate().atZone(ZoneId.systemDefault()))) {
                    task.setStatus(TaskStatus.TODO);

                    try {
                        updatedTasks.add(taskDao.setRecurringTaskToTODO(task));
                    } catch (SQLException e) {
                        log.error("Error trying to reset this task :{}", task.getID());
                    }

                } else {
                    updatedTasks.add(task);
                }
            } else {
                updatedTasks.add(task);
            }
        }
        return updatedTasks;
    }


    public BsonValue addTask(Task task) throws SQLException {
        return taskDao.addTask(task);
    }

    /**
     * @return The objectID that was updated
     */
    public ObjectId replaceTask(Task task, Task newTask) throws SQLException {
        return taskDao.replaceTask(task, newTask);
    }

    public List<Task> getTasks() {
        return taskDao.getTasks();
    }

    public Task deleteTask(Task task) throws SQLException {
        return taskDao.deleteTask(task);
    }

    /**
     * Set the Task status to Completed
     *
     * @param task The Task to complete
     * @return The updated Task from the Database
     * @throws SQLException if the transaction wasn't acknowledged
     */
    public Task completeTask(Task task) throws SQLException {
        return taskDao.completeTask(task);
    }

    /***
     * Will archive the task by moving it to the archive collection and setting it to status ARCHIVED
     * Use Transaction body to guarantee atomicity
     * @param task The Task to archive
     */
    public void archiveTask(Task task) throws SQLException {
        taskDao.archiveTask(task);
    }

    /**
     * Get Task Sorted by nearest upcoming deadline to furthest, then by oldest createdDate
     *
     * @return
     */
    public List<Task> getTaskSortedByDeadline() {
        return taskDao.getTaskSortedByDeadline();

    }
}
