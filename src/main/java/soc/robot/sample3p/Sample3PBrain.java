/*
 * Java Settlers - An online multiplayer version of the game Settlers of Catan
 * This file Copyright (C) 2017-2021 Jeremy D Monin <jeremy@nand.net>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The maintainer of this program can be reached at jsettlers@nand.net
 */
package soc.robot.sample3p;

import soc.debug.D;
import soc.game.SOCCity;
import soc.game.SOCGame;
import soc.game.SOCGameOptionSet;
import soc.game.SOCResourceConstants;
import soc.game.SOCResourceSet;
import soc.game.SOCSettlement;
import soc.game.SOCTradeOffer;
import soc.message.SOCMessage;
import soc.robot.SOCRobotBrain;
import soc.robot.SOCRobotClient;
import soc.robot.SOCRobotNegotiator;
import soc.util.CappedQueue;
import soc.util.SOCRobotParameters;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Sample of a trivially simple "third-party" subclass of {@link SOCRobotBrain}
 * Instantiated by {@link Sample3PClient}.
 *<P>
 * Trivial behavioral changes from standard {@code SOCRobotBrain}:
 *<UL>
 * <LI> When sitting down, greet the game members: {@link #setOurPlayerData()}
 * <LI> Uses third-party {@link SampleDiscardStrategy}: {@link #setStrategyFields()}
 * <LI> Reject trades unless we're offered clay or sheep: {@link #considerOffer(SOCTradeOffer)}
 *</UL>
 *
 * @author Jeremy D Monin
 * @since 2.0.00
 */

 
public class Sample3PBrain extends SOCRobotBrain
{
    private Socket servercon;
    private DataInputStream serverin;
    private DataOutputStream serverout;

    /**
     * Number of declined trades in the current negotiation.
     * An example of custom state tracked by the bot during turns.
     * @since 2.5.00
     */
    protected int numDeclinedTrades = 0;
    public int NUM_LAND_HEXES = 19;

    /**
     * Standard brain constructor; for javadocs see
     * {@link SOCRobotBrain#SOCRobotBrain(SOCRobotClient, SOCRobotParameters, SOCGame, CappedQueue)}.
     */
    public Sample3PBrain(SOCRobotClient rc, SOCRobotParameters params, SOCGame ga, CappedQueue<SOCMessage> mq)
    {
        super(rc, params, ga, mq);
    }

    /**
     * After the standard actions of {@link SOCRobotBrain#setOurPlayerData()},
     * sends a "hello" chat message as a sample action using {@link SOCRobotClient#sendText(SOCGame, String)}.
     * This bot also overrides {@link #setStrategyFields()}.
     *<P>
     * If the for-bots extra game option {@link SOCGameOptionSet#K__EXT_BOT} was set at the server command line,
     * prints its value to {@link System#err}. A third-party bot might want to use that option's value
     * to configure its behavior or debug settings.
     *<P>
     *<B>I18N Note:</B> Robots don't know what languages or locales the human players can read:
     * It would be unfair for a bot to ever send text that the players must understand
     * for gameplay. So this sample bot's "hello" is not localized.
     */
    @Override
    public void setOurPlayerData()
    {
        super.setOurPlayerData();

        final String botName = client.getNickname();
        client.sendText(game, "Hello from sample bot " + botName + "!");

        final String optExtBot = game.getGameOptionStringValue(SOCGameOptionSet.K__EXT_BOT);
        if (optExtBot != null)
            System.err.println("Bot " + botName + ": __EXT_BOT is: " + optExtBot);
    }

    /**
     * Override to use our custom {@link SampleDiscardStrategy}.
     * All other strategies are standard.
     */
    @Override
    protected void setStrategyFields()
    {
        super.setStrategyFields();
        discardStrategy = new SampleDiscardStrategy(game, ourPlayerData, this, rand);
    }

    /**
     * Override to clear our custom trade-related counter.
     */
    @Override
    public void resetFieldsAndBuildingPlan()
    {
        super.resetFieldsAndBuildingPlan();
        numDeclinedTrades = 0;
    }

    /**
     * Override to clear our custom trade-related counter.
     */
    @Override
    public void resetFieldsAtEndTurn()
    {
        super.resetFieldsAtEndTurn();
        numDeclinedTrades = 0;
    }

    /**
     * Consider a trade offer; reject if we aren't offered clay or sheep
     * unless {@link #numDeclinedTrades} &gt; 2.
     *<P>
     * {@inheritDoc}
     */
    // @Override
    // protected int considerOffer(SOCTradeOffer offer)
    // {
    //     if (! offer.getTo()[getOurPlayerNumber()])
    //     {
    //         return SOCRobotNegotiator.IGNORE_OFFER;
    //     }

    //     String decision = "0";

    //     try{

    //         //Get & format my victory points
    //         String my_vp = Integer.toString(getPlayerVP(getOurPlayerNumber()));
    //         String opp_vp = Integer.toString(getPlayerVP(offer.getFrom()));

    //         //Get & format current resource set for opponent
    //         String opponent_resources = formatPlayerResources(offer.getFrom());

    //         //Get & format current resource set for our agent
    //         String my_resources = formatPlayerResources(getOurPlayerNumber());

    //         //Get & format trade contents
    //         String getData = extractOfferGetData(offer);
    //         String giveData = extractOfferGiveData(offer);
            

    //         //Pass game state info to DQN server
    //         servercon = new Socket("localhost", 2004);
    //         servercon.setSoTimeout(300000);
    //         serverin = new DataInputStream(servercon.getInputStream());
    //         serverout = new DataOutputStream(servercon.getOutputStream());
    //         String msg = "trade|" + my_vp + "|" + opp_vp + "|" + my_resources + "|" + opponent_resources + "|" + getData + "|" + giveData;
    //         serverout.writeUTF(msg);

    //         //Receive decision from DQN server
    //         decision = serverin.readLine();

    //         serverout.flush();
    //         serverout.close(); 
    //         serverin.close();   
    //         servercon.close();  
    //     }
    //      catch(Exception e){
    //         System.err.println("Whoops! Connection with server lost ... ");
    //      }

    //     if (decision.contains("0")){
    //         System.err.println(decision);
    //         System.err.println("Rejecting offer ... ");
    //        return SOCRobotNegotiator.REJECT_OFFER;
    //     }
    //     else{
    //         System.err.println(decision);
    //         System.err.println("Accepting offer ... ");
    //         return SOCRobotNegotiator.ACCEPT_OFFER;
    //     }
    // }

    protected String extractOfferGiveData(SOCTradeOffer offer){
        //Get & format which resources we'll give away
        String giveData = "";
        SOCResourceSet give = offer.getGiveSet();
        giveData += Integer.toString(give.getAmount(SOCResourceConstants.CLAY));
        giveData += ",";
        giveData += Integer.toString(give.getAmount(SOCResourceConstants.WOOD));
        giveData += ",";
        giveData += Integer.toString(give.getAmount(SOCResourceConstants.SHEEP));
        giveData += ",";
        giveData += Integer.toString(give.getAmount(SOCResourceConstants.ORE));
        giveData += ",";
        giveData += Integer.toString(give.getAmount(SOCResourceConstants.WHEAT));

        return giveData;
    }

    protected String extractOfferGetData(SOCTradeOffer offer){
        //Get & format which resources we'll get in return
        String getData = "";
        SOCResourceSet get = offer.getGetSet();
        getData += Integer.toString(get.getAmount(SOCResourceConstants.CLAY));
        getData += ",";
        getData += Integer.toString(get.getAmount(SOCResourceConstants.WOOD));
        getData += ",";
        getData += Integer.toString(get.getAmount(SOCResourceConstants.SHEEP));
        getData += ",";
        getData += Integer.toString(get.getAmount(SOCResourceConstants.ORE));
        getData += ",";
        getData += Integer.toString(get.getAmount(SOCResourceConstants.WHEAT));
        return getData;
    }

    protected int getPlayerVP(int player){
        return game.getPlayer(player).getPublicVP();
    }

    protected String formatPlayerResources(int player){
        SOCResourceSet resources = game.getPlayer(player).getResources();
        String resData = "";
        resData += Integer.toString(resources.getAmount(SOCResourceConstants.CLAY));
        resData += ",";
        resData += Integer.toString(resources.getAmount(SOCResourceConstants.WOOD));
        resData += ",";
        resData += Integer.toString(resources.getAmount(SOCResourceConstants.SHEEP));
        resData += ",";
        resData += Integer.toString(resources.getAmount(SOCResourceConstants.ORE));
        resData += ",";
        resData += Integer.toString(resources.getAmount(SOCResourceConstants.WHEAT));

        return resData;
    }

    @Override
    protected void moveRobber()
    {
        D.ebugPrintlnINFO("!!! MOVING ROBBER !!!");

        final int prevRobberHex = game.getBoard().getRobberHex();
        //    int victimNum = selectPlayerToThwart(prevRobberHex);
        //    return selectRobberHex(prevRobberHex, victimNum);

        // determine network inputs
        int inputs[] = new int[126];
        int[] hexes = game.getBoard().getLandHexCoords();
        List<Integer> hexList = Arrays.stream(hexes).boxed().collect(Collectors.toList());
        // Creates our first 19 values representing the dice values of the land hexes. 0 is the land hex where the robber is currently placed
        for (int i = 0; i < NUM_LAND_HEXES; i++){
            inputs[i] = game.getBoard().getNumberOnHexFromCoord(hexes[i]);
        }
        D.ebugPrintlnINFO("%%% landHexes = " + Arrays.toString(inputs));

        // Get settlement and city values for each tile for each player
        int[] selfPlayerValues = new int[19];
        int[] player2Values = new int[19];
        int[] player3Values = new int[19];
        int[] player4Values = new int[19];

        // Get our player number
        final int ourPlayerNumber = ourPlayerData.getPlayerNumber();

        // Access the list for all settlements
        // For each settlement, get the player number, node coordinate, and hexes that the settlement is touching
        for (SOCSettlement settlement : game.getBoard().getSettlements()){
            // get the player number of the settlement
            int playerNum = settlement.getPlayer().getPlayerNumber();
            if (playerNum < ourPlayerNumber) {
                playerNum++;
            }
            // get the hexes that the settlement is touching
            List<Integer> touchingHexes = settlement.getAdjacentHexes();
            // for each hex that the settlement is touching, increment the value in the array
            for (int i = 0; i < touchingHexes.size(); i++){
                int hex = touchingHexes.get(i);
                int hex_index = hexList.indexOf(hex);
                if (hex_index == -1){
                    continue;
                }
                if (playerNum == ourPlayerNumber){
                    selfPlayerValues[hex_index]++;
                } else if (playerNum == 1){
                    player2Values[hex_index]++;
                } else if (playerNum == 2){
                    player3Values[hex_index]++;
                } else if (playerNum == 3){
                    player4Values[hex_index]++;
                }
            }
        }

        D.ebugPrintlnINFO("%%% our settlements = " + Arrays.toString(selfPlayerValues));
        D.ebugPrintlnINFO("%%% 2 settlements = " + Arrays.toString(player2Values));
        D.ebugPrintlnINFO("%%% 3 settlements = " + Arrays.toString(player3Values));
        D.ebugPrintlnINFO("%%% 4 settlements = " + Arrays.toString(player4Values));

        // Repeat for all cities
        for (SOCCity city : game.getBoard().getCities()){
            // get the player number of the city
            int playerNum = city.getPlayer().getPlayerNumber();
            if (playerNum < ourPlayerNumber) {
                playerNum++;
            }
            // get the hexes that the city is touching
            List<Integer> touchingHexes = city.getAdjacentHexes();
            // for each hex that the city is touching, increment the value in the array
            for (int i = 0; i < touchingHexes.size(); i++){
                int hex = touchingHexes.get(i);
                int hex_index = hexList.indexOf(hex);
                if (hex_index == -1){
                    continue;
                }
                if (playerNum == ourPlayerNumber){
                    selfPlayerValues[hex_index] += 2;
                } else if (playerNum == 1){
                    player2Values[hex_index] += 2;
                } else if (playerNum == 2){
                    player3Values[hex_index] += 2;
                } else if (playerNum == 3){
                    player4Values[hex_index] += 2;
                }
            }
        }

        D.ebugPrintlnINFO("%%% our cities = " + Arrays.toString(selfPlayerValues));
        D.ebugPrintlnINFO("%%% 2 cities = " + Arrays.toString(player2Values));
        D.ebugPrintlnINFO("%%% 3 cities = " + Arrays.toString(player3Values));
        D.ebugPrintlnINFO("%%% 4 cities = " + Arrays.toString(player4Values));


        // Each player array now contains the number of total resources they collect from each hex
        // Settlement = 1 resource, City = 2 resources
        // Add these values to the inputs array
        for (int i = 0; i < NUM_LAND_HEXES; i++){
            inputs[i + 19] = selfPlayerValues[i];
            inputs[i + 38] = player2Values[i];
            inputs[i + 57] = player3Values[i];
            inputs[i + 76] = player4Values[i];
        }

        D.ebugPrintlnINFO("%%% total cities and settlements = " + Arrays.toString(inputs));

        // Add number of development cards each player has
        inputs[95] = game.getPlayer(ourPlayerNumber).getInventory().getNumUnplayed();
        for (int i = 0; i < game.maxPlayers; i++){
            if (i != ourPlayerNumber) {
                if (i > ourPlayerNumber){
                    inputs[i + 95] = game.getPlayer(i).getInventory().getNumUnplayed();
                } else {
                    // If the player number is less than our player number, we need to increment the index by 1
                    inputs[i + 96] = game.getPlayer(i).getInventory().getNumUnplayed();
                }
            }
        }

        D.ebugPrintlnINFO("%%% development cards = " + Arrays.toString(inputs));


        // Add number of resources each player has
        inputs[99] = game.getPlayer(ourPlayerNumber).getResources().getTotal();
        for (int i = 0; i < game.maxPlayers; i++){
            if (i != ourPlayerNumber) {
                if (i > ourPlayerNumber){
                    inputs[i + 99] = game.getPlayer(i).getResources().getTotal();
                } else {
                    // If the player number is less than our player number, we need to increment the index by 1
                    inputs[i + 100] = game.getPlayer(i).getResources().getTotal();
                }
            }
        }

        D.ebugPrintlnINFO("%%% resources = " + Arrays.toString(inputs));


        // Get each player's VP
        inputs[103] = game.getPlayer(ourPlayerNumber).getTotalVP();
        for (int i = 0; i < game.maxPlayers; i++){
            if (i != ourPlayerNumber) {
                if (i > ourPlayerNumber){
                    inputs[i + 103] = game.getPlayer(i).getPublicVP();
                } else {
                    // If the player number is less than our player number, we need to increment the index by 1
                    inputs[i + 104] = game.getPlayer(i).getPublicVP();
                }
            }
        }

        D.ebugPrintlnINFO("%%% vps = " + Arrays.toString(inputs));


        // final set of inputs is previous robber hex
        for (int i = 0; i < NUM_LAND_HEXES; i++){
            inputs[i + 107] = prevRobberHex == hexes[i] ? 1 : 0;
        }

        D.ebugPrintlnINFO("%%% prev robber hex = " + Arrays.toString(inputs));


        // format feature vector message
        String msg = "robber|";
        // add dice values
        for (int i = 0; i < 18; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[18] + "|";
        // add settlement values
        for (int i = 19; i < 37; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[37] + "|";
        for (int i = 38; i < 56; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[56] + "|";
        for (int i = 57; i < 75; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[75] + "|";
        for (int i = 76; i < 94; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[94] + "|";

        // add development cards
        msg += inputs[95] + "|";
        for (int i = 96; i < 98; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[98] + "|";

        // add resources
        msg += inputs[99] + "|";
        for (int i = 100; i < 102; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[102] + "|";

        // add VP
        msg += inputs[103] + "|";
        for (int i = 104; i < 106; i++){
            msg += inputs[i] + ",";
        }
        msg += inputs[106] + "|";

        // add robber hex
        for (int i = 107; i < 125; i++){
            msg += inputs[i] + ",";
        }
        msg += Integer.toString(inputs[125]);
        int bestHexIndex = 0;

        D.ebugPrintlnINFO("%%% inputs = " + msg.toString());

        try {
            D.ebugPrintlnINFO("%%% try block = " + msg.toString());
            // Pass game state info to DQN server
            servercon = new Socket("localhost", 2004);
            servercon.setSoTimeout(300000);
            serverin = new DataInputStream(servercon.getInputStream());
            serverout = new DataOutputStream(servercon.getOutputStream());
            serverout.writeUTF(msg);
            bestHexIndex = serverin.readInt();
            serverout.flush();
            serverout.close(); 
            serverin.close();   
            servercon.close(); 
        }

        catch(Exception e){
            System.err.println("Whoops! Connection with server lost ... ");
        }

        D.ebugPrintlnINFO("%%% bestHex = " + Integer.toHexString(hexes[bestHexIndex]));
        D.ebugPrintlnINFO("!!! MOVING ROBBER !!!");
        client.moveRobber(game, ourPlayerData, hexes[bestHexIndex]);
        pause(2000);
    }
}

