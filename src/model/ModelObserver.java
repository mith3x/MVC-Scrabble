package model;

/**
 * Interface for the observer pattern. The ModelObserver interface is implemented by the
 * View class to allow the Model to notify the View when the model has changed.
 */
public interface ModelObserver {
    /**
     * Updates the view with the specified message and model.
     *
     * @param message the message to display
     * @param m       the model
     */
    void update(String message, Model m);
}