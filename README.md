# Overview

This is starter template for writing Android apps using **Clean architecture**. You can download it, modify it and start building your apps on top of it. Most of the boilerplate code for writing your first view, presenter and interactor is already written and you just need to implement your own logic.

This starter app supports **API 15 and above**.

This template uses **regular Java** instead of RxJava and does not use Dagger. Although I recommend Dagger, the reason for this is that I did not want to add more complexity as the architecture itself is probably complex enough to understand. If you prefer RxJava and Dagger then you can look at an awesome project called [Android-CleanArchitecture] that also inspired me to create this.

To see a sample app using Clean Architecture you can look [here].

## Libraries included

 - [Android Support Library] for backwards compatibility.
 - [Timber] for logging.
 - [Butterknife] for view injection.
 - [Retrofit] for network code.
 - [JUnit] and [Mockito] for testing.
 - [Findbugs] for finding bugs, *duh*.

# Things to change

You will want to make a few minor changes when using this template:

- Rename the base package `com.kodelabs.boilerplate` to your preferred name. *[How to]*
- Modify the `applicationId` in your `app/build.gradle` to the base package name you set in the above step.

# Getting started writing a new use case

A use case is just some isolated functionality of the app. A use case may  (e.g. on user click) or may not be started by a user. For example, a use case might be: *"Get all data from the database and display it on the UI when the app starts."*

In this example, our use case will be: ***"Greet the user with a message that is stored in the database when the app starts."*** This example can be tried out in the `example` branch. This example will showcase how to write the following three packages needed to make the use case work:

- the **presentation** layer
- the **storage** layer
- the **domain** layer

The first two belong to the outer layers while the last one is the inner/core layer. **Presentation** package is responsible for everything related to showing things on the screen — it includes the whole MVP stack (it means it also includes both the UI and Presenter packages even though they belong to different layers).


## **Writing a new interactor (inner/core layer)**

In reality you could start in any layer of the architecture, but I recommend you to start on your core business logic first. You can write it, test it and make sure it works without ever creating an activity.

So let's start by creating an interactor. The interactor is where the main logic of the use case resides. **All interactors are run in the background thread so there shouldn't be any impact on UI performance.** Let's create a new interactor with a warm name of `WelcomingInteractor`.

```
public interface WelcomingInteractor extends Interactor {

    interface Callback {

        void onMessageRetrieved(String message);

        void onRetrievalFailed(String error);
    }
}

```

The `Callback` is responsible for talking to the UI on the main thread, we put it into this Interactor’s interface so we don’t have to name it a ***WelcomingInteractorCallback*** — to distinguish it from other callbacks. Now let’s implement our logic of retrieving a message. Let's say we have some `MessageRepository` that can give us our welcome message.

```
public interface MessageRepository {
    String getWelcomeMessage();
}
```

Now we should implement our Interactor interface with our business logic. **It is important that the implementation extends the `AbstractInteractor` which takes care of running it on the background thread.**

```
public class WelcomingInteractorImpl extends AbstractInteractor implements WelcomingInteractor {

...

  private void notifyError() {
      mMainThread.post(new Runnable() {
          @Override
          public void run() {
              mCallback.onRetrievalFailed("Nothing to welcome you with :(");
          }
      });
  }

  private void postMessage(final String msg) {
      mMainThread.post(new Runnable() {
          @Override
          public void run() {
              mCallback.onMessageRetrieved(msg);
          }
      });
  }

  @Override
  public void run() {

      // retrieve the message
      final String message = mMessageRepository.getWelcomeMessage();

      // check if we have failed to retrieve our message
      if (message == null || message.length() == 0) {

          // notify the failure on the main thread
          notifyError();

          return;
      }

      // we have retrieved our message, notify the UI on the main thread
      postMessage(message);
  }
}
```
This just attempts to retrieve the message and sends the message or the error to the UI to display it. We notify the UI using our `Callback` which is actually going to be our `Presenter`. **That is the crux of our business logic. Everything else we need to do is framework dependent.**

Let's take a look which dependencies does this Interactor have:

```
import com.kodelabs.boilerplate.domain.executor.Executor;
import com.kodelabs.boilerplate.domain.executor.MainThread;
import com.kodelabs.boilerplate.domain.interactors.WelcomingInteractor;
import com.kodelabs.boilerplate.domain.interactors.base.AbstractInteractor;
import com.kodelabs.boilerplate.domain.repository.MessageRepository;
```

As you can see, there is **no mention of any Android code**. That is the **main benefit** of this approach. Also, we do not care about specifics of the UI or database, we just call interface methods that someone somewhere in the outer layer will implement.

## **Testing our interactor**

We can now run and test our Interactor without running an emulator. So let's write a simple **JUnit** test to make sure it works:

```
@Test
public void testWelcomeMessageFound() throws Exception {

    String msg = "Welcome, friend!";

    when(mMessageRepository.getWelcomeMessage())
            .thenReturn(msg);

    WelcomingInteractorImpl interactor = new WelcomingInteractorImpl(
      mExecutor,
      mMainThread,
      mMockedCallback,
      mMessageRepository
    );
    interactor.run();

    Mockito.verify(mMessageRepository).getWelcomeMessage();
    Mockito.verifyNoMoreInteractions(mMessageRepository);
    Mockito.verify(mMockedCallback).onMessageRetrieved(msg);
}
```

