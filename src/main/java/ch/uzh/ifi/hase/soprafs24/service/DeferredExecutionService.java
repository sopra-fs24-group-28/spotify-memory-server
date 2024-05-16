package ch.uzh.ifi.hase.soprafs24.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * The {@link DeferredExecutionService} is only to be used for the handling of the Inactive player request.
 * Since we handle the inactivity rather trivially in the frontend, and need to avoid racing Rest Requests and WS updates
 * we manually lock a game that has a "running turn", i.e. is calculating at the moment of an incoming Inactive.
 * Said request is stored here for execution after calculation completion.
 */
@Service
public class DeferredExecutionService {
    private final Map<Integer, Runnable> deferredTasks = new ConcurrentHashMap<>();

    public void deferTask(Integer gameId, Runnable task) {
        deferredTasks.put(gameId, task);
    }

    public void executeDeferredTask(Integer gameId) {
        Runnable task = deferredTasks.remove(gameId);
        if (task != null) {
            task.run();
        }
    }

    public boolean hasDeferredTask(Integer gameId) {
        return deferredTasks.containsKey(gameId);
    }
}
