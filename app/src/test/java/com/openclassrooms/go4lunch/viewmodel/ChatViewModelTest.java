package com.openclassrooms.go4lunch.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.openclassrooms.go4lunch.chatui.models.ChatMessage;
import com.openclassrooms.go4lunch.models.Message;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.repository.ChatRepo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ChatViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    public ChatViewModel chatViewModel;

    public ChatRepo chatRepo;

    public MutableLiveData<List<Message>> mutableListMessages = new MutableLiveData<>();

    public List<Message> messages = Arrays.asList(
            new Message("abc", System.currentTimeMillis(), ChatMessage.Type.SENT, "bastien"),
            new Message("abc", System.currentTimeMillis(), ChatMessage.Type.SENT, "bastien")
    );

    public User user = new User();

    @Before
    public void setUp() {
        chatRepo = mock(ChatRepo.class);
        chatViewModel = new ChatViewModel(chatRepo);

    }

    @Test
    public void getListMessage_With_GoodName() {
        String name = "bastien";
        mutableListMessages.setValue(messages);
        when(chatRepo.getAllMessages(name)).thenReturn(mutableListMessages);
        assertNotNull(chatViewModel.getAllMessages(name));
        chatViewModel.getAllMessages(name).observeForever(messages1 -> {
            assertArrayEquals(messages1.toArray(), messages.toArray());
        });
    }
}