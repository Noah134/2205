import java.awt.*;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.Console;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

// Test

public class Main {

    private final int width = 20;
    private final int height = 20;

    private int wall_damage;

    public static boolean game_over = false;
    public static List<Safe> safes = new ArrayList<>();

    Room[][] rooms = new Room[width+1][height+1];

    private Player player = new Player();

    private void ini()
    {
        Random random = new Random();
        boolean bRunning = true;

        ///////////////////////////////////////////// story making
        int storyRoomCount = 4; // NUMBER OF STORIES

        int[][] srooms = new int[storyRoomCount][2];
        while (bRunning)
        {
            // Create
            for (int i = 0; i < storyRoomCount - 1; ++i) // YES -1 IS IMPORTANT
            {
                srooms[i][0] = random.nextInt(width);
                srooms[i][1] = random.nextInt(height);

                if (srooms[i][0] == 0 && srooms[i][1] == 0)
                {
                    --i;
                }
            }

            // Check
            bRunning = false;
            BEXIT:
            for (int topPos = 0; topPos < srooms.length; ++topPos)
                for (int botPos = 0; botPos < srooms.length; ++botPos)
                {
                    if (topPos != botPos)
                        if (Math.abs(srooms[topPos][0] - srooms[botPos][0]) +
                                Math.abs(srooms[topPos][1] - srooms[botPos][1]) <
                                (storyRoomCount * 4) / (width * (height / width)))
                        {
                            bRunning = true;
                            break BEXIT;
                        }
                }
        }

        rooms[0][0] = new Room(5, player);
        for (int[] storyRoom : srooms)
        {
            rooms[ storyRoom[0] ][ storyRoom[1] ] = new Room(5, player);
        }

        ///////////////////////////////////////////// secret making

        // FOR DIFFERENT OCCURRENCY CHANGE THE LAST NUMBER
        int secretRoomCount = (width * height) / 100;
        int keyRoomCount = storyRoomCount;

        /*

        0 == SECRECT ROOM GEN
        1 == KEY ROOM GEN

         */
        for (int rco = 0; rco < 2; ++rco)
        {
            int[][] crooms;
            int thisRoomCount;

            if (rco == 0)
            {
                crooms = new int[secretRoomCount][2];
                thisRoomCount = secretRoomCount;
            }
            else // if (rco == 1)
            {
                crooms = new int[keyRoomCount][2];
                thisRoomCount = storyRoomCount;
            }

            bRunning = true;
            while (bRunning)
            {
                for (int i = 0; i < thisRoomCount; ++i)
                {
                    crooms[i][0] = random.nextInt(width);
                    crooms[i][1] = random.nextInt(height);

                    if (rooms[crooms[i][0]][crooms[i][1]] != null)
                    {
                        --i;
                    }
                }

                bRunning = false;
                BEXIT:
                for (int topPos = 0; topPos < crooms.length; ++topPos)
                    for (int botPos = 0; botPos < crooms.length; ++botPos)
                    {
                        if (topPos != botPos)
                            if (Math.abs(crooms[topPos][0] - crooms[botPos][0]) +
                                    Math.abs(crooms[topPos][1] - crooms[botPos][1]) <
                                    (thisRoomCount * 3) / (width * (height / width)))
                            {
                                bRunning = true;
                                break BEXIT;
                            }
                    }
            }

            if (rco == 0)
            {
                for (int[] secretRoom : crooms)
                {
                    rooms[secretRoom[0]][secretRoom[1]] = new Room(7, player);
                }
            }
            else if (rco == 1)
            {
                for (int i = 0; i < crooms.length; ++i)
                {
                    rooms[ crooms[i][0] ][ crooms[i][1] ] = new Room(8, player);
                    rooms[ crooms[i][0] ][ crooms[i][1] ].KEYBYTE = (byte) Math.pow(2, i);
                }
            }
        }

        ///////////////////////////////////////////// Rest

        for (int pos = 0; pos < rooms.length * rooms[0].length; ++pos)
        {
            if (rooms[pos % rooms.length][pos / rooms.length] == null)
            {
                rooms[pos % rooms.length][pos / rooms.length] = new Room(0, player);
            }
        }

        /**

         0 = wall
         1 = safe
         2 = fight
         3 = passive
         4 = treasure
         5 = story
         6 = shop
         7 = secret
         8 = KeyRoom

         */

        // possibilitiesSum HAS TO BE THE SUM OF ALL FIRST VALUES FROM possibilities
        int possibilitiesSum = 1000;
        int[][] possibilities =
                {
                        {300, 0}, // WALL
                        {100, 1}, // SAFE
                        {150, 2}, // FIGHT
                        {350, 3}, // PASSIVE
                        {60,  4}, // TREASURE
                        {40,  6}, // SHOP
                };

        bRunning = true;
        while (bRunning)
        {
            int rand = 0;
            for (Room[] roomArray : rooms) for (Room room : roomArray)
            {
                if (room.getType() != 7 &&
                        room.getType() != 5 &&
                        room.getType() != 8)
                {
                    rand = Math.abs(random.nextInt(possibilitiesSum));

                    int probability = 0;
                    for (int[] posb : possibilities)
                    {
                        if (rand <= (probability += posb[0]))
                        {
                            room.setType(posb[1]);
                            break;
                        }
                    }
                }

                if (room.isResearved())
                {
                    room.setResearved(false);
                }
            }

            bRunning = recrusiveWayCheck(0, 0, 0, (byte) 0) != storyRoomCount;
        }
    }

