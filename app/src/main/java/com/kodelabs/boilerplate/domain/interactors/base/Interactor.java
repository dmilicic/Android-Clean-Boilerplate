package com.kodelabs.boilerplate.domain.interactors.base;

/**
 * Created by dmilicic on 12/13/15.
 */
public interface Interactor {

    /**
     * This is the main method that starts an interactor. It will make sure that the interactor operation is done on a
     * background thread.
     */
    void execute();
}
