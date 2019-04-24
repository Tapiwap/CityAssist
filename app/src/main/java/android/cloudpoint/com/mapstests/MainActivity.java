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
    private String vehicleModel, vehicleType, fullname;
    private int phoneNumber;

    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise view components
        initialiseViewComponents();

        // Initialise Firebase objects
        initialiseDatabase();

        // Read from the database
        databaseEventListener();

        // When the save button is clicked
        saveButtonEventListener();

        //When the skip button is clicked
        skipButtonEventListener();
    }

    private void skipButtonEventListener() {
        skipBtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                Log.d(TAG, "skipBtn: Opening Map Activty");
                navigateToMap();
            }
        });
    }

    private void saveButtonEventListener() {
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "saveBtn: Starting the registration process");

                if (isFormValid()) {
                    final User user = saveUser();

                    showToast("Welcome to City Assist, " + user.getFullName());

                    clearFields();

                    Log.d(TAG, "saveBtn: Finished the registration process");

                    Log.d(TAG, "saveBtn: Opening Map Activty");
                    navigateToMap();
                } else {
                    Log.d(TAG, "onClick: FORM IS NOT VALID");
                    String message = "Please Fill in all the fields.";
                    showToast(message);
                }
            }
        });
    }

    private void databaseEventListener() {
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
                Log.w(TAG, "Failed to read Users.", error.toException());
            }
        });
    }

    private void initialiseDatabase() {
        Log.d(TAG, "initialiseDatabase: Initialising Firebase");
        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");
    }

    /**
     * Initialise the view components
     */
    private void initialiseViewComponents() {
        Log.d(TAG, "initialiseViewComponents: Initialising the view");
        fullNameTxt = (EditText) findViewById(R.id.fullName);
        phoneNumberTxt = (EditText) findViewById(R.id.phoneNumber);
        vehicleTypeTxt = (EditText) findViewById(R.id.vehicleType);
        vehicleModelTxt = (EditText) findViewById(R.id.vehicleModel);
        saveBtn = (Button) findViewById(R.id.saveBtn);
        skipBtn = (Button) findViewById(R.id.skip);
    }

    /**
     * Commit the user to the database
     * @return return the user object
     */
    private User saveUser() {
        Log.d(TAG, "saveUser: Saving the user");
        // Initialise the User Object
        final User user = createUser();

        // Save the User to the database
        myRef.child(String.valueOf(user.getPhoneNumber())).setValue(user);
        return user;
    }

    /**
     * Check if the user has entered relevant info
     * in the right format
     * @return true or false
     */
    private boolean isFormValid() {
        Log.d(TAG, "isFormValid: Checking user input");
        boolean valid = true;
        if (fullNameTxt.getText().toString().trim().isEmpty()) {
            fullNameTxt.setError("Please Enter Your Full Name.");
            valid = false;
        }

        if (vehicleTypeTxt.getText().toString().trim().isEmpty()) {
            vehicleTypeTxt.setError("Please Enter Your Vehicle Type");
            valid = false;
        }

        if (vehicleModelTxt.getText().toString().trim().isEmpty()) {
            vehicleModelTxt.setError("Please Enter Your Vehicle's Model");
            valid = false;
        }

        if (phoneNumberTxt.getText().toString().trim().isEmpty()) {
            phoneNumberTxt.setError("Please Enter Your Phone Number.");
            valid = false;
        }

        if (phoneNumberTxt.getText().toString().trim().length() != 8) {
            phoneNumberTxt.setError("Phone Number Must Have 8 Digits.");
            valid = false;
        }
        return valid;
    }

    /**
     * Navigate user to the map activity
     */
    private void navigateToMap() {
        Log.d(TAG, "navigateToMap: navigating between activities");
        Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
        startActivity(intent);
    }

    /**
     * Set all the fields to their initial values
     */
    private void clearFields() {
        Log.d(TAG, "clearFields: clearing the view");
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
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }

    /**
     * Assign the user object all the relevant info
     *
     * @return the newly created user object
     */
    private User createUser() {
        Log.d(TAG, "createUser: marshalling the user object");
        Log.d(TAG, "createUser: creating the user");
        Vehicle car = new Vehicle(vehicleTypeTxt.getText().toString(), vehicleModelTxt.getText().toString());
        return new User(fullNameTxt.getText().toString(), Integer.parseInt(phoneNumberTxt.getText().toString()), car);
    }


}
