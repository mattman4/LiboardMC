package org.limc.liboardmc;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameStateEvent;
import io.github.rapha149.signgui.SignGUI;
import io.github.rapha149.signgui.SignGUIAction;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class ChessBot {

    // make it so when you execute command, it asks for your first move
    // then, it will constantly loop
    // if new value found => ask user for value => check if hasEnteredMove is true to send it => then set to false

    private LiboardMC plugin;
    private ClientAuth client;
    private StringContainer lastMove = new StringContainer();
    private ArrayList<UUID> mobs = new ArrayList<>();
    public ChessBot(LiboardMC plugin, String token) {
        this.plugin = plugin;
        client = Client.auth(token);
    }

    private void openSign(Player player, ClientAuth client, String id) {
        SignGUI sign = SignGUI.builder()
                .setLines(null, "^^^^^^^^", "enter your", "chess move")
                .setType(Material.CHERRY_SIGN)
                .setColor(DyeColor.BLACK)
                .setHandler((p, result) -> {
                    String input = result.getLineWithoutColor(0);
                    if(input.isEmpty()) {
                        return List.of(SignGUIAction.displayNewLines(null, "^^^^^^^^", "enter your", "chess move"));
                    }
                    lastMove.setValue(input);
                    client.bot().move(id, lastMove.getValue());
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            spawnMobs(player.getWorld(), client.games().ongoing(1).stream().toList().get(0).fen());
                        }
                    }.runTask(plugin);
                    return Collections.emptyList();
                })
                .build();
        sign.open(player);
    }


    public void challenge(Player player, String name) {
        new BukkitRunnable() {
            @Override
            public void run() {
                client.challenges().challenge(name, params -> params.clockBlitz5m0s().color(Enums.ColorPref.white));

                Stream<Event> events = client.bot().connect().stream();
                events.forEach(event -> {
                    switch(event.type())
                    {
                        case gameStart:
                            player.sendMessage("Game started");
                            Stream<GameStateEvent> gameEvents = client.bot().connectToGame(event.id()).stream();
                            gameEvents.forEach(gameEvent -> {
                                switch(gameEvent.type()) {
                                    case gameFull: // this activates only ONCE on game start
                                        lastMove.setValue("e2e4");
                                        client.bot().move(event.id(), "e2e4");
                                        new BukkitRunnable() {
                                            @Override
                                            public void run() {
                                                spawnMobs(player.getWorld(), client.games().ongoing(1).stream().toList().get(0).fen());
                                            }
                                        }.runTask(plugin);
                                        break;
                                    case gameState:
                                        Enums.Status status = ((GameStateEvent.State)gameEvent).status();
                                        List<String> moves = ((GameStateEvent.State) gameEvent).moveList();
                                        String lastMoveDetected = moves.get(moves.size()-1);

                                        if(status == Enums.Status.mate) {
                                            player.sendMessage("mate");
                                        } else {
                                            if(lastMoveDetected.equals(lastMove.getValue())) break;
                                            player.sendMessage("The opponent played: " + lastMoveDetected);

                                            String fen = client.games().ongoing(1).stream().toList().get(0).fen();

                                            new BukkitRunnable() {
                                                @Override
                                                public void run() {
                                                    spawnMobs(player.getWorld(), fen);
                                                }
                                            }.runTask(plugin);

                                            openSign(player, client, event.id());
                                        }
                                }
                            });
                            break;
                    }
                });
            }
        }.runTaskAsynchronously(plugin);

    }

    private Entity getEntityByUUID(World world, UUID id) {
        for (Entity entity : world.getEntities()) {
            if (entity.getUniqueId().equals(id)) return entity;
        }
        //Bukkit.broadcastMessage("Couldn't find entity");
        return null;
    }

    private EntityType getEntityFromChar(char chr) {
        switch(chr) {
            case 'P':
                return EntityType.VILLAGER;
            case 'p':
                return EntityType.PILLAGER;
            case 'R':
                return EntityType.IRON_GOLEM;
            case 'r':
                return EntityType.WARDEN;
            case 'N':
                return EntityType.HORSE;
            case 'n':
                return EntityType.SKELETON_HORSE;
            case 'B':
                return EntityType.SKELETON;
            case 'b':
                return EntityType.WITHER_SKELETON;
            case 'Q':
                return EntityType.ALLAY;
            case 'q':
                return EntityType.PHANTOM;
            case 'K':
                return EntityType.SQUID;
            case 'k':
                return EntityType.GLOW_SQUID;
            default:
                return null;
        }
    }

    private void spawnMobs(World world, String fen) {
        String[] separatedFen = fen.split(" ");
        String positions = new StringBuilder(separatedFen[0]).reverse().toString();
        String[] ranks = positions.split("/");

        for(UUID id : mobs) {
            for (Entity entity : world.getEntities()) {
                if (entity.getUniqueId().equals(id)) entity.remove();
            }
        }
        mobs.clear();


        Location loc = new Location(world, 0.5, -60, -0.5);

        for(String rank : ranks) {
            rank = new StringBuilder(rank).reverse().toString();
            for(int i = 0; i<rank.length(); i++) {
                char chr = rank.charAt(i);
                EntityType type = getEntityFromChar(chr);

                if(type == null) {
                    if(Character.isDigit(chr)) {
                        loc = loc.add(new Vector(0, 0, Character.getNumericValue(chr)));
                        continue;
                    } else {
                        loc = loc.add(new Vector(0, 0, 1));
                        continue;
                    }
                }

                //Bukkit.broadcastMessage(loc.getX() + "/" + loc.getY() + "/" + loc.getZ());
                //Bukkit.broadcastMessage(storedLoc.getValue().getX() + "/" + storedLoc.getValue().getY() + "/" + storedLoc.getValue().getZ());

                Entity entity = world.spawnEntity(loc, type);
                ((LivingEntity)entity).setAI(false);
                entity.setInvulnerable(true);
                //Bukkit.broadcastMessage("spawned " + entity.getType().toString() + " at " + loc.getX() + "/" + loc.getY() + "/" + loc.getZ());
                mobs.add(entity.getUniqueId());


                loc = loc.add(new Vector(0, 0, 1));
            }
            loc = loc.add(new Vector(1.0, 0, 0));
            loc.setZ(-0.5);
        }

    }

}
