package com.openclassrooms.go4lunch.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import com.openclassrooms.go4lunch.models.Message;
import com.openclassrooms.go4lunch.repository.ChatRepo;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ChatViewModel extends ViewModel {

    private final ChatRepo repository;
    private SavedStateHandle handle;

    @Inject
    public ChatViewModel(ChatRepo chatRepo) {
        this.repository = chatRepo;
    }

    //MESSAGE
    public LiveData<List<Message>> getAllMessages(String messageReceiver) {
        return repository.getAllMessages(messageReceiver);
    }

    public void updateStatusMessage(boolean status, String ejabberdName) {
        repository.updateStatusMessage(status, ejabberdName);
    }
}
