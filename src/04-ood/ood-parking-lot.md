
这里好像也没啥 就核心两点，第一注意设计 enum Spot 给每个一个 tier，这样会比较好扩展。
第二是用 queue 管理每种车很好，需要讨论的是我们是不是需要

我们先想想下面的设计有什么缺憾？
就是牺牲了扩展性。
这一类容积问题，什么不会变？一般来说尺寸固定了就不会变了。
所以 enum SpotSize 确实是很不错的。
但是 要注意 普通车位，残parking, 电车，vip是动态添加的属性。
为什么这么复杂，究其原因是因为问题设计的不好。因为我们不管一个车位大还是小。
应该对 regular, 残疾，电车，vip 搞分别的 manager. 而不是按 size 区分。

这里最重要的是，其实 SpotManager 只需要 Manage Spot 就好了。
就不要管你是什么 SpotManager, 这个在 ParkingLocation 里用一个 Map 就解决的问题。你只需要解决


```java
// "static void main" must be defined in a public class.

// Parking Building
// Spot, Spot has 3 SpotSize, and handicapped
// Ticket if service. 
// Vehicle, should have a field for Ticket.
// Chanllenge here is that if we want to have multiple 

class ParkingLocation {
    private Map<SpotType, SpotManager> managers;
    // 最好在这里用一个 Map<Vehicle, Spot> parkedVehicles
    public ParkingLocation(Map<SpotType, Integer> input) {
        this.managers = new HashMap<>();
        for (Map.Entry<SpotType, Integer> entry : input.entrySet()) {
            managers.put(entry.getKey(), new SpotManager(entry.getKey(), entry.getValue()));
        }
    }
    
    public boolean parkVehicle(Vehicle vehicle){
        SpotType spotType = findBestVehicleSpotType(vehicle);
        if (spotType == null) return false;
        SpotManager manager = managers.get(spotType);
        manager.assignSpot(vehicle);
        return true;
    }
    
    public void releaseVehicle(Vehicle vehicle) {
        SpotManager manager = managers.get(vehicle.getSpot().getSpotType());
        manager.recycleSpot(vehicle);
    }
    
    private SpotType findBestVehicleSpotType(Vehicle vehicle) {
        SpotType[] spotTypes = SpotType.values(); // note how to get all enum values
        Arrays.sort(spotTypes, (a, b) -> a.getTier() - b.getTier());
        for (SpotType spotType : spotTypes) {
            // if has avalible spot
            if (managers.get(spotType).getSpotLeft() == 0) continue;
            // if spot type fit
            if (canVehicleFitSpotType(spotType, vehicle)) return spotType;
        }
        return null;
    }
    
    private boolean canVehicleFitSpotType(SpotType spotType, Vehicle vehicle) {
        double[] spotTypeDimensions = spotType.getDimension();
        double[] vehicleDimensions = vehicle.getDimension();

        for (int i = 0; i < spotTypeDimensions.length; i++) {
            if (spotTypeDimensions[i] < vehicleDimensions[i]) {
                return false;
            }
        }
        return true;
    }
    
    public void print() {
        for (SpotType key : managers.keySet()) {
            System.out.println(key.name() + ' ' + managers.get(key).getSpotLeft());
        }
    }
}

class SpotManager {
    private ArrayDeque<Spot> queue;
    private SpotType spotType;
    private int maxCapacity;
    
    public SpotManager(SpotType spotType, int maxCapacity) {
        this.spotType = spotType;
        this.maxCapacity = maxCapacity;
        this.queue = new ArrayDeque<>();
        for (int i = 0; i < maxCapacity; i++) {
            queue.offer(new Spot(spotType, i));
        }
    }

    public int getSpotLeft() {
        return queue.size();
    }

    public void assignSpot(Vehicle vehicle) {
        Spot spot = queue.poll();
        vehicle.setSpot(spot);
    }
    
    public void recycleSpot(Vehicle vehicle) {
        Spot spot = vehicle.getSpot();
        vehicle.setSpot(null);
        queue.offer(spot);
    }
    // spotManager should not worry about ticket
}


enum SpotType {
    SMALL(1, 4.2, 3.4),
    MEDIUM(2, 5.4, 5.2),
    LARGE(3, 8.2, 6.2);
    // HANDICAPPED(-1, 10, 10);
    
    private int tier;
    private double[] dimension;
    SpotType(int tier, double width, double length){
        this.tier = tier;
        this.dimension = new double[]{width, length};
        Arrays.sort(dimension);
    }
    
    public int getTier() {return tier;}
    public double[] getDimension() {return dimension;}
}


class Spot {
    private SpotType spotType;
    private int spotId;
    private boolean occupied; // 不需要这个其实 过于复杂了
    public Spot(SpotType spotType, int spotId) {
        this.spotType = spotType;
        this.spotId = spotId;
    }
    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }
    public boolean getOccupied() {return occupied;}
    public int getSpotId() {return spotId;}
    public SpotType getSpotType(){return spotType;}
}

class Vehicle {
    private double[] dimension;
    private Spot spot;
    
    public Vehicle (Builder builder) {
        this.dimension = builder.dimension;
    }
    
    static class Builder {
        private double[] dimension;
        
        Builder(){};
        
        public Builder addDimension(double[] dimension) {
            this.dimension = dimension;
            return this;
        }

        public Vehicle build() {
            if (dimension == null) throw new IllegalArgumentException("wrong parameters!");
            Arrays.sort(dimension);
            return new Vehicle(this);
        }
    }
    
    
    public Spot getSpot() {return this.spot;}
    public void setSpot(Spot spot) {this.spot = spot;}
    public double[] getDimension(){return this.dimension;}
}


// if we need to calculate the fee, we can add a ticket class.
class Ticket {
    
}

public class Main {
    public static void main(String[] args) {
        Map<SpotType, Integer> input = new HashMap<>();
        input.put(SpotType.SMALL, 2);
        input.put(SpotType.MEDIUM, 2);
        input.put(SpotType.LARGE, 2);
        ParkingLocation location = new ParkingLocation(input);
        Vehicle big = new Vehicle.Builder().addDimension(new double[]{6.1, 8.1}).build();
        
        location.parkVehicle(big);
        location.print();
        location.parkVehicle(big);
        location.print();
        location.releaseVehicle(big);
        location.print();
    }
}
```