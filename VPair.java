public class VPair extends Edge{
    private int stops;

    public VPair(String airportCode1, String airportCode2, int weight, int ui, int vi, int stops) {
        super(airportCode1, airportCode2, weight);

        setUIndex(ui);
        setVIndex(vi);
        this.stops = stops;
    }

    public void setStops(int stops ){
        this.stops = stops;
    }

    public int getStops() {
        return stops;
    }
}
