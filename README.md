# LiboardMC
Connects to a Lichess game in Minecraft by sending a challenge request to a hardcoded opponent. The board is reflected via mobs on a Minecraft recreation of a chess board with pieces being different mobs. Each turn, a sign GUI comes up asking you to input a move which updates the Lichess board as well as the Minecraft board.

## Setup
1. Create a 1.20.2 Spigot Minecraft server (see [here](https://www.spigotmc.org/wiki/spigot-installation/))
2. Import [this world](https://drive.google.com/file/d/1Vfa1tuHvKqDc3urVETr_m1K7Ets5tYxm/view?usp=drive_link) or create your own world where, at coordinates (0.5, -60, -0.5), you build an 8x8 chess board where that coordinate is the bottom left corner of the board.
3. Create a lichess bot account - [instructions here](https://lichess.org/api#tag/Bot/operation/apiBotOnline) (creating API token + running `curl` command to upgrade to bot account) **IMPORTANT: note down the API token**
4. Clone repository
5. In `LiboardMC.java`, at line `11`, replace with your API access token created in step 3
6. In `StartCommand.java`, at line `22`, replace the preset username `(mattman23)` with the username to challenge - the person you will be playing against from Minecraft
7. In `pom.xml`, at line `43`, replace the outputFile path with the path of your `plugins` folder with the server created in step 1
8. Run maven lifecycle `package`, which will generate .jar plugin file in your `plugins` folder
9. Run the server, connect, and do `/start` in game to create the challenge request
10. When accepted, you will automatically play the opening `1. e4` simply because I couldn't be bothered to be able to change the first move you do. Then you will have to wait for the person you're playing against to make a move.
11. Once they have made a move, a sign GUI will come up in game, and you have to enter your move. **IMPORTANT: the notation for this is different to normal algebraic chess notation! You put the source square, then destination square e.g. `e2e4`, `a1a3`, so DO NOT put the name of the piece. As long as it is valid, the relevant piece will be moved. If you put an invalid move, because there is no error handling you will not be given another opportunity to make a move and you will have to restart the game.**
12. Continue playing until mate! (there is no handling for resignations, draws, etc. Didn't get a chance in the time frame!). You will get a message in game if you get mated but it will not update the move as we forgot to check for this. Oops.
