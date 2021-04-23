package com.openclassrooms.go4lunch.repository;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.openclassrooms.go4lunch.database.dao.MessageDao;
import com.openclassrooms.go4lunch.database.dao.UserDao;
import com.openclassrooms.go4lunch.models.Message;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import io.reactivex.rxjava3.subscribers.ResourceSubscriber;

public class ChatRepo {

    private final MessageDao messageDao;
    private final UserDao userDao;

    @Inject
    public ChatRepo(MessageDao messageDao, UserDao userDao) {

        this.messageDao = messageDao;
        this.userDao = userDao;
    }

    public void insertMessage(Message message) {
        messageDao.insertMessage(message)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onError(Throwable t) {
                    }

                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                    }

                });
    }

    @SuppressLint("CheckResult")
    public LiveData<List<Message>> getAllMessages(String messageReceiver) {
        LiveData<List<Message>> resultMessages = new MutableLiveData<>();
        messageDao.getMessagesConf(messageReceiver)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ResourceSubscriber<List<Message>>() {
                    @Override
                    public void onNext(List<Message> messages) {
                        ((MutableLiveData<List<Message>>) resultMessages).postValue(messages);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d("ListMessage", "" + t.toString());
                    }

                    @Override
                    public void onComplete() {
                    }
                });

        return resultMessages;
    }

    public void updateStatusMessage(boolean status, String ejabberdName) {
        userDao.updateStatus(status, ejabberdName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new CompletableObserver() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onComplete() {
                        Log.d("updateStatus", "Success");
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }
                });
    }
}

