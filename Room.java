import java.util.UUID;

public class Room {

    private int type;
    private String name;
    private Event event;
    private String id;

    public boolean isResearved()
    {
        return researved;
    }

    public void setResearved(boolean researved)
    {
        this.researved = researved;
    }

    private boolean researved = false;

    Room(int type, Player player){
        setType(type);

        this.id = UUID.randomUUID().toString();
        if(type == 1){
            Main.safes.add(new Safe(id));
        }
        this.event = new Event(type, player, id);
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public Event getEvent() {
        return event;
    }

    public void setType(int type) {
        this.type = type;

        if(type == 0 || type == 7) {
            name = "Wand";
        } else {
            name = "Tür";
        }
    }

    public String getId() {
        return id;
    }
}
