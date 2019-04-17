package android.cloudpoint.com.mapstests;

public class Vehicle {

    private String vehicleType, vehicleModel;

    public Vehicle() {
    }

    public Vehicle(String vehicleType, String vehicleModel) {
        this.vehicleType = vehicleType;
        this.vehicleModel = vehicleModel;
    }

    public String getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(String vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getVehicleModel() {
        return vehicleModel;
    }

    public void setVehicleModel(String vehicleModel) {
        this.vehicleModel = vehicleModel;
    }

    @Override
    public String toString() {
        return "Vehicle{" +
                "vehicleType='" + vehicleType + '\'' +
                ", vehicleModel='" + vehicleModel + '\'' +
                '}';
    }
}
