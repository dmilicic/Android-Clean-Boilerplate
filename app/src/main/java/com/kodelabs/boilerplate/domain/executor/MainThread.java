package com.kodelabs.boilerplate.domain.executor;

/**
 * This interface will define a class that will enable interactors to run certain operations on the main (UI) thread. For example,
 * if an interactor needs to show an object to the UI this can be used to make sure the show method is called on the UI
 * thread.
 * <p/>
 */
public interface MainThread {

    /**
     * Make runnable operation run in the main thread.
     *
     * @param runnable The runnable to run.
     */
    void post(final Runnable runnable);
}
