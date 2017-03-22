package edu.upenn.cis350.foodtruckapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rafaelcastro on 3/14/17.
 */

public class FoodTruckOrderMGM extends AppCompatActivity  {

    private String username;
    private FirebaseDatabase database;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseRef;

    public FoodTruckOrderMGM(String userWhoOrdered) {
        this.username  = userWhoOrdered;
    }

    protected void orderDone() {

        databaseRef = FirebaseDatabase.getInstance().getReference("Users");
        mAuth = FirebaseAuth.getInstance();
        sendOrder();

        //for testing purposes we are going to send the notification to the same device we are testing
        //Do this part inside the client send order part
        String username = FirebaseInstanceId.getInstance().getId();
        FirebaseMessaging.getInstance().subscribeToTopic("user_"+username);
        ///////////////////////
    }

    private void sendOrder() {
        String currVendor = mAuth.getCurrentUser().getUid();
        DatabaseReference nameofft = databaseRef.child(currVendor).child("Name Of Food Truck");

        nameofft.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String foodTruckName= dataSnapshot.getValue().toString();
                sendNotificationToUser(username, "Your order at " + foodTruckName + " is ready!");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    //This sends a request to the queue, then the Node script handles everything in our other server,
    //and sends notification to user. PLEASE DO NOT MODIFY ANYTHING IN THIS FILE!
    private void sendNotificationToUser(String user, final String message) {
        //Access the queue part of the database
        database = FirebaseDatabase.getInstance();
        final DatabaseReference notifications = database.getReference("notificationRequests");
        Map notification = new HashMap<>();
        notification.put("username", user);
        notification.put("message", message);
        notifications.push().setValue(notification);
    }
}