Again, this Interactor code has no idea that it will live inside an Android app.

## **Writing the presentation layer**

Presentation layer is the **outer layer** in Clean. It consists of framework dependent code to display the UI to the user. We will use `MainActivity` class to display the welcome message to the user when the app resumes.

Let's start by writing the interface of our `Presenter` and `View`. The only thing our `view` needs to do is to display the welcome message:

```
public interface MainPresenter extends BasePresenter {

    interface View extends BaseView {
        void displayWelcomeMessage(String msg);
    }
}
```

So how and where do we start the Interactor when an app resumes? Everything that is not strictly view related should go into the `Presenter` class. This helps achieve `separation of concerns` and prevents the `Activity` classes from getting bloated. This includes all code working with Interactors.

In our `MainActivity` class we override the `onResume` method:

```
@Override
protected void onResume() {
    super.onResume();

    // let's start welcome message retrieval when the app resumes
    mPresenter.resume();
}
```

All `Presenter` objects implement the `resume()` method when they extend `BasePresenter`. We start the interactor inside the `MainPresenter` class in the `resume()` method:

```
@Override
public void resume() {

    mView.showProgress();

    // initialize the interactor
    WelcomingInteractor interactor = new WelcomingInteractorImpl(
            mExecutor,
            mMainThread,
            this,
            mMessageRepository
    );

    // run the interactor
    interactor.execute();
}
```

The `execute()` method will just execute the `run()` method of the `WelcomingInteractorImpl` in a background thread. The `run()` method can be seen later in the ***Writing a new interactor*** section.

You may notice that the Interactor behaves similarly to the `AsyncTask` class. You supply it with all that it needs to run and execute it. You might ask why didn't we just use `AsyncTask`? Because that is **Android specific code** and you would need an emulator to run it and to test it.


We provide several things to the Interactor:

- The `ThreadExecutor` instance which is responsible for executing interactors in a background thread. I usually make it a singleton. This class actually resides in the `domain` and does not need to be implemented in an outer layer.
- The `MainThreadImpl` instance which is responsible for posting runnables on the main thread from the interactor. Main threads are accessible using framework dependent code and we implement it in an outer `threading` layer.
- You may also notice we provide `this` to the Interactor — `MainPresenter` is the `Callback` object the Interactor will use to notify the UI for events.
- We provide an instance of the `WelcomeMessageRepository` which implements the `MessageRepository` interface that our interactor uses. The `WelcomeMessageRepository` is covered later in the ***Writing the storage layer*** section.

Regarding `this`, the `MainPresenter` of the `MainActivity` really does implement the `Callback` interface:

```
public class MainPresenterImpl extends AbstractPresenter implements MainPresenter,
        WelcomingInteractor.Callback {
```

And that is how we listen for events from the Interactor. This is the code from the `MainPresenter`:

```
@Override
public void onMessageRetrieved(String message) {
    mView.hideProgress();
    mView.displayWelcomeMessage(message);
}

@Override
public void onRetrievalFailed(String error) {
    mView.hideProgress();
    onError(error);
}
```

The `View` seen in these snippets is our `MainActivity` which implements this interface:

```
public class MainActivity extends AppCompatActivity implements MainPresenter.View {
```

Which is then responsible for displaying these messages, as seen here:

```
@Override
public void displayWelcomeMessage(String msg) {
    mWelcomeTextView.setText(msg);
}
```

And that is pretty much it for the presentation layer.

## **Writing the storage layer**

This is where our repository gets implemented. All the database specific code should come here. The repository pattern just abstracts where the data is coming from. Our main business logic is oblivious to the source of the data — be it from a database, a server or text files.

For complex data you can use [ContentProviders] or ORM tools such as [DBFlow]. If you need to retrieve data from the web then [Retrofit] will help you. If you need simple key-value storage then you can use [SharedPreferences]. You should use the right tool for the job.

Our database is not really a database. It is going to be a very simple class with some simulated delay:

```
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
```
As far as our `WelcomingInteractor` is concerned, the lag might be because of the real network or any other reason. It doesn't really care what is underneath the `MessageRepository` as long as it implements that interface.  


# License

`MIT`


[here]: <https://github.com/dmilicic/android-sample-app>
[How to]: <https://stackoverflow.com/questions/16804093/android-studio-rename-package>
[Butterknife]: <https://github.com/JakeWharton/butterknife>
[Timber]: <https://github.com/JakeWharton/timber>
[Android Support Library]: <https://developer.android.com/tools/support-library/index.html>
[JUnit]: <https://github.com/junit-team/junit/wiki/Download-and-Install>
[Mockito]: <http://site.mockito.org/>
[Retrofit]: <https://square.github.io/retrofit/>
[Findbugs]: <http://findbugs.sourceforge.net/>
[DBFlow]: <https://github.com/Raizlabs/DBFlow>
[SharedPreferences]: <http://developer.android.com/training/basics/data-storage/shared-preferences.html>
[ContentProviders]: <http://developer.android.com/guide/topics/providers/content-providers.html>

[Android-CleanArchitecture]: <https://github.com/android10/Android-CleanArchitecture>
