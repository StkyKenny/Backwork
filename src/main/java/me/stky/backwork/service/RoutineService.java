package me.stky.backwork.service;

import me.stky.backwork.dao.RoutineDao;
import me.stky.backwork.model.routine.Routine;
import org.bson.BsonValue;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.List;

@Service
public class RoutineService {

    private RoutineDao routineDao;

    public RoutineService(RoutineDao routineDao) {
        this.routineDao = routineDao;
    }

    public BsonValue addTask(Routine routine) throws SQLException {
        return routineDao.addRoutine(routine);
    }

    public ObjectId replaceRoutine(Routine routine, Routine updatedRoutine) throws SQLException {
        return routineDao.replaceRoutine(routine, updatedRoutine);
    }

    public List<Routine> getRoutineSortedByNextDate() {
        return routineDao.getRoutineSortedByNextDate();
    }

    public Routine deleteRoutine(Routine routine) throws SQLException {
        return routineDao.deleteRoutine(routine);
    }

    public Routine completeRoutine(Routine routine) throws SQLException {
        return routineDao.completeRoutine(routine);
    }

    public void archiveRoutine(Routine routine) throws SQLException {
        routineDao.archiveRoutine(routine);
    }

    public List<Routine> reset(List<Routine> routines) {
        // Implementation similar to TaskService.reset()
        return routines;
    }
}