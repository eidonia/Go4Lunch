package com.openclassrooms.go4lunch.ui.restaurant;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.openclassrooms.go4lunch.R;
import com.openclassrooms.go4lunch.databinding.ActivityChatBinding;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.viewmodel.ChatViewModel;
import com.openclassrooms.go4lunch.viewmodel.RestaurantViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

import static com.openclassrooms.go4lunch.utils.Constante.PACKAGE_NAME;
import static com.openclassrooms.go4lunch.utils.Constante.SENT_MESSAGE;

@AndroidEntryPoint
public class ChatActivity extends AppCompatActivity {

    private final LifecycleOwner lifecycleOwner = this;
    private ActivityChatBinding binding;
    private ChatViewModel chatViewModel;
    private RestaurantViewModel restaurantViewModel;
    private User user;
    private Intent intent;
    private String jid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        intent = getIntent();
        jid = intent.getStringExtra("userEjabberd");

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        restaurantViewModel = new ViewModelProvider(this).get(RestaurantViewModel.class);

        chatViewModel.updateStatusMessage(false, jid);

        createListMessage();
        createUI();

        binding.chatView.setOnSentMessageListener(chatMessage -> {
            String message = binding.chatView.getTypedMessage();
            sendMessage(message);
            binding.chatView.getInputEditText().getText().clear();
            return false;
        });
    }

    private void createListMessage() {
        chatViewModel.getAllMessages(jid).observe(lifecycleOwner, messages -> {
            binding.chatView.clearMessages();
            binding.chatView.addMessages((ArrayList) messages);
        });
    }

    private void createUI() {
        String name = intent.getStringExtra("userName");
        binding.topBar.setTitle(name);
        binding.topBar.setTitleTextColor(getResources().getColor(R.color.colorTitleTopBar));
    }

    private void sendMessage(String message) {
        restaurantViewModel.getUserFirebaseMessage(intent.getStringExtra("userEmail")).observe(lifecycleOwner, user1 -> {

            Intent intent = new Intent();
            intent.setAction(SENT_MESSAGE);
            intent.setPackage(PACKAGE_NAME);
            intent.putExtra("sendMessage", message);
            intent.putExtra("sendJid", user1.getEjabberdName());
            intent.putExtra("timeSent", System.currentTimeMillis());
            sendBroadcast(intent);
        });


    }

}