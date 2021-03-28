package com.openclassrooms.go4lunch.viewmodel;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.MutableLiveData;

import com.openclassrooms.go4lunch.models.Restaurant;
import com.openclassrooms.go4lunch.models.User;
import com.openclassrooms.go4lunch.repository.MapRepository;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestaurantViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    public RestaurantViewModel restaurantViewModel;

    public MapRepository repo;
    public MutableLiveData<List<Restaurant>> mutableListRest = new MutableLiveData<>();

    public MutableLiveData<List<User>> mutableListUser = new MutableLiveData<>();

    public MutableLiveData<User> mutableUser = new MutableLiveData<>();

    @Before
    public void setUp() {
        repo = mock(MapRepository.class);
        restaurantViewModel = new RestaurantViewModel(repo);
    }

    @Test
    public void getUsersRoom() {
        List<User> listUser = new ArrayList<>();
        listUser.add(new User());
        listUser.add(new User());
        mutableListUser.setValue(listUser);
        when(repo.getUsersRoom()).thenReturn(mutableListUser);
        assertNotNull(restaurantViewModel.getUsersRoom());
        restaurantViewModel.getUsersRoom().observeForever(users -> {
            assertArrayEquals(users.toArray(), listUser.toArray());
        });
    }

    @Test
    public void getRestauQueryList() {
        List<Restaurant> listRestau = new ArrayList<>();
        listRestau.add(new Restaurant());
        listRestau.add(new Restaurant());
        mutableListRest.setValue(listRestau);
        String restau = "Café des Sports";
        when(repo.searchRestaurant(restau)).thenReturn(mutableListRest);
        assertNotNull(restaurantViewModel.getRestauQueryList(restau));
        restaurantViewModel.getRestauQueryList(restau).observeForever(restaurants -> {
            System.out.println("rdefgqz " + restaurants.size() + " " + listRestau.size());
            assertArrayEquals(restaurants.toArray(), listRestau.toArray());
        });
    }

    @Test
    public void getListRestaurant() {
        List<Restaurant> listRestau = new ArrayList<>();
        listRestau.add(new Restaurant());
        listRestau.add(new Restaurant());
        mutableListRest.setValue(listRestau);
        when(repo.getRestFromFirebase()).thenReturn(mutableListRest);
        assertNotNull(restaurantViewModel.getListRestaurant());
        restaurantViewModel.getListRestaurant().observeForever(restaurants -> {
            System.out.println("rdefgqz " + restaurants.size() + " " + listRestau.size());
            assertArrayEquals(restaurants.toArray(), listRestau.toArray());
        });
    }

    @Test
    public void getUser() {
        User userTest = new User();
        mutableUser.setValue(userTest);
        when(repo.getUserFirebase()).thenReturn(mutableUser);
        assertNotNull(restaurantViewModel.getUser());
        restaurantViewModel.getUser().observeForever(user -> {
            assertEquals(user, userTest);
        });
    }

    @Test
    public void getUserFirebaseMessage() {
        String email = "email@email.email";
        User userTest = new User();
        mutableUser.setValue(userTest);
        when(repo.getUserFirebaseMessage(email)).thenReturn(mutableUser);
        assertNotNull(restaurantViewModel.getUserFirebaseMessage(email));
        restaurantViewModel.getUserFirebaseMessage(email).observeForever(user -> {
            assertEquals(user, userTest);
        });
    }
}