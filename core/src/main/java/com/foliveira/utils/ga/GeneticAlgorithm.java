package com.foliveira.utils.ga;

import com.foliveira.WumpusGameScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;
import java.util.stream.Collectors;

public class GeneticAlgorithm {
    final int POPULATION_SIZE = 20;
    final int MAX_GENERATIONS = 100;
    final float MUTATION_RATE = 0.03f;
    ArrayList<GAgent> population = new ArrayList<>();
    ArrayList<GAgent> nextGeneration = new ArrayList<>();
    private final Random random = new Random();
    WumpusGameScreen gameScreen;

     public void initializePopulation(WumpusGameScreen gameScreen){
        this.gameScreen = gameScreen;
        for (int i = 0; i< POPULATION_SIZE; i++) {
            GAgent agent = new GAgent(gameScreen);
            population.add(agent);
        }
    }

    public void run(){
        System.out.println("running ga... ");
        for (int i = 0; i< MAX_GENERATIONS; i++) {
            System.out.println("\n===================GENERATION=" + i + "===================");
            nextGeneration.clear();
            evaluatePopulation();
            getBestAgents(population);
            crossover(nextGeneration);
            mutate(nextGeneration);
            updatePopulation();

        }
    }

    void evaluatePopulation() {
         for (int i=0; i<POPULATION_SIZE; i++){
             System.out.println("Agent " + i + "==================\n");
             population.get(i).calculateFitness(gameScreen.currentGameState);
         }
    }

    public void getBestAgents(ArrayList<GAgent> agents) {
        nextGeneration.clear();
        nextGeneration = (ArrayList<GAgent>) agents.stream()
            // 1. Ordena a lista em ordem decrescente de fitness
            .sorted(Comparator.comparingInt(GAgent::getFitness).reversed())
            // 2. Limita o stream a metade da população atual
            .limit((int) POPULATION_SIZE/2)
            // 3. Coleta os elementos resultantes em uma nova lista
            .collect(Collectors.toList());
    }

    private void crossover(ArrayList<GAgent> population){
         int size = population.size();
         for (int k=0;k<size;k++) {
         GAgent parent1 = population.get(random.nextInt(size));
         GAgent parent2 = population.get(random.nextInt(size));

         ArrayList<Rule> rules1 = new ArrayList<>();
         ArrayList<Rule> rules2 = new ArrayList<>();
         for (int i=0; i<16; i++){
             rules1.add(parent1.rules.get(i));
             rules2.add(parent2.rules.get(i));
         }
         for (int i=16; i<32; i++){
             rules1.add(parent2.rules.get(i));
             rules2.add(parent1.rules.get(i));
         }
         GAgent child1 = new GAgent(gameScreen, rules1);
         GAgent child2 = new GAgent(gameScreen, rules2);

         nextGeneration.add(child1);
         nextGeneration.add(child2);
         }
    }

    void mutate(ArrayList<GAgent> population) {
        for (GAgent agent : population) {
            if (random.nextFloat() <= MUTATION_RATE) {
                int ruleIndex = random.nextInt(agent.rules.size());
                Action newAction = Action.getRandomAction();
                Rule newRule = new Rule(ruleIndex, newAction);
                for (Rule rule : agent.rules) {
                    if (newRule.perception == rule.perception) rule = newRule;
                }
            }
        }
    }

    void updatePopulation() {
        getBestAgent();
        population.clear();
        population.addAll(nextGeneration);
    }

    void getBestAgent(){
        ArrayList<GAgent> list = (ArrayList<GAgent>) population.stream()
             .sorted(Comparator.comparingInt(GAgent::getFitness).reversed())
             .limit(5).collect(Collectors.toList());

        System.out.println("\nBest fitness: ");
        for (GAgent agent : list) {
            System.out.println((list.indexOf(agent) + 1) + ": " + agent.fitness);
        }
    }
}
