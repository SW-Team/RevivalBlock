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
import milk.revivalblock.util.Utils;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class RevivalBlock extends PluginBase implements Listener{

    public HashMap<String, Position[]> pos = new HashMap<>();

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
        Config data = new Config(new File(this.getDataFolder(), "data.yml"), Config.YAML);
        data.setAll(this.rand);
        data.save();

        Config revi = new Config(new File(this.getDataFolder(), "revi.dat"), Config.YAML);
        revi.setAll(this.revi);
        revi.save();
        this.getServer().getLogger().info(TextFormat.GOLD + "[RevivalBlock]Plugin has been disabled");
    }

    public int getRevivalBlock(Position pos){
        pos = pos.floor();
        Object data = this.revi.get(((int) pos.x) + ":" + ((int) pos.y) + ":" + ((int) pos.z) + ":" + pos.level.getFolderName());
        if(data == null || !(data instanceof Integer)){
            return -2;
        }
        return (Integer) data;
    }

    public boolean isRevivalBlock(Position pos){
        return this.getRevivalBlock(pos) > -2;
    }

    public boolean isTool(Item item){
        return item.getId() == this.getConfig().get("tool-id", Item.STICK);
    }

    @EventHandler
    public void PlayerTouchBlock(PlayerInteractEvent ev){
        Block block = ev.getBlock();
        Player player = ev.getPlayer();
        if(this.isTool(ev.getItem()) && player.hasPermission("revival.command.revi")){
            Position[] pos;
            if(this.pos.containsKey(player.getName())){
                pos = this.pos.get(player.getName());
            }else{
                pos = new Position[2];
                this.pos.put(player.getName(), pos);
            }

            if((ev.getAction() == PlayerInteractEvent.RIGHT_CLICK_BLOCK && ev.getFace() != 255) || ev.getAction() == PlayerInteractEvent.LEFT_CLICK_BLOCK){
                boolean kkk = ev.getAction() == PlayerInteractEvent.LEFT_CLICK_BLOCK;
                pos[kkk ? 0 : 1] = block.floor();
                player.sendMessage("[RevivalBlock]" +(kkk ? "Pos1" : "Pos2") + "지점을 선택했습니다(" + ((int) block.x) + ", " + ((int) block.y) + ", " + ((int) block.z) + ", " + block.level.getFolderName() + ")");
            }
            ev.setCancelled();
        }
    }


    @EventHandler
    public void PlayerBreakBlock(BlockBreakEvent ev){
        Item item = ev.getItem();
        Block block = ev.getBlock();
        Player player = ev.getPlayer();
        
        int value;
        if(this.isTool(item) && player.hasPermission("revival.command.revi")){
            Position[] pos;
            if(this.pos.containsKey(player.getName())){
                pos = this.pos.get(player.getName());
            }else{
                pos = new Position[2];
                this.pos.put(player.getName(), pos);
            }
            pos[0] = block;
            player.sendMessage("[RevivalBlock]Pos1지점을 선택했습니다(" + block.x + ", " + block.y + ", " + block.z + ", " + block.level.getFolderName() + ")");
            ev.setCancelled();
        }else if((value = this.getRevivalBlock(block)) > -2){
            if(value == -1){
                String[] as = this.rand.get("normal").toString().split("/");
                if(Utils.rand(1, Integer.parseInt(as[1])) > Integer.parseInt(as[0])){
                    ev.setCancelled();
                    return;
                }
                for(int[] d : block.getDrops(item)){
                    player.getInventory().addItem(Item.get(d[0], d[1], d[2]));
                }
            }else{
                Block block1 = Block.get(value);
                if(block.getId() == block1.getId() && block.getDamage() == block1.getDamage()){
                    Object list = this.rand.get(block1.getId() + "");
                    if(!(list instanceof HashMap)){
                        return;
                    }

                    Item[] item1 = {Item.get(Item.AIR)};
                    for(Object string : ((HashMap) list).keySet()){
                        try{
                            String[] rand = ((HashMap) list).get(string).toString().split("/");
                            if(Utils.rand(1, Integer.parseInt(rand[1])) <= Integer.parseInt(rand[0])){
                                Item item2 = Item.fromString((String) string);
                                if(item2 instanceof ItemBlock){
                                    item1[0] = item2;
                                }else{
                                    ((HashMap) list).remove(string);
                                }
                            }
                        }catch(Exception ignore){}
                    }

                    if(item1[0].getId() > 0){
                        player.getLevel().setBlock(new Vector3(block.x, block.y, block.z), item1[0].getBlock(), true);
                    }else{
                        for(int[] d : block1.getDrops(item)){
                            player.getInventory().addItem(Item.get(d[0], d[1], d[2]));
                        }
                    }
                }else{
                    for(int[] d : block.getDrops(item)){
                        player.getInventory().addItem(Item.get(d[0], d[1], d[2]));
                    }
                    player.getLevel().setBlock(block, block1, true);
                }
            }
            Item slot = player.getInventory().getItemInHand();
            if(slot.isTool() && !player.isCreative()){
                if(slot.useOn(block) && slot.getDamage() >= slot.getMaxDurability()){
                    slot.count--;
                }
                player.getInventory().setItemInHand(slot);
            }
            ev.setCancelled();
        }
    }

    public void makeBlock(int startX, int startY, int startZ, int endX, int endY, int endZ, boolean isChange, Level level){
        for(int x = startX; x <= endX; x++){
            for(int y = startY; y <= endY; y++){
                for(int z = startZ; z <= endZ; z++){
                    int id = level.getBlock(new Vector3(x, y, z)).getId();
                    if(isChange && this.rand.containsKey((id) + "")){
                        this.revi.put(x + ":" + y + ":" + z + ":" + level.getFolderName(), id);
                    }else{
                        this.revi.put(x + ":" + y + ":" + z + ":" + level.getFolderName(), -1);
                    }
                }
            }
        }
    }

    public void destroyBlock(int startX, int startY, int startZ, int endX, int endY, int endZ, Level level){
        for(int x = startX; x <= endX; x++){
            for(int y = startY; y <= endY; y++){
                for(int z = startZ; z <= endZ; z++){
                    this.revi.remove(x + ":" + y + ":" + z + ":" + level.getFolderName());
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender i, Command cmd, String label, String[] sub){
        if(!(i instanceof Player)){
            i.sendMessage("[RevivalBlock]게임 안에서 사용해주세요");
            //i.sendMessage("[RevivalBlock]Please use this command in game");
            return true;
        }

        Player player = (Player) i;
        Position[] pos = this.pos.get(player.getName());
        if(pos == null || pos[0] == null || pos[1] == null || pos[1].getLevel() != pos[0].getLevel()){
            player.sendMessage("[RevivalBlock]소생블럭이 설정되지 않았거나 지역이 서로 달라요");
            //player.sendMessage("[RevivalBlock]Please tap a block to make to revival block");
            return true;
        }

        int sx = (int) Math.min(pos[0].x, pos[1].x);
        int sy = (int) Math.min(pos[0].y, pos[1].y);
        int sz = (int) Math.min(pos[0].z, pos[1].z);
        int ex = (int) Math.max(pos[0].x, pos[1].x);
        int ey = (int) Math.max(pos[0].y, pos[1].y);
        int ez = (int) Math.max(pos[0].z, pos[1].z);

        if(cmd.getName().equals("revi")){
            this.makeBlock(sx, sy, sz, ex, ey, ez, sub.length > 0, pos[0].getLevel());
        }else{
            this.destroyBlock(sx, sy, sz, ex, ey, ez, pos[0].getLevel());
        }

        player.sendMessage("[RevivalBlock]" + (cmd.getName().equals("revi") ? "선택한 블럭은 소생블럭이 되었어요" : "선택한 블럭은 이제 더이상 소생블럭이 아니에요"));//"The chosen block was made to revival block" : "The chosen block is no more revival block"));
        return true;
    }
}
