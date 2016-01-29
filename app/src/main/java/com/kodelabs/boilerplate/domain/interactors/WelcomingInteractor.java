package com.kodelabs.boilerplate.domain.interactors;


import com.kodelabs.boilerplate.domain.interactors.base.Interactor;


public interface WelcomingInteractor extends Interactor {

    interface Callback {
        void onMessageRetrieved(String message);

        void onRetrievalFailed(String error);
    }
}
