package android.cloudpoint.com.mapstests;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button saveBtn, skipBtn;
    private EditText vehicleModelTxt, vehicleTypeTxt, phoneNumberTxt, fullNameTxt;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise view components
        fullNameTxt = (EditText) findViewById(R.id.fullName);
        phoneNumberTxt = (EditText) findViewById(R.id.phoneNumber);
        vehicleTypeTxt = (EditText) findViewById(R.id.vehicleType);
        vehicleModelTxt = (EditText) findViewById(R.id.vehicleModel);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        skipBtn = (Button) findViewById(R.id.skip);

        // Initialise Firebase objects
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Log.d(TAG, "onDataChange: " + dataSnapshot.getValue());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to Users.", error.toException());
            }
        });

        // When the save button is clicked
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "saveBtn: Starting the registration process");

                if (isFormFilled()) {
                    // Initialise the User Object
                    final User user = createUser();

                    // Save the User to the database
                    myRef.child(String.valueOf(user.getPhoneNumber())).setValue(user);

                    String message = "Welcome " + user.getFullName();
                    showToast(message);

                    clearFields();

                    Log.d(TAG, "saveBtn: Finished the registration process");

                    Log.d(TAG, "saveBtn: Opening Map Activty");
                    navigateToMap();
                } else {
                    String message = "Please Fill in all the fields.";
                    showToast(message);
                }
            }
        });

        skipBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "skipBtn: Opening Map Activty");
                navigateToMap();
            }
        });
    }

    private void navigateToMap() {
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    /**
     * Set all the fields to their initial values
     */
    private void clearFields() {
        fullNameTxt.setText("");
        phoneNumberTxt.setText("");
        vehicleTypeTxt.setText("");
        vehicleModelTxt.setText("");
    }

    /**
     * Show message to the user
     *
     * @param message accepts the message to be shown
     */
    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Assign the user object all the relevant info
     *
     * @return the newly created user object
     */
    private User createUser() {
        Log.d(TAG, "createUser: creating the user");
        Vehicle car = new Vehicle(vehicleTypeTxt.getText().toString(), vehicleModelTxt.getText().toString());
        return new User(fullNameTxt.getText().toString(), Integer.parseInt(phoneNumberTxt.getText().toString()), car);
    }

    /**
     * Checks if the User has filled in their info
     *
     * @return true if user has filled in their info else false
     */
    private boolean isFormFilled() {
        return !fullNameTxt.getText().equals("") && !phoneNumberTxt.getText().equals("")
                && !vehicleTypeTxt.getText().equals("") && !vehicleModelTxt.getText().equals("");
    }


}
