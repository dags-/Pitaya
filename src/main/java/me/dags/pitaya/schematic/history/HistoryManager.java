package me.dags.pitaya.schematic.history;

public class HistoryManager {

    private static final HistoryManager instance = new HistoryManager();

    private History current = History.NONE;

    public static synchronized History push() {
        WeakHistory history = new WeakHistory();
        instance.current = history;
        return history;
    }

    public static synchronized void pop(History history) {
        if (instance.current == history) {
            instance.current = History.NONE;
        }
    }

    public static synchronized History getHistory() {
        return instance.current;
    }
}
