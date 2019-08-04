package MultiWorld;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.command.Command;
import cn.nukkit.command.CommandSender;
import cn.nukkit.level.Level;
import cn.nukkit.level.generator.Generator;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.scheduler.AsyncTask;
import cn.nukkit.utils.MainLogger;
import cn.nukkit.utils.TextFormat;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class MultiWorld extends PluginBase {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("mw.command")) {
            sender.sendMessage(cmd.getPermissionMessage());
            return true;
        }

        if (args.length <= 0 || args[0].toLowerCase().equals("help")) {
            if(!sender.hasPermission("mw.command.help")){
                sender.sendMessage(cmd.getPermissionMessage());
                return true;
            }

            sender.sendMessage(getHelp(sender));
            return true;
        }

        Level level = null;
        Player p = null;

        if (sender instanceof Player) {
            p = (Player) sender;
            level = p.getLevel();
        }

        switch (args[0].toLowerCase()) {
            case "load":
            case "l":
                if(!sender.hasPermission("mw.command.unload")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if (args.length != 2) {
                    sender.sendMessage(TextFormat.GRAY + "use: " + TextFormat.YELLOW + "/mw load <world name>");
                    return true;
                }

                if (!getServer().loadLevel(args[1])) {
                    sender.sendMessage(TextFormat.RED + "level " + args[1] + " doesn't exist");
                    return true;
                }

                sender.sendMessage(TextFormat.GREEN + "Level has been successfully loaded");
                break;
            case "unload":
            case "ul":
                if(!sender.hasPermission("mw.command.unload")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if (args.length != 2) {
                    sender.sendMessage(TextFormat.GRAY + "use: " + TextFormat.YELLOW + "/mw unload <world name>");
                    return true;
                }

                Level lvl = getServer().getLevelByName(args[1]);

                if (lvl == null) {
                    sender.sendMessage(TextFormat.RED + "level " + args[1] + " doesn't exist");
                    return true;
                }

                getServer().unloadLevel(lvl);
                sender.sendMessage(TextFormat.GREEN + "Level has been successfully unloaded");
                break;
            case "create":
            case "c":
                if(!sender.hasPermission("mw.command.create")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if (args.length < 2 || args.length > 4) {
                    sender.sendMessage(TextFormat.GRAY + "use: " + TextFormat.YELLOW + "/mw create <world name> [generator] [seed]");
                    return true;
                }

                long seed = args.length >= 4 ? Long.valueOf(args[3]) : new Random().nextLong();
                Class<? extends Generator> generator = args.length >= 3 ? Generator.getGenerator(args[2]) : null;

                if (!getServer().generateLevel(args[1], seed, generator)) {
                    sender.sendMessage(TextFormat.RED + "level " + args[1] + " already exists");
                    return true;
                }

                sender.sendMessage(TextFormat.GREEN + "Level has been successfully generated");
                break;
            case "delete":
            case "d":
                if(!sender.hasPermission("mw.command.delete")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if (args.length != 2) {
                    sender.sendMessage(TextFormat.GRAY + "use: " + TextFormat.YELLOW + "/mw delete <world name>");
                    return true;
                }

                Level level1 = getServer().getLevelByName(args[0]);
                File path = new File(getServer().getDataPath() + "worlds/" + args[0]);

                if (level1 != null) {
                    getServer().unloadLevel(level1);
                }

                getServer().getScheduler().scheduleAsyncTask(this, new AsyncTask() {
                    boolean error = false;

                    @Override
                    public void onRun() {
                        try {
                            FileUtils.deleteDirectory(path);
                        } catch (IOException e) {
                            MainLogger.getLogger().logException(e);
                            error = true;
                        }
                    }

                    @Override
                    public void onCompletion(Server server){
                        if(error){
                            sender.sendMessage(TextFormat.RED + "Error happened during level deleting");
                        } else {
                            sender.sendMessage(TextFormat.GREEN + "Level has been successfully deleted");
                        }
                    }
                });
                break;
            case "teleport":
            case "tp":
            case "goto":
                if(!sender.hasPermission("mw.command.teleport")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if ((p == null && args.length != 3) || args.length < 2 || args.length > 3) {
                    sender.sendMessage(TextFormat.GRAY + "use: " + TextFormat.YELLOW + "/mw teleport <world name> [player]");
                    return true;
                }

                Level level2 = getServer().getLevelByName(args[1]);

                if (level2 == null) {
                    if (!getServer().loadLevel(args[1])) {
                        sender.sendMessage(TextFormat.RED + "level " + args[1] + " doesn't exist");
                        return true;
                    }

                    level2 = getServer().getLevelByName(args[1]);

                    if (level2 == null) {
                        sender.sendMessage(TextFormat.RED + "level " + args[1] + " doesn't exist");
                        return true;
                    }
                }

                Player target = p;
                String msg = TextFormat.GRAY + "Teleporting to level " + level2.getFolderName()+"...";

                if (args.length == 3) {
                    target = getServer().getPlayer(args[2]);

                    if (target == null) {
                        sender.sendMessage(TextFormat.RED + "Player " + args[2] + " doesn't exist");
                        return true;
                    }

                    msg = TextFormat.GRAY + "Teleporting " + TextFormat.YELLOW + target.getName() + TextFormat.GRAY + " to level " + TextFormat.GREEN + level2.getFolderName() + "...";
                }

                target.teleport(level2.getSafeSpawn());
                sender.sendMessage(msg);
                break;
            case "list":
                if(!sender.hasPermission("mw.command.list")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                StringBuilder message = new StringBuilder(TextFormat.GRAY + "All loaded levels:\n");

                for(Level level3 : getServer().getLevels().values()){
                    message.append(TextFormat.GREEN + "- " + TextFormat.GRAY).append(level3.getFolderName()).append("\n");
                }

                sender.sendMessage(message.toString());
                break;
            case "spawn":
                if(!sender.hasPermission("mw.command.spawn")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if(p == null){
                    sender.sendMessage(TextFormat.RED+"You can use this command in game only");
                    break;
                }

                p.sendMessage(TextFormat.GRAY + "Teleporting to spawn...");
                p.teleport(level.getSafeSpawn());
                break;
            case "setspawn":
                if(!sender.hasPermission("mw.command.setspawn")){
                    sender.sendMessage(cmd.getPermissionMessage());
                    break;
                }

                if(p == null){
                    sender.sendMessage(TextFormat.RED+"You can use this command in game only");
                    break;
                }

                p.sendMessage(TextFormat.GRAY+"spawn set");
                level.setSpawnLocation(p.clone());
                break;
        }

        return true;
    }

    private String getHelp(CommandSender sender) {
        String msg = TextFormat.BLUE+"Printing all MultiWorld commands: \n";

        if(sender.hasPermission("mw.command.load")) msg += TextFormat.YELLOW+"/mw load "+TextFormat.GREEN+"- Loads an existing world\n";
        if(sender.hasPermission("mw.command.unload")) msg += TextFormat.YELLOW+"/mw unload "+TextFormat.GREEN+"- Unloads an existing world\n";
        if(sender.hasPermission("mw.command.create")) msg += TextFormat.YELLOW+"/mw create "+TextFormat.GREEN+"- Create a new world\n";
        if(sender.hasPermission("mw.command.delete")) msg += TextFormat.YELLOW+"/mw delete "+TextFormat.GREEN+"- Delete an existing world\n";
        if(sender.hasPermission("mw.command.list")) msg += TextFormat.YELLOW+"/mw list "+TextFormat.GREEN+"- Shows all loaded worlds\n";
        if(sender.hasPermission("mw.command.teleport")) msg += TextFormat.YELLOW+"/mw teleport "+TextFormat.GREEN+"- Teleport to a world\n";
        if(sender.hasPermission("mw.command.spawn")) msg += TextFormat.YELLOW+"/mw spawn "+TextFormat.GREEN+"- Teleport to world's spawn\n";
        if(sender.hasPermission("mw.command.setspawn")) msg += TextFormat.YELLOW+"/mw setspawn "+TextFormat.GREEN+"- Sets world's spawn\n";


        return msg;
    }

    public static String getPrefix(){
        return "";
    }
}
