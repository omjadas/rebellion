import java.util.Collection;
import java.util.Random;

public class Agent extends Person {
    private final double deathChance;
    private final double governmentLegitimacy;
    private final double k;
    private final double perceivedHardshipIncreaseRate;
    private final double riskAversion;
    private final double threshold;

    /**
     * Whether the agent is active
     */
    private boolean active = false;

    /**
     * Whether the agent is dead
     */
    private boolean dead = false;

    /**
     * The agent's current jail term, 0 if not in jail
     */
    private int jailTerm = 0;

    /**
     * The agent's perceived hardship
     */
    private double perceivedHardship;

    /**
     * Initialise an Agent.
     *
     * @param governmentLegitimacy          legitimacy of the government 0-1
     * @param k                             constant value
     * @param threshold                     threshold to determine whether agent
     *                                      should rebel
     * @param deathChance                   chance of dying for each unit time
     *                                      spent in jail
     * @param perceivedHardshipIncreaseRate the rate at which the agent's
     *                                      perceived hardship will increase
     *                                      towards 1.0 for each agent they see
     *                                      die
     * @param move                          whether the agent should move
     */
    public Agent(
            double governmentLegitimacy,
            double k,
            double threshold,
            double deathChance,
            double perceivedHardshipIncreaseRate,
            boolean move) {
        super(move);
        Random rand = new Random();

        this.deathChance = deathChance;
        this.governmentLegitimacy = governmentLegitimacy;
        this.k = k;
        this.perceivedHardship = rand.nextDouble();
        this.perceivedHardshipIncreaseRate = perceivedHardshipIncreaseRate;
        this.riskAversion = rand.nextDouble();
        this.threshold = threshold;
    }

    /**
     * Determine whether the agent is active.
     *
     * @return a boolean indicating whether the agent is active or not
     */
    public boolean getActive() {
        return active;
    }

    /**
     * Set whether the agent is active.
     *
     * @param active whether the agent is active.
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    private double getGrievance() {
        return perceivedHardship * (1 - governmentLegitimacy);
    }

    /**
     * Retrieve the agents jail term.
     *
     * @return remaining jail term for the agent, 0 if not in jail
     */
    public int getJailTerm() {
        return jailTerm;
    }

    /**
     * Set the jail term for the agent.
     *
     * @param jailTerm the jail term to set for the agent
     */
    public void setJailTerm(int jailTerm) {
        this.jailTerm = jailTerm;
    }

    /**
     * Retrieve dead.
     *
     * @return true if the agent is dead, false otherwise
     */
    public boolean getDead() {
        return dead;
    }

    /**
     * Notify the agent of a nearby death.
     */
    private void notifyDeath() {
        // Perceived hardship will tend towards 1.0
        perceivedHardship += (1 - perceivedHardship) *
            perceivedHardshipIncreaseRate;
    }

    @Override
    public void takeTurn(Collection<Tile> visibleTiles) {
        if (!dead) {
            if (jailTerm == 0) {
                super.takeTurn(visibleTiles);

                int cops = 0;
                int activeAgents = 1;

                for (Tile tile : visibleTiles) {
                    for (Person person : tile.getPeople()) {
                        if (person instanceof Cop) {
                            cops++;
                        } else if (person instanceof Agent &&
                            ((Agent) person).getActive()) {
                            activeAgents++;
                        }
                    }
                }

                double estimatedArrestProbability = 1 -
                    Math.exp(-k * Math.floor(cops / activeAgents));

                double netRisk = estimatedArrestProbability * riskAversion;

                active = getGrievance() - netRisk > threshold;
            } else {
                Random rand = new Random();
                if (rand.nextDouble() <= deathChance) {
                    dead = true;

                    // Notify visible agents of the death
                    for (Tile tile : visibleTiles) {
                        for (Person person : tile.getPeople()) {
                            if (person instanceof Agent) {
                                ((Agent) person).notifyDeath();
                            }
                        }
                    }
                } else {
                    jailTerm--;
                }
            }
        }
    }
}
