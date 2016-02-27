package milk.revivalblock;

import cn.nukkit.block.Block;
import cn.nukkit.event.EventHandler;
import cn.nukkit.item.ItemBlock;
import cn.nukkit.level.Level;
import cn.nukkit.Player;
import cn.nukkit.item.Item;
import cn.nukkit.level.Position;
import cn.nukkit.math.Vector3;
import cn.nukkit.event.Listener;
import cn.nukkit.command.Command;
import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import cn.nukkit.plugin.PluginBase;
import cn.nukkit.command.CommandSender;
import cn.nukkit.event.block.BlockBreakEvent;
import cn.nukkit.event.player.PlayerInteractEvent;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RevivalBlock extends PluginBase implements Listener{

    public HashMap<String, Position> pos = new HashMap<>();

    public LinkedHashMap<String, Object> rand;
    public LinkedHashMap<String, Object> revi;

    public void onEnable(){
        this.saveDefaultConfig();
        this.saveResource("data.yml", false);

        this.rand = (LinkedHashMap<String, Object>) new Config(new File(this.getDataFolder(), "data.yml"), Config.YAML).getAll();
        this.revi = (LinkedHashMap<String, Object>) new Config(new File(this.getDataFolder(), "revi.dat"), Config.YAML).getAll();

        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getLogger().info(TextFormat.GOLD + "[RevivalBlock]Plugin has been enabled");
    }

    public void onDisable(){
        Config data = new Config(this.getDataFolder() + "data.yml", Config.YAML);
        data.setAll(this.rand);
        data.save();

        Config revi = new Config(this.getDataFolder() + "revi.dat", Config.YAML);
        revi.setAll(this.revi);
        revi.save();
        this.getServer().getLogger().info(TextFormat.GOLD + "[RevivalBlock]Plugin has been disabled");
    }

    public int getRevivalBlock(Position pos){
        pos = pos.floor();
        Object data = this.revi.get(pos.x + ":" + pos.y + ":" + pos.z + ":" + pos.level.getFolderName());
        if(data == null || !(data instanceof Integer)){
            return -2;
        }
        return (Integer) data;
    }

    public boolean isTool(Item item){
        return item.getId() == this.getConfig().get("tool-id", Item.STICK);
    }

    @EventHandler
    public void PlayerTouchBlock(PlayerInteractEvent ev){
        Block block = ev.getBlock();
        Player player = ev.getPlayer();
        if(this.isTool(ev.getItem()) && player.isOp()){
            if(ev.getAction() == PlayerInteractEvent.RIGHT_CLICK_BLOCK && ev.getFace() != 255) {
                this.pos[p.getName()]["pos2"] = [block.x, block.y, block.z];
                player.sendMessage("[RevivalBlock]Pos2지점을 선택했습니다({t.x}, {t.y}, {t.z})");
            }elseif(ev.getAction() == PlayerInteractEvent.LEFT_CLICK_BLOCK){
                this.pos[p.getName()]["pos1"] = [block.x, block.y, block.z];
                player.sendMessage("[RevivalBlock]Pos1지점을 선택했습니다({t.x}, {t.y}, {t.z})");
            }
            ev.setCancelled();
        }
    }

    @EventHandler
    public void PlayerBreakBlock(BlockBreakEvent ev){
        i = ev.getItem();
        t = ev.getBlock();
        p = ev.getPlayer();
        x = t.x;
        y = t.y;
        z = t.z;
        if(this.isTool(i) && p.isOp()){
            this.pos[p.getName()]["pos1"] = [x, y, z];
            p.sendMessage("[RevivalBlock]Pos1지점을 선택했습니다(x, y, z)");
            ev.setCancelled();
        }elseif((value = self.getRevivalBlock(t)) != false){
            if(value == true){
                as = explode("/", this.rand["normal"]);
                if(mt_rand(1, as[1]) > as[0]){
                    ev.setCancelled();
                    return;
                }
                foreach(t.getDrops(i) as d){
                    p.getInventory().addItem(Item.get(...d));
                }
            }else{
                block = Item.fromString(value);
                if(t.getId() == block.getId() and t.getDamage() == block.getDamage()){
                    item = Item.get(Item.AIR);
                    foreach(this.rand[block.getId()] as string => as){
                        as = explode("/", as);
                        if(mt_rand(1, as[1]) <= as[0]){
                            if(Item.fromString(string) instanceof ItemBlock){
                                item = Item.fromString(string);
                            }else{
                                unset(this.rand[block.getId()][string]);
                            }
                        }
                    }
                    if(item.getId() > 0){
                        p.getLevel().setBlock(new Vector3(x,y,z), item.getBlock(), true);
                    }else{
                        foreach(block.getBlock().getDrops(i) as drops){
                            p.getInventory().addItem(Item.get(...drops));
                        }
                    }
                }else{
                    foreach (t.getDrops(i) as drops){
                        p.getInventory().addItem(Item.get(...drops));
                    }
                    p.getLevel().setBlock(t, block.getBlock(), true);
                }
            }
            slot = p.getInventory().getItemInHand();
            if(slot.isTool() && !p.isCreative()){
                if(slot.useOn(t) and slot.getDamage() >= slot.getMaxDurability()) slot.count--;
                p.getInventory().setItemInHand(slot);
            }
            ev.setCancelled();
        }
    }

    public void makeBlock(startX, startY, startZ, endX, endY, endZ, isChange, Level level){
        for(x = startX; x <= endX; x++){
            for(y = startY; y <= endY; y++){
                for(z = startZ; z <= endZ; z++){
                    if(isChange && isset(this.rand[id = level.getBlock(new Vector3(x, y, z)).getId()])){
                        this.revi["x:y:z"] = id;
                    }else{
                        this.revi["x:y:z"] = true;
                    }
                }
            }
        }
    }

    public void destroyBlock(startX, startY, startZ, endX, endY, endZ){
        for(x = startX; x <= endX; x++){
            for(y = startY; y <= endY; y++){
                for(z = startZ; z <= endZ; z++){
                    unset(this.revi["x:y:z"]);
                }
            }
        }
    }

    public void onCommand(CommandSender i, Command cmd, String label, String[] sub){
        if(!(i instanceof Player)){
            return true;
        }

        String pu = i.getName();
        String output = "[RevivalBlock]";
        if(!isset(this.pos[pu]["pos1"]) or !isset(this.pos[pu]["pos2"])){
            output += "Please tap a block to make to revival block";
            i.sendMessage(output);
            return true;
        }

        sx = min(this.pos[pu]["pos1"][0], this.pos[pu]["pos2"][0]);
        sy = min(this.pos[pu]["pos1"][1], this.pos[pu]["pos2"][1]);
        sz = min(this.pos[pu]["pos1"][2], this.pos[pu]["pos2"][2]);
        ex = max(this.pos[pu]["pos1"][0], this.pos[pu]["pos2"][0]);
        ey = max(this.pos[pu]["pos1"][1], this.pos[pu]["pos2"][1]);
        ez = max(this.pos[pu]["pos1"][2], this.pos[pu]["pos2"][2]);
        if(cmd.getName() == "revi"){
            this.makeBlock(sx, sy, sz, ex, ey, ez, isset(sub[0]), i.getLevel());
        }else{
            this.destroyBlock(sx, sy, sz, ex, ey, ez);
        }
        output += cmd.getName() == "revi" ? "The chosen block was made to revival block" : "The chosen block is no more revival block";
        i.sendMessage(output);
        return true;
    }
}
