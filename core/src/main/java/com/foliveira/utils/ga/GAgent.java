package com.foliveira.utils.ga;

import com.foliveira.WumpusGameScreen;
import com.foliveira.config.GameConfig;

import java.util.ArrayList;

public class GAgent {
    public ArrayList<Rule> rules = new ArrayList<>();
    public int fitness;
    WumpusGameScreen gameScreen;

    public GAgent(WumpusGameScreen gameScreen){
        this.gameScreen = gameScreen;
        makeRandomRuleSet();
    }

    public GAgent(WumpusGameScreen gameScreen, ArrayList<Rule> rules){
        this.gameScreen = gameScreen;
        this.rules = rules;
    }

    public void makeRandomRuleSet(){
        /*for (Perception p : Perception.values()) {
            Action a = Action.getRandomAction();
            Rule rule = new Rule(p, a);
            rules.add(rule);
        }*/
        for (int i=0; i<32; i++) {
            Action a = Action.getRandomAction();
            Rule rule = new Rule(i, a);
            rules.add(rule);
        }
        this.fitness = 0;
    }
    public Action getAction(int perception){
        for (Rule rule : rules) {
            if (rule.perception == perception) {
                return rule.action;
            }
        } return null;
    }

    private int updatePerception(WumpusGameScreen.WumpusWorldState simState) {
        int playerX = simState.playerX;
        int playerY = simState.playerY;

        int percepts = 0;
        // check pit's breeze (adjacent)
        if (gameScreen.isBreeze(playerX, playerY, simState)) percepts |= GameConfig.BREEZE;

        // check wumpus's stench (adjacent)
        if (gameScreen.isStench(playerX, playerY, simState)) percepts |= GameConfig.STENCH;

        // check gold's glitter
        if (gameScreen.isGlitter(simState)) percepts |= GameConfig.GLITTER;

        return percepts;
    }

    public void calculateFitness(WumpusGameScreen.WumpusWorldState initialState) {
        WumpusGameScreen.WumpusWorldState simState = initialState.copy(); // Trabalha em uma cópia para não alterar o jogo real
        int fitness = 0;
        boolean goldReached = false;
        boolean returnedToStartWithGold = false;
        int initialArrows = simState.arrowsLeft;
        int movesTaken = 0;

        for (Rule rule: rules) {
            switch (rule.action) {
                case MOVE_FORWARD:
                    gameScreen.moveForward(simState);
                    fitness -= 10;
                    break;
                case TURN_LEFT:
                    gameScreen.turnLeft(simState);
                    fitness -= 10;
                    break;
                case TURN_RIGHT:
                    gameScreen.turnRight(simState);
                    fitness -= 10;
                    break;
                case GRAB:
                    boolean hadGoldBeforeGrab = simState.hasGold;
                    gameScreen.searchForGold(simState);
                    if (simState.hasGold && !hadGoldBeforeGrab) {
                        fitness += 1000; // Grande recompensa por pegar o ouro
                    } else {
                        fitness -= 50; // Penalidade por tentar pegar ouro onde não há
                    }
                    break;
                case SHOOT:
                    gameScreen.shootArrow(simState);
                    if (!simState.wumpusAlive) fitness += 1000;
                    fitness -= 50;
                    break;
            }

            movesTaken++;

            System.out.print("action: " + rule.action + " ");

            // Penalidades por estado de GAME_OVER
            if (simState.gameState == WumpusGameScreen.GameState.GAME_OVER) {
                fitness -= 1000; // Grande penalidade por morrer
                break; // Termina a simulação deste cromossomo
            }

            // Recompensa por vitória
            if (simState.gameState == WumpusGameScreen.GameState.GAME_WON) {
                fitness += 1000; // Recompensa massiva por vencer o jogo
                returnedToStartWithGold = true;
                break; // Termina a simulação deste cromossomo
            }
        }

        /*while (simState.gameState == WumpusGameScreen.GameState.PLAYING && movesTaken <= 20) {
            // checar a perception da sala e aplicar a action correspondente
            Action action = getAction(updatePerception(simState));
            switch (action) {
                case MOVE_FORWARD:
                    gameScreen.moveForward(simState);
                    fitness -= 10;
                    break;
                case TURN_LEFT:
                    gameScreen.turnLeft(simState);
                    fitness -= 10;
                    break;
                case TURN_RIGHT:
                    gameScreen.turnRight(simState);
                    fitness -= 10;
                    break;
                case GRAB:
                    boolean hadGoldBeforeGrab = simState.hasGold;
                    gameScreen.searchForGold(simState);
                    if (simState.hasGold && !hadGoldBeforeGrab) {
                        fitness += 1000; // Grande recompensa por pegar o ouro
                    } else {
                        fitness -= 50; // Penalidade por tentar pegar ouro onde não há
                    }
                    break;
                case SHOOT:
                    gameScreen.shootArrow(simState);
                    if (!simState.wumpusAlive) fitness += 1000;
                    fitness -= 50;
                    break;
            }

            movesTaken++;

            System.out.print("action: " + action + " ");

            // Penalidades por estado de GAME_OVER
            if (simState.gameState == WumpusGameScreen.GameState.GAME_OVER) {
                fitness -= 1000; // Grande penalidade por morrer
                break; // Termina a simulação deste cromossomo
            }

            // Recompensa por vitória
            if (simState.gameState == WumpusGameScreen.GameState.GAME_WON) {
                fitness += 1000; // Recompensa massiva por vencer o jogo
                returnedToStartWithGold = true;
                break; // Termina a simulação deste cromossomo
            }
        }*/

        if (simState.hasGold && !returnedToStartWithGold) {
            // Recompensa adicional se pegou o ouro mas não voltou à entrada
            fitness += 100;
        }

        if (simState.wumpusAlive && simState.gameState != WumpusGameScreen.GameState.GAME_WON) {
            // Se o Wumpus ainda estiver vivo, penalizar.
            fitness -= 50;
        }

        // Se não pegou o ouro e não venceu
        if (!simState.hasGold && simState.gameState != WumpusGameScreen.GameState.GAME_WON) {
            fitness -= 200; // Grande penalidade por não cumprir o objetivo principal
        }

        this.fitness = fitness;

        System.out.print("\nfitness: " + fitness + "\n");
    }

    public int getFitness() {
        return fitness;
    }

}
