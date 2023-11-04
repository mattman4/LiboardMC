package org.limc.liboardmc;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.Enums;
import chariot.model.Event;
import chariot.model.GameStateEvent;
import com.mrivanplays.conversations.base.Conversation;
import com.mrivanplays.conversations.base.question.Question;
import com.mrivanplays.conversations.spigot.SpigotConversationManager;
import com.mrivanplays.conversations.spigot.SpigotConversationPartner;
import io.github.rapha149.signgui.SignGUI;
import io.github.rapha149.signgui.SignGUIAction;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ChessBot {

    // make it so when you execute command, it asks for your first move
    // then, it will constantly loop
    // if new value found => ask user for value => check if hasEnteredMove is true to send it => then set to false

    private LiboardMC plugin;
    private ClientAuth client;
    private StringContainer lastMove = new StringContainer();

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

}
