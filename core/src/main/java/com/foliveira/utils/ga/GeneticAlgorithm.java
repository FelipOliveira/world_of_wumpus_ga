package com.foliveira.utils.ga;

import com.foliveira.WumpusGameScreen;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class GeneticAlgorithm {
    final int POPULATION_SIZE = 20;
    final int MAX_GENERATIONS = 30;
    final double MUTATION_RATE = 0.05;
    final int NUM_OF_TESTS = 10;
    final int OPTIMAL_GENERATION = 5;
    final int ELITISM_COUNT = 2;
    ArrayList<GAgent> population = new ArrayList<>();
    ArrayList<GAgent> nextGeneration = new ArrayList<>();
    ArrayList<Integer> topFitnessEachGeneration = new ArrayList<>();
    private final Random random = new Random();
    WumpusGameScreen gameScreen;
    int optimalGenerationCounter;
    int thisTopFit;
    int previousTopFit;
    final ArrayList<Integer> results = new ArrayList<>();

    public void initializePopulation(WumpusGameScreen gameScreen){
        this.gameScreen = gameScreen;
        optimalGenerationCounter = 0;
        thisTopFit = -10000;
        previousTopFit = -10000;
        for (int i = 0; i< POPULATION_SIZE; i++) {
            GAgent agent = new GAgent(gameScreen);
            population.add(agent);
        }
    }

    public void run(){
        System.out.println("running ga... ");
        for (int k=0;k<NUM_OF_TESTS;k++) {
            initializePopulation(gameScreen);
            for (int i=0; i < MAX_GENERATIONS; i++) {
                System.out.println("\nTEST=#" + (k+1)+ "=GENERATION=" + i + "/" + MAX_GENERATIONS);
                nextGeneration.clear();
                evaluatePopulation();
                getBestAgents(population);
                crossover(nextGeneration);
                mutate(nextGeneration);
                updatePopulation();
                thisTopFit = getBestAgent();
                System.out.println(thisTopFit);
            }
            if (thisTopFit == previousTopFit) {
                optimalGenerationCounter++;
                if (optimalGenerationCounter > OPTIMAL_GENERATION) {
                    System.out.println("optimal value reached!");
                    break;
                }
            } else {
                optimalGenerationCounter = 0;

            }
            previousTopFit = thisTopFit;
        }
        bestFitAvg();
        System.out.println("ending ga... ");
    }

    void evaluatePopulation() {
        for (int i=0; i<POPULATION_SIZE; i++){
             System.out.print("Agent " + i + " ");
             population.get(i).calculateFitness(gameScreen.currentGameState);
        }
    }

    public void getBestAgents(ArrayList<GAgent> agents) {
        nextGeneration.clear();
        nextGeneration.addAll(agents.stream()
            // 1. Ordena a lista em ordem decrescente de fitness
            .sorted(Comparator.comparingInt(GAgent::getFitness).reversed())
            // 2. Limita o stream a metade da população atual
            .limit((int) POPULATION_SIZE/2)
            // 3. Coleta os elementos resultantes em uma nova lista
            .collect(Collectors.toList()));

    }

    private void crossover(ArrayList<GAgent> population){
         //nextGeneration = getBestAgents(population);
         int size = nextGeneration.size();

         for (int i=0;i<size;i++) {
             GAgent parent1 = nextGeneration.get(random.nextInt(size));
             GAgent parent2 = nextGeneration.get(random.nextInt(size));

             // Define o ponto de corte
             int chromosomeLength = parent1.rules.size();
             int crossoverPoint = chromosomeLength/2;

             // Pega as "cabeças" dos cromossomos dos pais
             List<Rule> parent1Head = parent1.rules.subList(0, crossoverPoint);
             List<Rule> parent2Head = parent2.rules.subList(0, crossoverPoint);

             // Pega as "caudas" dos cromossomos dos pais
             List<Rule> parent1Tail = parent1.rules.subList(crossoverPoint, chromosomeLength);
             List<Rule> parent2Tail = parent2.rules.subList(crossoverPoint, chromosomeLength);

             // Cria os cromossomos dos filhos combinando cabeça e cauda
             ArrayList<Rule> child1Chromosome = new ArrayList<>(parent1Head);
             child1Chromosome.addAll(parent2Tail); // Cabeça do Pai 1 + Cauda do Pai 2

             ArrayList<Rule> child2Chromosome = new ArrayList<>(parent2Head);
             child2Chromosome.addAll(parent1Tail); // Cabeça do Pai 2 + Cauda do Pai 1

             // Adiciona os novos filhos à nova geração
             nextGeneration.add(new GAgent(gameScreen, child1Chromosome));
             if (nextGeneration.size() < population.size()) { // Garante que não exceda o tamanho da população
                 nextGeneration.add(new GAgent(gameScreen, child2Chromosome));
             }
         }
    }

    void mutate(ArrayList<GAgent> population) {
        for (GAgent agent : population) {
            if (random.nextDouble() < MUTATION_RATE) {
                System.out.println("Mutation!!!");
                int ruleIndex = random.nextInt(agent.rules.size());
                Action newAction = Action.getRandomAction();
                Rule newRule = new Rule(ruleIndex, newAction);
                agent.rules.set(ruleIndex, newRule);
            }
        }
    }

    void updatePopulation() {
        topFitnessEachGeneration.add(getBestAgent());
        population.clear();
        population.addAll(nextGeneration);
    }

    int getBestAgent(){
        ArrayList<GAgent> list = (ArrayList<GAgent>) population.stream()
             .sorted(Comparator.comparingInt(GAgent::getFitness).reversed())
             .limit(5).collect(Collectors.toList());

        System.out.println("\nBest fitness: ");
        for (GAgent agent : list) {
            System.out.println((list.indexOf(agent) + 1) + ": " + agent.fitness);
        }
        return list.get(0).getFitness();
    }

    private void bestFitAvg(){
        double avg = topFitnessEachGeneration.stream()
            .mapToDouble(Integer::doubleValue)
            .average()
            .orElse(0.0);
        System.out.print("\n=============\n");
        System.out.print("=average: " + avg + "=");
        System.out.print("\n=============\n");
    }
}
