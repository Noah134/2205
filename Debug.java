public class Debug
{
    public final static String DEBUG_PREFIX = "\\$dc_";

    public static String resolve_debug(String command, Player player, Room[][] rooms)
    {
        switch(command)
        {
            case "get_position":
                return "X" + player.getPos_x() +
                       ":Y" + player.getPos_y();
            case "get_health":
                return Integer.toString(player.getHealth());
            case "get_oxygen":
                return Integer.toString(player.getOxygen());
            case "get_luck":
                return Integer.toString(player.getLuck());
            case "get_story":
                return Integer.toString(player.getStory_chapter());
            case "get_secrets":
            {
                StringBuilder sb = new StringBuilder();

                for (int x = 0; x < rooms.length; ++x)
                    for (int y = 0; y < rooms[x].length; ++y)
                        if (rooms[x][y].getType() == 7)
                        {
                            sb.append("\n" + "X" + x + "Y" + y);
                        }

                return sb.toString();
            }
            case "get_all":
            {
                StringBuilder sb = new StringBuilder();
                sb.append("\n(Y:X/T-X/T ...)");

                for (int y = 0; y < rooms[0].length; ++y)
                {
                    sb.append('\n');
                    sb.append(y + ":");

                    for (int x = 0; x < rooms.length; ++x)
                    {
                        sb.append(x + "/" + rooms[x][y].getType() + "-");
                    }
                }

                return sb.toString();
            }
            case "set_max_oxygen":
                player.setOxygen(Integer.MAX_VALUE);
                return null;
            case "toggle_execute":
                Event e = rooms[player.getPos_x()]
                     [player.getPos_y()].getEvent();
                e.forceExecute(!e.getExecuted());

                return Boolean.toString(e.getExecuted());
            case "skipmission":
                player.setStory_chapter(1 + player.getStory_chapter());

                return Integer.toString(player.getStory_chapter());
            default:
                return "UNKOWN_COMMAND";
        }
    }
}
