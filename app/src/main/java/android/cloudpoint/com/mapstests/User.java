package android.cloudpoint.com.mapstests;

public class User {

    private String fullName;
    private int phoneNumber;
    private Vehicle vehicle;

    public User() {
    }

    public User(String fullName, int phoneNumber, Vehicle vehicle) {
        this.fullName = fullName;
        this.phoneNumber = phoneNumber;
        this.vehicle = vehicle;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", phoneNumber=" + phoneNumber +
                ", vehicle=" + vehicle +
                '}';
    }
}