    private int recrusiveWayCheck(int x, int y, int MAXBEFORE, byte TKEYBYTE){
        if (x < 0 || y < 0 || x >= width || y >= height ||
                !rooms[x][y].getName().equals("Tür"))
        {
            return 0;
        }

        if (rooms[x][y].isResearved() &&
                rooms[x][y].getMAXKEYSHERE() >= MAXBEFORE)
        {
            return 0;
        }

        rooms[x][y].setResearved(true);

        if (rooms[x][y].getType() == 8 &&
                (rooms[x][y].KEYBYTE & TKEYBYTE) == 0)
        {
            ++MAXBEFORE;

            TKEYBYTE |= rooms[x][y].KEYBYTE;
        }

        rooms[x][y].setMAXKEYSHERE(MAXBEFORE);

        int found = 0;
        if (x != 0 && y != 0 &&
                rooms[x][y].getType() == 5)
        {
            if (MAXBEFORE > 0)
            {
                --MAXBEFORE;
                ++found;
            }
            else
            {
                return 0;
            }
        }

        found += recrusiveWayCheck(x + 1, y, MAXBEFORE, TKEYBYTE);
        found += recrusiveWayCheck(x - 1, y, MAXBEFORE, TKEYBYTE);
        found += recrusiveWayCheck(x, y + 1, MAXBEFORE, TKEYBYTE);
        found += recrusiveWayCheck(x, y - 1, MAXBEFORE, TKEYBYTE);

        return found;
    }

    private void printMap(Player p){
        boolean shown = false;
        System.out.println("MAP | S = Spieler ; X = Ziel ; M = Mauer");
        for(int i = height; i >= 0; i--){
            for(int j = width; j >= 0; j--){
                if(i == p.getPos_x() && j == p.getPos_y()){
                    System.out.print("S");
                } else if(rooms[i][j].getType() == 5 && !shown){
                    System.out.print("X");
                    shown = true;
                } else if(rooms[i][j].getType() == 0){
                    System.out.print("M");
                } else {
                    System.out.print(" ");
                }
                System.out.print(" ");
            }
            System.out.println();
        }
    }

    private void printRooms(Player p){
        int x = p.getPos_x();
        int y = p.getPos_y();
        System.out.println("Vor dir befindet sich eine " + (y < height ? rooms[x][y+1].getName() : "Wand") +
                ", Links eine " + (x > 0 ? rooms[x-1][y].getName() : "Wand") +
                ", Rechts eine " + (x < width ? rooms[x+1][y].getName() : "Wand") +
                " und hinter dir eine " + (y > 0 ? rooms[x][y-1].getName() : "Wand") + ".");
    }

    private boolean check(int x, int y) {
        if(rooms[x][y].getType() == 5 && player.getKeys() <= 0 && rooms[x][y].isLocked()){
            Event.printText("Diese Tür ist verschlossen. Finde zunächst einen passenden Schlüssel um diese zu öffnen.");
            return false;
        } else if(rooms[x][y].getType() == 5 && player.getKeys() > 0 && rooms[x][y].isLocked()){
            rooms[x][y].setLocked(false);
            Event.printText("Du konntest die Tür mit dem Schlüssel öffnen, jedoch ist dieser leider abgebrochen und somit nicht mehr verwendbar.");
        }
        return x >= 0 && x <= width && y >= 0 && y <= height && rooms[x][y].getType() != 0;
    }

