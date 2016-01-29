package com.kodelabs.boilerplate.storage;

import com.kodelabs.boilerplate.domain.repository.MessageRepository;

/**
 * Created by dmilicic on 1/29/16.
 */
public class WelcomeMessageRepository implements MessageRepository {
    @Override
    public String getWelcomeMessage() {
        String msg = "Welcome, friend!"; // let's be friendly


        // let's simulate some network/database lag
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        return msg;
    }
}
