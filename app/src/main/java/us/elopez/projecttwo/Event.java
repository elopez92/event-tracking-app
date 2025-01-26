package us.elopez.projecttwo;

public class Event {
    private int id;
    private String name;
    private String datetime;

    public Event(int id, String name, String datetime) {
        this.id = id;
        this.name = name;
        this.datetime = datetime;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDatetime() {
        return datetime;
    }
}
