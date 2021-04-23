package com.openclassrooms.go4lunch.di;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.room.Room;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.openclassrooms.go4lunch.database.ChatDatabase;
import com.openclassrooms.go4lunch.database.dao.MessageDao;
import com.openclassrooms.go4lunch.database.dao.UserDao;
import com.openclassrooms.go4lunch.network.GoogleMapService;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.qualifiers.ApplicationContext;
import dagger.hilt.components.SingletonComponent;
import hu.akarnokd.rxjava3.retrofit.RxJava3CallAdapterFactory;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.Context.LOCATION_SERVICE;
import static com.openclassrooms.go4lunch.utils.Constante.DB_NAME;

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {

    @Provides
    @Singleton
    public static GoogleMapService gMapService() {
        Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        return new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/maps/api/place/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(GoogleMapService.class);
    }

    @Provides
    @Singleton
    @Named("users")
    public DatabaseReference getUsers() {
        return FirebaseDatabase.getInstance().getReference().child("users");
    }

    @Provides
    @Singleton
    @Named("restaurants")
    public DatabaseReference getRest() {
        return FirebaseDatabase.getInstance().getReference().child("restaurants");
    }

    @SuppressLint("MissingPermission")
    @Provides
    @Singleton
    public static Location getLocation(@ApplicationContext Context context) {
        Log.d("Location", "Here");
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        return locationManager.getLastKnownLocation(provider);
    }

    @Provides
    @Singleton
    public FirebaseStorage provideFirebaseStorage() {
        return FirebaseStorage.getInstance("gs://go4lunch-8f1f6.appspot.com");
    }

    @Provides
    @Singleton
    public StorageReference provideStorageRef(FirebaseStorage storage) {
        return storage.getReference();
    }

    @Provides
    @Singleton
    public FirebaseFirestore providesDb() {
        return FirebaseFirestore.getInstance();
    }


    //CHAT

    @Provides
    @Singleton
    public ChatDatabase provideChatDatabase(@ApplicationContext Context context) {
        return Room.databaseBuilder(context, ChatDatabase.class, DB_NAME)
                .allowMainThreadQueries()
                .fallbackToDestructiveMigration()
                .build();
    }

    @Provides
    @Singleton
    public MessageDao provideMessageDao(ChatDatabase chatDatabase) {
        return chatDatabase.messageDao();
    }

    @Provides
    @Singleton
    public UserDao provideUserDao(ChatDatabase chatDatabase) {
        return chatDatabase.userDao();
    }
}
