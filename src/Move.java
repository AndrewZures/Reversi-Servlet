/**
 * Created with IntelliJ IDEA.
 * User: andrewzures
 * Date: 8/25/13
 * Time: 8:47 PM
 * To change this template use File | Settings | File Templates.
 */
class Move {
    private int id;
    private String color;
    private String location;

    Move(String color, String loc) {
        this.color = color;
        this.location = loc;
    }

    public String getLocation() {
        return this.location;
    }

    public String getColor() {
        return this.color;
    }
}