    private void movementError(){
        Random r = new Random();
        String s = null;
        switch (r.nextInt(5)){
            case 0:
                s = "Nein! Das geht leider nicht..";
                break;
            case 1:
                s = "Hier geht es leider nicht lang..";
                break;
            case 2:
                s = "Schade! Ich dachte schon hier kann man lang gehen.";
                break;
            case 3:
                s = "Hmm.. Hier scheint nichts zu sein.";
                break;
            case 4:
                s = "Better luck next time!";
                break;
        }

        Event.printText(s);
        player.setHealth( player.getHealth() - wall_damage );
    }

    private Main(){
        Scanner s = new Scanner(System.in);

        ini();

        Event.printText("Willkommen bei 2 2 0 5. Einem auf Text basiertem Open-World-Game.\n" +
                "Wähle einen Schwierigkeitsgrad:\n" +
                "[1] EINFACH: Du startest mit viel Sauerstoff, Monster können gemütlich besiegt werden und du kriegst keinen Schaden wenn du gegen Wände läufst.\n" +
                "[2] NORMAL: Du startest mit genug Sauerstoff, Monster können relativ einfach besiegt werden und du kriegst etwas Schaden wenn du gegen Wände läufst.\n" +
                "[3] SCHWER: Du startest mit einem knappen Vorrat an Sauerstoff, musst dich anstrengen um Monster zu besiegen und du verletzt dich stark, wenn du gegen eine Wand läufst.");
        Event.printText("Wie möchtest du spielen? [1|2|3] ", 30, false);

        String difficulty = s.nextLine();
        boolean printMap = false;
        switch (difficulty){
            case "1":
                player.setOxygen(1500);
                Event.maxReactionTime = 2000;
                wall_damage = 0;
                printMap = true;
                break;
            case "2":
                player.setOxygen(1000);
                Event.maxReactionTime = 1500;
                wall_damage = 5;
                break;
            case "3":
                player.setOxygen(750);
                Event.maxReactionTime = 1000;
                wall_damage = 40;
                break;
            default:
                player.setOxygen(1000);
                Event.maxReactionTime = 1500;
                wall_damage = 5;
                break;
        }

        Event.cls(false);

        //rooms[0][0].getEvent().execute();

        Event.printText("Du kannst dich ab sofort frei auf der Map bewegen. Und Übrigens: Wände sind nicht immer Wände ;)");

        while (!game_over)
        {

            if(player.getHealth() <= 0 || player.getOxygen() <= 0){
                game_over = true;
                break;
            }

            int x, y;
            String input;
            Event.cls(false);
            if(printMap) printMap(player);
            System.out.println("Aktueller Sauerstoffgehalt: " + player.getOxygen());
            printRooms(player);

            do {
                Event.printText("Wohin möchtest du gehen? ", 30, false);

                input = s.nextLine().toLowerCase(Locale.GERMAN);
                x = player.getPos_x();
                y = player.getPos_y();

                if (input.matches(Debug.DEBUG_PREFIX + ".*"))
                {
                    String err = Debug.resolve_debug(input.substring(4), player, rooms);
                    if (err != null)
                        System.out.println("DEBUG&" + err + ";");
                }
            } while (input.matches(Debug.DEBUG_PREFIX + ".*"));

            if(input.contains("links")){
                if(check(x-1, y)) {
                    player.setPos_x(x - 1);
                } else {
                    movementError();
                }
            } else if(input.contains("rechts")){
                if(check(x+1,y)) {
                    player.setPos_x(x + 1);
                } else {
                    movementError();
                }
            } else if(input.contains("vor") || input.contains("geradeaus")){
                if(check(x, y+1)) {
                    player.setPos_y(y + 1);
                } else {
                    movementError();
                }
            } else if(input.contains("hinter") || input.matches("\\s*zur.ck\\s*")){
                if(check(x, y-1)) {
                    player.setPos_y(y - 1);
                } else {
                    movementError();
                }
            } else {
                System.out.println("Das habe ich nicht verstanden..");
                continue;
            }
            player.setOxygen(player.getOxygen() - 10);
            rooms[player.getPos_x()][player.getPos_y()].getEvent().execute();
        }
    }

    public static void main(String[] args){
        if(System.getProperty("os.name").startsWith("Windows")) {
            Console console = System.console();
            if (console == null && !GraphicsEnvironment.isHeadless()) {
                String filename = Main.class.getProtectionDomain().getCodeSource().getLocation().toString().substring(6);
                try {
                    Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start", "cmd", "/k", "java -jar \"" + filename + "\""});
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        new Main();
    }
}
