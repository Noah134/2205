import java.io.File;
import java.io.IOException;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class Event {
    private boolean isVoid;
    private int room_type;
    private Player player;
    private boolean executed;
    private String roomID;
    public static int maxReactionTime = 1500;

    public void setType(int type)
    {
        this.room_type = type;
        this.isVoid = type == 0;
    }

    Event(int room_type, Player player, String roomID){
        this.isVoid = room_type == 0;
        this.room_type = room_type;
        this.player = player;
        this.roomID = roomID;
    }

    public boolean getExecuted()
    {
        return executed;
    }

    public void forceExecute(boolean b)
    {
        executed = b;
    }

    void execute(){
        if(!isVoid){
            switch (room_type){
                case 1:
                    safe();
                    break;
                case 2:
                    if(!executed)
                    {
                        fight();
                    }
                    else
                    {
                        passive();
                    }

                    break;
                case 3:
                    passive();
                    break;
                case 4:
                    if(!executed)
                    {
                        treasure();
                    }
                    else
                    {
                        printText("Die Kiste in diesem raum ist schon lehr.");
                    }

                    break;
                case 5:
                    if(!executed)
                    {
                        story();
                    }
                    else
                    {
                        printText("Du glaubst hier schonmal gewesen zu sein.");
                    }

                    break;
                case 6:
                    shop();
                    break;
                case 7:
                    if(!executed)
                    {
                        secret();
                    }
                    else
                    {
                        passive();
                    }

                    break;
                case 8:
                    if(!executed)
                    {
                        key();
                    }
                    else
                    {
                        passive();
                    }

            }
            executed = true;
        }
    }

    void typhon(int n, int ms){
        printText("ACHTUNG! Du befindest dich in einem Raum mit einem Typhon!");
        for (int i = 0; i < n;){
            char key = (char) ThreadLocalRandom.current().nextInt(97,123);
            char capKey = (char) (key - 32);

            printText("Drücke schnell " + key);
            try {
                long t1 = System.currentTimeMillis();
                char input = (char) RawConsoleInput.read(true);
                if(input == key || input == capKey){
                    long t2 = System.currentTimeMillis();
                    if((t2 - t1) < ms){
                        i++;
                        printText("Du konntest dem Typhon schaden!");
                    } else {
                        player.setHealth(player.getHealth() - 15);
                        printText("Typhon hat dich attackiert!");
                    }
                } else {
                    player.setHealth(player.getHealth() - 15);
                    printText("Typhon hat dich attackiert!");
                }
                RawConsoleInput.resetConsoleMode();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        printText("Du hast den Typhon getötet!");
    }

    void fight(){
        Main.playSound("sounds/typhon.wav");
        player.setOxygen(player.getOxygen() - 20);
        int enemyType = ThreadLocalRandom.current().nextInt(0,100);
        if(enemyType >= 0 && enemyType <= 70){//Typhon
            typhon(3, maxReactionTime);
        } else if(enemyType > 70 && enemyType <= 90){//Typhone mit Raumanzug
            printText("ACHTUNG! Du befindest dich in einem Raum mit einem Typhon mit Raumanzug!");
            boolean fighting = true;
            while (fighting) {
                int number = ThreadLocalRandom.current().nextInt(1000, 100000);
                printText("Merke dir folgenden Deaktivierungscode: " + number);
                printText("");
                char key = (char) ThreadLocalRandom.current().nextInt(97, 123);
                char capKey = (char) (key - 32);

                printText("Drücke schnell " + key);
                try {
                    long t1 = System.currentTimeMillis();
                    char input = (char) RawConsoleInput.read(true);
                    if (input == key || input == capKey) {
                        long t2 = System.currentTimeMillis();
                        if ((t2 - t1) < maxReactionTime) {
                            printText("Du konntest dem Typhon schaden!");
                            boolean correct = false;
                            while (!correct){
                                String key1 = String.valueOf((char) ThreadLocalRandom.current().nextInt(97,123));
                                String key2 = String.valueOf((char) ThreadLocalRandom.current().nextInt(97,123));
                                String key3 = String.valueOf((char) ThreadLocalRandom.current().nextInt(97,123));
                                System.out.print(key1 + " " + key2 + "" + key3);
                                correct = key1.equals(key2) && key2.equals(key3);
                            }
                            System.out.println();
                            printText("Mit dem Deaktivierungscode des Raumanzuges kannst du den Typhon entgültig eliminieren");
                            cls(false);
                            printText("Bitte hier eingeben: ");
                            if (new Scanner(System.in).nextLine().equals(String.valueOf(number))) {
                                printText("Du hast den Typhon getötet!");
                                fighting = false;
                            }
                        } else {
                            player.setHealth(player.getHealth() - 25);
                            printText("Typhon hat dich attackiert!");
                        }
                    } else {
                        player.setHealth(player.getHealth() - 25);
                        printText("Typhon hat dich attackiert!");
                    }
                    RawConsoleInput.resetConsoleMode();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {//Typhone mit Waffe
            printText("Hier befindet sich ein Typhon mit Waffe. Möchtest du fliehen oder kämpfen?");
            Scanner s = new Scanner(System.in);
            String input = s.nextLine().toLowerCase(Locale.GERMAN);
            if(input.matches("\\s*k.mpfen\\s*")){
                int chances = ThreadLocalRandom.current().nextInt(0,5) + (player.isWeapon() ? 30 : 0) + (player.getLuck() / 2) + (player.isArmor() ? 10 : 0) + player.getHealth() / 4;
                if(chances >= 75){
                    printText("Deine Chancen standen gut. Du konntest den Typhon erfolgreich eliminieren.");
                    int money_found = ThreadLocalRandom.current().nextInt(0,1000);
                    printText("Der Typhon hatte " + money_found + "$, eine Sauerstoffflasche und eine Waffe.");
                    printText("Möchtest du die Gegenstände mitnehmen? ", 30, false);
                    String input_objects = s.nextLine().toLowerCase(Locale.GERMAN);
                    if(input_objects.contains("ja")){
                        player.setMoney(player.getMoney() + money_found);
                        player.setWeapon(true);
                        player.setLuck((player.getLuck() < 10 ? 11 : player.getLuck()) - 10);
                    } else if(input_objects.contains("nein")){
                        printText("Ok.");
                    } else {
                        printText("Das habe ich nicht verstanden.");
                    }
                } else {
                    printText("Deine Chancen standen nicht gut. Der Typhon hat dich getötet.");
                    Main.game_over = true;
                }
            }
        }
    }

    void buy(int price, String name, String plural, int oxygen, int health, int luck, boolean weapon, boolean armor){
        printText("Gebe eine Stückzahl ein: ", 30, false);
        int stueckzahl = 1;
        try {
            stueckzahl = Integer.parseInt(new Scanner(System.in).nextLine());
        } catch (Exception e){/*Default Stueckzahl wird verwendet*/}
        if(stueckzahl < 1) stueckzahl = 1;
        price *= stueckzahl;
        if(player.getMoney() >= price){
            Main.playSound("sounds/coins.wav");
            player.setMoney(player.getMoney() - price);
            player.setOxygen(player.getOxygen() + oxygen);
            player.setHealth(player.getHealth() + health);
            player.setLuck(player.getLuck() + luck);
            player.setWeapon(player.isWeapon() || weapon);
            player.setArmor(player.isArmor() || armor);
            printText("Du hast " + stueckzahl + " " + name + (stueckzahl > 1 ? plural : "") + " gekauft.");
        } else {
            printText("Du hast nicht genug Münzen!");
        }
    }

    void shop(){
        printText("Vor dir befindet sich ein Automat.");
        printText("Es gibt hier noch Sauerstoffflaschen, Snacks, Medikamente, Waffen, Glücksbringer und Rüstungen.");
        printText("Du hast aktuell " + player.getMoney() + " Münzen. Möchtest du etwas kaufen? ", 30, false);
        Scanner s = new Scanner(System.in);
        if(s.nextLine().toLowerCase(Locale.GERMAN).contains("ja")){
            printText("Was darf es denn sein? ", 30, false);
            String input = s.nextLine().toLowerCase(Locale.GERMAN);
            if(input.contains("sauerstoff")){
                buy(10, "Sauerstoffflasche", "n", 100, 0, 0, false, false);
            } else if(input.contains("snack")){
                buy(30, "Snack", "s", 0, 15, 0, false, false);
            } else if(input.contains("med")){
                buy(70, "Medikament", "e", 0, 48, 0, false, false);
            } else if(input.contains("waffe")){
                buy(200, "Waffe", "n", 0, 0, 0, true, false);
            } else if(input.matches("\\s*gl.ck\\s*")){
                buy(800, "Glücksbringer", "", 0, 0, 25, false, false);
            } else if(input.contains("\\s*r.stung\\s*")){
                buy(1000, "Rüstung", "en", 0, 0, 0, false, true);
            }
        } else {
            printText("Ok.");
        }
    }
    void safe(){
        boolean bDoRun = true;
        for(Safe safe : Main.safes){
            if(safe.getId().equals(roomID) && !safe.isOpen()){
                bDoRun = false;
                safe.setOpen(true);
                printText("Hier befindet sich ein Safe mit der ID " + safe.getPublicID() + "\nMöchtest du einen Code eingeben?");
                Scanner s = new Scanner(System.in);
                String input = s.nextLine().toLowerCase(Locale.GERMAN);
                if (input.contains("ja")) {
                    printText("Code: ", 30, false);
                    if (Integer.parseInt(s.nextLine()) == safe.getCode()) {
                        printText("Du hast den Safe geöffnet!");
                        printText("Du hast " + safe.getMoney() + "$, " + safe.getOxygen() + " an Sauerstoff und " + safe.getMeds() + " Medikament(e) gefunden.");
                        player.setMoney(player.getMoney() + safe.getMoney());
                        player.setOxygen(player.getOxygen() + safe.getOxygen());
                        player.setHealth(player.getHealth() + safe.getMeds() * 48);
                        if (player.getHealth() > 100) {
                            player.setHealth(100);
                        }
                        safe.setOpen(true);
                    } else {
                        printText("Der Code ist leider falsch!");
                    }
                } else if (input.contains("nein")) {
                    printText("Ok.");
                }
            }
        }
        if (bDoRun)
        {
            passive();
        }
    }

    void treasure(){
        // 1. bit = oxygen
        // 2. bit = money
        // 3. bit = weapon
        // 4. bit = Lucky charm
        byte treasure = 0;
        treasure |= ThreadLocalRandom.current().nextInt(0, 4) == 0 ? 1 : 0;
        treasure |= ThreadLocalRandom.current().nextInt(0, 4) == 0 ? 2 : 0;
        treasure |= ThreadLocalRandom.current().nextInt(0, 4) == 0 ? 4 : 0;
        treasure |= ThreadLocalRandom.current().nextInt(0, 4) == 0 ? 8 : 0;

        if (treasure == 0)
        {
            treasure |= (byte) Math.pow(2,
                    ThreadLocalRandom.current().nextInt(0, 3));
        }

        int chanceOxygen = ThreadLocalRandom.current().nextInt(0, 400);
        int chanceMoney = ThreadLocalRandom.current().nextInt(0,100);
        int chanceWeapon = ThreadLocalRandom.current().nextInt(0,100);
        int chanceLuckyCharm = ThreadLocalRandom.current().nextInt(0,100);

        if((treasure & 1) != 0 && chanceOxygen >= 0 && chanceOxygen <= 80){
            printText("Du hast hier Sauerstoff gefunden!");
            player.setOxygen(player.getOxygen() + chanceOxygen);
        }

        if((treasure & 2) != 0 && chanceMoney >= 0 && chanceMoney <= 75){
            int money = 20 * player.getLuck();
            printText("Du hast hier " + money + "$ gefunden!");
            Main.playSound("sounds/coins.wav");
            player.setMoney(player.getMoney() + money);
        }

        if((treasure & 4) != 0 && (chanceWeapon >= 0 && chanceWeapon <= 50) && !player.isWeapon()){
            printText("Du hast hier eine Waffe gefunden!");
            player.setWeapon(true);
        }

        if((treasure & 8) != 0 && chanceLuckyCharm >= 0 && chanceLuckyCharm <= 35){
            printText("Du hast hier einen Glücksbringer gefunden!");
            player.setLuck(player.getLuck() + 25);
        }
    }
    void story(){
        Main.playSound("sounds/story.wav");
        player.setOxygen(player.getOxygen() - 50);
        int chapter = player.getStory_chapter();
        player.setStory_chapter(chapter + 1);
        Scanner s = new Scanner(System.in);
        if(chapter == 0){
            printText("Du wachst aus einem Hyperschlaf auf.\n" +
                    "Du befindest dich auf der Talos-1-Spacestation.\n" +
                    "Alle deine Team Kollegen befinden sich noch im Hyperschlaf. Aber wieso...");
            boolean chapter_complete = false;
            while (!chapter_complete){
                printText("Was möchtest du tun/wissen? ", 30, false);
                String input = s.nextLine().toLowerCase(Locale.GERMAN);
                if(input.contains("ich")){
                    printText("Du arbeitest hier mit den besten Wissenschaftlern der Erde zusammen, du bist einer von ihnen, ein hochbegabter Physiker und Techniker. Dein Name: Morgan.\n");
                } else if(input.contains("mission")){
                    printText("Die Mission war es durch einen Hyperschlaf mehrere Jahre in die Zukunft zu reisen ohne zu altern um einen langwierigen Forschungsprozess zu überspringen.\n" +
                            "Während des Schlafens soll ein Programm den Forschungsprozess automatisieren.");
                } else if(input.contains("forschung")){
                    printText("Die Erforschung einer neuen Lebensform, die Typhon. Normalerweise dauert die Analyse des Gewebes einer Lebensform nur ein bis zwei Tage doch das Gewebe der Typhon ist viel komplexer und erfordert daher mehrere Fachgebiete zum Analysieren was die Forschung auf mehrere Jahre strecken könnte. Viel zu viele Jahre...dafür war keine Zeit.");
                } else if(input.contains("schlaf")){
                    printText("Du schaust dir deine Hyperschlaf-Kapsel genauer an. Ein Fehler! Du bist 100 Jahre zu früh aufgewacht! Geplant war das Jahr 2205 doch du bist im Jahr 2105 aufgewacht. 2032 war das Jahr in dem sich alle in den Hyperschlaf gestürzt haben.");
                } else if(input.contains("gehe") && input.contains("schlaf")){
                    printText("Funktioniert nicht! Die Hyperschlaf_Kapsel ist defekt. Wenn du diese Kapsel nicht zum Laufen bekommst oder eine neue funktionierende Kapsel findest könnte es dein Tod bedeuten, da du unmöglich 100 Jahre leben kannst.");
                } else if(input.contains("aufwecken")){
                    printText("Dir ist der Fehler Unbekannt. Du möchtest nicht riskieren, dass der gleiche Fehler auch bei deinen Team Kollegen auftritt und du jemanden mit in deine Situation hinein ziehst.");
                } else if(input.contains("raus")){
                    printText("Du entscheidest dich aus dem Hyperschlaf-Raum raus zugehen. Du öffnest die Tür und kannst deinen Augen nicht glauben. Alles ist verwüstet, überall sind schwarze Spuren an den Wänden, wie du sie eigentlich nur aus der Typhon-Forschung kennst...sind die Typhon ausgebrochen? Auf einem Informationsbildschirm erkennst du, dass es ebenfalls überall auf der Talos-1 Sauerstofflecks gibt. Du ziehst dir einen Raumanzug an dessen Sauerstoffversorgung noch 75% beträgt. Ebenfalls nimmst du vorerst als temporäre Bewaffnung eine Rohrzange die auf dem Boden zwischen den Trümmern herum lag.\n" +
                            "Der Raumanzug gibt ebenfalls Informationen über deinen Gesundheitlichen Status aus:\n" +
                            "Gesundheit = 100% | Krankheiten = Keine\n"+
                            "Der Raumanzug enthält außerdem einen Kompass.\n");
                    cls(true);
                    printText("Schwarze Flächen an der Wand, alles zertrümmert, Sauerstofflecks. Hier ist wirklich nichts von Interesse für dich, das Einzige was dir Sorgen macht sind diese Schwarzen Flecken.\n" +
                            "Diese Flecken sehen dem Gewebe der Typhons sehr ähnlich. Überall wo die Typhen hingehen, hinterlassen sie solche Flecken. Es macht dir Sorgen das sie hier im zerstörten Raum auch aufgetaucht sind.\n" +
                            "Die Typhons sind tatsächlich ausgebrochen. Hinter dir und im ganzen Raum schließen sich die Türen wegen einer Warnmeldung, die Technologie auf der Raumstation scheint auch schon beschädigt zu sein.");
                    chapter_complete = true;
                } else {
                    printText("Das geht leider nicht.. Du kannst aber mehr über dich, deine Mission oder der Hyperschlaf-Kapsel erfahren.");
                }
            }
        } else if(chapter == 1){
            printText("Nachdem du dich durch verschiedene Räumen der Talos-1 durchgekämpft hast, triffst du auf einen Raum der durch ein Kraftfeld geschützt ist. Zum Glück verfügst du über einen Sicherheitscode, der Kraftfelder ausschalten kann.\n" +
                    "Das Kraftfeld ist so etwas wie ein tödliches und dichtes Laserfeld das nichts hindurch lässt.\n" +
                    "Es wundert dich, dass du so ein Feld hier vorfindest, normalerweise können die nur manuell von Hand aufgestellt werden, und nicht durch das Sicherheitssystem.\n" +
                    "Es ist unmöglich durch dieses Feld irgendetwas zu erkennen.");
            boolean chapter_complete = false;
            while(!chapter_complete){
                printText("Was möchtest du tun/wissen? ", 30, false);
                String input = s.nextLine().toLowerCase(Locale.GERMAN);
                if(input.contains("code")){
                    printText("Der Code lautet 2-4-1-6.");
                } else if(input.contains("2416") || input.contains("2-4-1-6") || input.contains("2 4 1 6")){
                    printText("Du gibst den Code ein. Das Kraftfeld deaktiviert sich. Du kannst jetzt in den Raum gehen");
                } else if(input.contains("gehe") || input.contains("raum")){
                    printText("Du gehst in den Raum. Eine Person! Sie scheint sich selbst in einer Energie-Draht-Falle gefangen zu haben. Das Einzige was sie hat ist ein Laptop der vor ihr liegt und eine freie Hand. " +
                            "Der Raum scheint noch ganz unberührt von der Typhon Katastrophe zu sein. Es befindet sich ebenfalls ein Terminal im Raum... Ob sich damit die Identität der Person herausfinden lässt?");
                    boolean room_complete = false;
                    boolean has_informations = false;
                    while (!room_complete){
                        printText("Was möchtest du tun/wissen? ", 30, false);
                        String input_room = Normalizer.normalize(s.nextLine().toLowerCase(Locale.GERMAN), Normalizer.Form.NFKC);
                        if(input_room.contains("person")){
                            printText("Anhand der Uniform lässt sich erkennen, dass sie zu den Volontären auf der Talos-1 gehört.\n" +
                                    "Sie scheint ansprechbar zu sein und anhand eines Terminals und ihrer Volontär ID, die auf ihrem Anzug erkennbar ist, sollte es möglich sein mehr über sie heraus zu finden.");
                        } else if(input_room.matches("\\s*volont.r\\s*")){
                            printText("Volontäre, sind Freiwillige die ihren Körper für die Versuche und Experimente auf der Talos-1 zur Verfügung gestellt haben...also so etwas wie Laborratten.\n" +
                                    "Meistens handelt es sich um Strafgefangene die ihr Leben sowieso lebenslänglich im Gefängnis verbracht hätten.");
                        } else if(input_room.contains("id") || input_room.contains("genau")){
                            printText("Auf der Uniform steht die ID: V-010655-37");
                        } else if(input_room.contains("terminal") && input_room.contains("v-010655-37")){
                            printText("Name: Olivia\n" +
                                    "Nachname: Colomar\n" +
                                    "Straftat: Versuchter Hackerangriff auf die größte Bank der Welt.\n" +
                                    "\t\t13 weitere gelungene Hackerangriffe auf verschieden große Firmen.\n" +
                                    "\t\tDiebstahl von 50 Millionen Dollar.\n" +
                                    "\t\tDatendiebstahl an mehreren Millionen Personen.\n" +
                                    "Warnung:  keine Technischen Geräte im Radius von 10m vom Testsubjekt aufbewahren.");
                            has_informations = true;
                        } else if(input_room.contains("reden")){
                            if(has_informations){
                                printText("Olivia: \t\"Ich schätze du weißt schon, anhand meiner Akten, wer ich bin.\n" +
                                        "Ich wurde ebenfalls in den Hyperschlaf versetzt. Du wunderst dich, wieso ich auch schon wach bin.\nBevor man mich Schlafen lies hatte ich genug Zeit mich in ein anderes Terminal zu hacken und meine Schlafdauer zu verkürzen.\nNur leider ist mein Umgang mit Waffen miserabel und ich habe mich in\n" +
                                        "einer dieser Energie-Draht-Fallen selbst gefangen. Ich hatte eigentlich vor, mit einem Shuttle zu fliehen, aber bemerkte schon früh, dass ich die Sicherheitssystem-Software und das Kommunikationssystem\n" +
                                        "mit meinen Softwaremanipulationen zerschossen habe und damit eure Forschungssubjekte befreit habe. Ich habe dich ebenfalls aus dem Hyperschlaf geweckt und dir die richtigen Türen geöffnet damit du zu " +
                                        "mir kommst und mich befreist. Ich habe den Doktor dieser Raumstation dabei beobachtet mit welchen Code er die Tür zur Krankenstation öffnet und soweit ich weiß, ist der Operationstisch fähig einen Hyperschlaf\n" +
                                        "durchzuführen. Wenn du mich befreist, mir hilfst zum Shuttle zu kommen und  mir hilfst zu fliehen verrate ich dir den Code.\"");
                                printText("Olivia Colomar befreien? ", 30, false);
                                String input_room_decision = s.nextLine().toLowerCase(Locale.GERMAN);
                                if(input_room_decision.contains("ja")){
                                    player.setDecision(true);
                                    printText("Du befreist Olivia. Sie bedankt sich sehr und sagt, dass du es nicht bereuen wirst.\n" +
                                            "Sie öffnet auf ihrem Laptop eine Karte, berechnet den kürzesten Weg zum Shuttle und öffnet dir die richtigen Türen.\n");
                                } else if(input_room_decision.contains("nein")){
                                    player.setDecision(false);
                                    printText("Du reißt Olivia ihren Laptop aus der Hand, wirfst ihn gegen eine Wand, nachdem du dir eine Karte über die Raumstation heruntergeladen hast und lässt sie am Boden gefesselt liegen.\n" +
                                            "\tDein Neues Ziel: Das Shuttle finden um zur Erde zu fliegen und darüber zu Informieren was hier vor sich geht. Und noch während du den Raum verlässt, hat sie dich auf Spanisch auf 10 verschiedene Arten beleidigt.");
                                }
                                room_complete = true;
                                chapter_complete = true;
                            } else {
                                printText("Vielleicht solltest du dich lieber erst über sie Informieren...\nDu weißt nicht, wen du vor dir hast.");
                            }
                        } else {
                            printText("Das geht leider nicht.. Du kannst aber mehr über die Person, Volontäre oder deren ID erfahren..");
                        }
                    }
                } else {
                    printText("Das geht leider nicht.. Achte nochmal darauf was ich gesagt hatte.");
                }
            }

        } else if(chapter == 2){
            if(player.isDecision()) {
                printText("Ihr seid im Shuttle-Raum angekommen, doch es ist voll mit Typhons und es hat nicht lange gedauert bis sie euch entdeckt haben.");
                typhon(9, maxReactionTime);
                printText("Ihr habt es geschafft! Die meisten Typhons im Shuttle-Raum wurden besiegt. Und die, die noch übrig waren sind vor Angst geflohen.");
            } else {
                printText("Du bist im Shuttle-Raum angekommen doch drinnen ist ein riesiger Typhon-Phantom, es hat nicht lange gedauert bis er dich bemerkt hat und auf dich zu stürmt. Du konntest dem Ansturm noch grade so ausweichen. Es führt kein weg an ihm vorbei du musst kämpfen.");
                typhon(9, maxReactionTime - 200);
                printText("Du hast es geschafft! Als du den riesigen Typhon-Phantom besiegt hast und er sich vor dir vor schmerzen krümmend in Luft auflöst, fliehen die restlichen Typhons die noch im Raum waren vor angst. Dir steht der Shuttle-Raum jetzt frei zur Verfügung.");
            }

                boolean chapter_complete = false;
                boolean cmd_active = false;
                while(!chapter_complete) {
                    printText("Was möchtest du tun/wissen? ", 30, false);
                    String input = s.nextLine().toLowerCase(Locale.GERMAN);
                    if(input.contains("umsehen")){
                        if(player.isDecision()) {
                            printText("Ein riesiger Raum, so groß das drei Shuttle 's gleichzeitig in ihm Platz nehmen könnten, doch bereit stand nur ein Shuttle. Auf dem Shuttle steht gedruckt: „SHUTTLE-ADVENT“.\n" +
                                    "Vor dem Raumschiff befindet sich ein gigantisches und rundes Tor das ihr auf jeden Fall öffnen müsst aber vorher müsst ihr ein paar Bedingungen erfüllen. \n" +
                                    "Neben dir steht Olivia Colomar, sie lächelt dich die meiste Zeit an, es wirkt vertraulich aber, gleichzeitig so vertraulich das es wieder nicht vertraulich wirkt.\n" +
                                    "Ihr befindet euch zurzeit im Kontroll-Bereich des Shuttle-Raums.\n" +
                                    "Eine große Schaltfläche befindet sich vor euch. Und ein Notizzettel klebt neben dran.");
                        } else {
                            printText("Ein riesiger Raum, so groß das drei Shuttle 's gleichzeitig in ihm Platz nehmen könnten, doch bereit stand nur ein Shuttle. Auf dem Shuttle steht gedruckt: „SHUTTLE-ADVENT“.\n" +
                                    "Vor dem Raumschiff befindet sich ein gigantisches und rundes Tor das du auf jeden Fall öffnen müsst aber vorher musst du ein paar Bedingungen erfüllen. \n" +
                                    "Du befindest dich zurzeit im Kontroll-Bereich des Shuttle-Raums.\n" +
                                    "Eine große Schaltfläche befindet sich vor dir. Und ein Notizzettel klebt neben dran.");
                        }
                    } else if(input.contains("shuttle")){
                        printText("Das Schiff ist eindeutig einwandfrei, doch nicht erreichbar da die Brücke noch eingefahren ist.");
                    } else if(input.contains("tor")){
                        printText("Das Tor muss auf jeden Fall geöffnet werden, dies ist bestimmt mit der Schaltfläche möglich.");
                    } else if(input.contains("notiz") || input.contains("zettel")){
                        printText("Auf dem Notizzettel steht geschrieben:\n" +
                                "\t\"1. Brücke herunterfahren \n" +
                                "\t 2. Shuttle-Tür öffnen \n" +
                                "\t 3. Vakuum aktivieren\n" +
                                "\t 4. Tor öffnen\n" +
                                "\t 5. Start-Schiene ausfahren\n" +
                                "\t 6. Shuttle Starten\"");
                    } else if(input.matches("\\s*schaltfl.che\\s*")){
                        printText("auf dem „Looking-Glass“ der Schaltfläche steht:\n" +
                                "\tKonsole: \"Bitte geben sie den Namen des Shuttle's ein das sie anwählen möchten.\"");
                    } else if(input.contains("advent")){
                        cmd_active = true;
                        printText("Konsole: „Shuttle wurde gefunden und ausgewählt.“\n" +
                                "Es stehen dir nun verschiedene Befehle auf der Schaltfläche zur Verfügung:\n" +
                                "\t[Vakuum aktivieren]\n" +
                                "\t[Brücke herunterfahren]\n" +
                                "\t[Start-Schiene ausfahren]\n" +
                                "\t[Tor öffnen]\n" +
                                "\t[Shuttle-Tür öffnen]\n" +
                                "\t[Shuttle Starten]");
                    } else if(input.matches("\\s*br.cke herunterfahren\\s*") && cmd_active){
                        printText("Brücke fährt herunter..");
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        printText("Brücke heruntergefahren.");
                        if(s.nextLine().toLowerCase(Locale.GERMAN).matches("\\s*shuttle-t.r .ffnen\\s*")){
                            printText("Shuttle-Tür geöffnet");
                            if(s.nextLine().toLowerCase(Locale.GERMAN).contains("vakuum aktivieren")){
                                printText("Vakuum aktiviert");
                                if(s.nextLine().toLowerCase(Locale.GERMAN).matches("\\s*tor .ffnen\\s*")){
                                    printText("Tor geöffnet");
                                    if(s.nextLine().toLowerCase(Locale.GERMAN).contains("start-schiene ausfahren")){
                                        printText("Schiene wurde ausgefahren");
                                        if(s.nextLine().toLowerCase(Locale.GERMAN).contains("shuttle starten")){
                                            if(player.isDecision()) {
                                                printText(" Olivia: „Vielen dank! Wenn ich eins tue dann halte ich mich an meine Versprechen.\n" +
                                                        "Hier ist die Karte, auf ihr sind die Koordinaten zur Krankenstation markiert\n" +
                                                        "und hier der code: 2205“\t\n" +
                                                        "hasta luego!“\n" +
                                                        "\n" +
                                                        "Du siehst wie sie zum Schiff läuft, einsteigt und ab fliegt.\n" +
                                                        "Dann gehst du zum Ausgang des Shuttle-Raums um dich auf dem Weg zur Krankenstation zu begeben... ...doch irgendwie hast du ein schlechtes Gefühl ihr geholfen zu haben.");
                                            } else {
                                                printText("Das Tor ist offen, dir kommen fasst die Tränen, als du die Erde durch das Tor siehst.\n" +
                                                        "Alles ist startbereit, worauf wartest du?");
                                                while (!s.nextLine().toLowerCase(Locale.GERMAN).contains("gehe"));
                                                printText("Du bist jetzt im Shuttle und betätigst die richtigen Schalter um die Triebwerke zu zünden.\n" +
                                                        "Der Flug zur Erde verging mühelos, du konntest in der Shuttle-Station in L.A.\n" +
                                                        "landen, die Menschen dort waren ganz verwundert, sie erwarteten keinen Shuttle Flüge von der Talos-1, sie waren informiert, dass sich die Crew dort in Hyperschlaf befindet.\n" +
                                                        "Sie haben auch nicht lange gezögert als du ihnen von dem was auf der Talos-1 passiert ist erzählt hast und haben sofort einen Shuttle zur Weltraumstation gestartet der voll gepackt mit Soldaten und Waffen war.\n" +
                                                        "Es hat nicht lange gedauert bis sie die komplette Station von den Typhons gereinigt haben. Olivia Colomar haben sie auch gefunden und wieder in Gewahrsam gebracht,\n" +
                                                        "diesmal noch sicherer. Und die Crew haben sie auch aus dem Hyperschlaf geweckt um sie über alles was hier geschehen ist und deinen Taten zu informieren.\n" +
                                                        "Und du wurdest noch am gleichen Tag wie ein Held gefeiert bevor ihr wieder den Hyperschlaf betretenen habt.");
                                                Main.game_over = true;
                                            }
                                            chapter_complete = true;
                                        } else {
                                            printText("Falsche Reihenfolge");
                                        }
                                    } else {
                                        printText("Falsche Reihenfolge!");
                                    }
                                } else {
                                    printText("Falsche Reihenfolge!");
                                }
                            } else {
                                printText("Falsche Reihenfolge!");
                            }
                        } else {
                            printText("Falsche Reihenfolge!");
                        }
                    } else {
                        printText("Das geht leider nicht.. Du kannst dich aber umsehen und mehr über einzelne Objekte erfahren.");
                    }
                }

        } else if(chapter == 3){
                printText("Du bist jetzt an der verschlossenen Tür der Krankenstation angekommen. Wie war der Code nochmal?");
                boolean chapter_complete = false;
                boolean code_entered = false;
                while(!chapter_complete) {
                    String input = s.nextLine().toLowerCase(Locale.GERMAN);
                    if(input.contains("2205")){
                        printText("Du gibst den Code ein. Die Tür öffnet sich. Du gehst in die Krankenstation.");
                        code_entered = true;
                    } else if(input.contains("umsehen") && code_entered){
                        printText("Alles ist so weiß und sieht so steril aus, ist endlich mal was anderes, im Gegensatz zu den anderen Räumen, bis jetzt.\n" +
                                "Und da steht er, der Operationstisch, in der Mitte des Raumes. Du weißt was zu tun ist.");
                    } else if(input.contains("tisch") || input.contains("ein")){
                        printText("Du schaltest ihn auf Hyperschlaf, nimmst die richtigen Schlaf-Dauer Einstellungen vor.\n" +
                                "Und aktivierst es, während du dich grade selbst hineinlegst.\n" +
                                "Du schläfst ein, mit den schönen Gedanken wieder in 2205 aufzuwachen und mit deinen Kollegen am neuen Projekt weiter zusammen arbeiten zu können und ihnen von deinem Abenteuer erzählen zu können... doch du bist niemals mehr wieder aufgewacht. \n" +
                                "Dein selbstsüchtiges denken hat dich dazu geführt, nur noch daran zu denken, wieder in den Hyperschlaf zu gehen. Du hast alles dafür gemacht. Du hast sogar eine Meister Hackerin auf\n" +
                                "die Erde losgelassen. Doch was ist mit den Typhon, hast du sie etwa vergessen.\n" +
                                "Sie haben sich über die letzten 100 Jahre, in den du geschlafen hast, so stark vermehrt, dass sie irgendwann die Raumstation buchstäblich aufgefressen haben samt deinen Team Kollegen.\n" +
                                "Du hattest die Chance es zu vermeiden, doch hattest die falschen Prioritäten...");
                        Main.game_over = true;
                    } else if(code_entered) {
                        printText("Das geht leider nicht.. Du kannst dich aber umsehen oder den Operationstisch als Hyperschlaf-Kapsel verwenden.");
                    } else {
                        printText("Das geht leider nicht.. Du musst jetzt den Code eingeben um weiterzukommen.");
                    }
                }
        }
    }
    void secret() {
        printText("Du hast einen geheimen Raum gefunden!");
        printText("Du hast hier 3000$, +1000 an Sauerstoff, eine Waffe und eine Rüstung gefunden.");
        printText("Glückwunsch!");
        player.setMoney(player.getMoney() + 3000);
        player.setOxygen(player.getOxygen() + 1000);
        player.setWeapon(true);
        player.setArmor(true);
    }

    // Sum of all events minus 'NOTHING'
    final int passiveSumMP = 1000;
    // Probability, ID
    final int[][] passiveEvents =
            {
                    { 1,  0 }, // INSTANT LOSE
                    { 40, 1 }, // LUCK
                    { 10, 2 }, // VERY GOOD LUCK
                    { 40, 3 }, // BAD LUCK
                    { 10, 4 }, // VERY BAD LUCK
                    { 40, 5 }, // OXYGEN LEAK
                    { 50, 6 } // MONEY ON THE GROUND
            };
    void passive(){
        printText("Du bist in einem scheinbar leeren Raum.");

        for (Safe safe : Main.safes) {
            if (safe.isGenerated() && safe.getCodeRoomId().equals(roomID)) {
                printText("Möchtest du den Zettel nochmal genauer anschauen?");
                String input = new Scanner(System.in).nextLine().toLowerCase(Locale.GERMAN);
                if (input.contains("ja")) {
                    printText("Auf dem Zettel steht:\n\tSafe-ID: " + safe.getPublicID() + "\n\tCode: " + safe.getCode());
                } else if (input.contains("nein")) {
                    printText("Ok.");
                }
                break;
            } else if(!safe.isGenerated() && !safe.isOpen()) {
                printText("An der Wand befindet sich ein Zettel. Möchtest du diesen genauer anschauen?");
                String input = new Scanner(System.in).nextLine().toLowerCase(Locale.GERMAN);
                if (input.contains("ja")) {
                    printText("Auf dem Zettel steht:\n\tSafe-ID: " + safe.getPublicID() + "\n\tCode: " + safe.getCode());
                } else if (input.contains("nein")) {
                    printText("Ok.");
                }
                safe.setGenerated(true);
                safe.setCodeRoomId(roomID);
                break;
            }
        }

        int event = -1;
        int chance = ThreadLocalRandom.current().nextInt(0, passiveSumMP);
        int probability = 0;
        for (int[] passiveEvent : passiveEvents)
        {
            if (chance <= (probability += passiveEvent[0]))
            {
                event = passiveEvent[1];
                break;
            }
        }

        switch (event)
        {
            case 0: // INSTANT LOSE
                printText("Du bist ganz erschöpft und ruhst dich aus.");
                printText("Als du Eingeschlafen bist kamen Typhonen und haben dich angegriffen");
                printText("Du hattest keine Chance mehr dich zu wehren.");
                printText("GAME OVER");

                Main.game_over = true;

                break;
            case 1: // LUCK
                printText("Dich erfüllt ein gutes gefühl.");
                printText("Dein Glück wurde erhöht");

                player.setLuck(player.getLuck() + 10);

                break;
            case 2: // VERY GOOD LUCK
                printText("Es ist als ob heute dein Glückstag wäre.");
                printText("Dein Glück wurde stark erhöht");

                player.setLuck(player.getLuck() * 10);

                break;
            case 3: // BAD LUCK
                printText("Du fühlst dich nicht gut.");
                printText("Dein Glück wurde gesenkt.");

                player.setLuck(player.getLuck() - 10);
                if (player.getLuck() < 0 )
                {
                    player.setLuck(0);
                }

                break;
            case 4: // VERY BAD LUCK
                printText("Es hängt ein umgedrehtes Kreuz an der Wand!!!");
                printText("Dein Glück ist so gut wie verschwunden");

                player.setLuck(0);

                break;
            case 5: // OXYGEN LEAK
                printText("Du hörst ein zischen, dein Sauerstofftank hat ein Leak!");
                printText("Du schaffst es zu schliesen, aber es ist warscheinlich einiges entwischen.");

                int ox = player.getOxygen();

                ox *= 2.5;
                ox /= 3;

                player.setOxygen(ox);

                break;
            case 6: // MONEY ON THE GROUND
                printText("Am boden liegt Geld, willst du es aufheben?");

                if (new Scanner(System.in).nextLine().toLowerCase(Locale.GERMAN).contains("ja"))
                {
                    int money = ThreadLocalRandom.current().nextInt(10, 100);

                    printText("Du hebst das Geld auf.");
                    Main.playSound("sounds/coins.wav");
                    printText("Es sind " + money + " münzen!");
                    printText("Du hast in der Zeit jedoch einiges an Sauerstoff verloren.");

                    player.setMoney( player.getMoney() + money);
                    player.setOxygen( player.getOxygen() + 10 );

                    if (player.getOxygen() < 0)
                    {
                        player.setOxygen(0);
                    }
                }
                else {
                    printText("Du entscheidest dich es zu ignorieren und gehst weiter.");
                }

                break;
        }
    }

    void key(){
        printText("Du hast hier einen Master-Key gefunden. Damit kannst du jede verschlossene Tür öffnen!");
        player.setKeys(player.getKeys() + 1);
    }

    public static void cls(boolean enter){
        if(enter){
            try {
                printText("Drücke ENTER um fortzufahren..", 30, false);
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 50; ++i) System.out.println();
    }
    public static void printText(String s){
        printText(s, 30, true);
    }

    public static void printText(String s, int t, boolean newLine){
        for(int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            System.out.print(c);
            if(c != ' ') {
                try {
                    Thread.sleep(t);
                } catch (InterruptedException ignored) {}
            }
        }
        if(newLine) System.out.println();
    }
}
