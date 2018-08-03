package baritone.bot.pathing.calc;

import baritone.bot.pathing.goals.Goal;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;

public abstract class AbstractNodeCostSearch implements IPathFinder {

    //TODO this shouldn't be necessary!!
    public static AbstractNodeCostSearch currentlyRunning = null;


    protected final BlockPos start;
    protected final Goal goal;
    protected final HashMap<BlockPos, PathNode> map;
    protected PathNode startNode;
    protected PathNode mostRecentConsidered;
    protected PathNode[] bestSoFar;
    private volatile boolean isFinished;

    /**
     * This is really complicated and hard to explain. I wrote a comment in the old version of MineBot but it was so
     * long it was easier as a Google Doc (because I could insert charts).
     *
     * @see <a href="https://docs.google.com/document/d/1WVHHXKXFdCR1Oz__KtK8sFqyvSwJN_H4lftkHFgmzlc/edit"></a>
     */
    protected static final double[] COEFFICIENTS = {1.5, 2, 2.5, 3, 4, 5, 10};
    /**
     * If a path goes less than 5 blocks and doesn't make it to its goal, it's not worth considering.
     */
    protected final static double MIN_DIST_PATH = 5;

    AbstractNodeCostSearch(BlockPos start, Goal goal) {
        this.start = start;
        this.goal = goal;
        this.map = new HashMap<>();
    }

    public synchronized IPath calculatePath() {
        if (isFinished) {
            throw new IllegalStateException("Unable to re-use path finder");
        }
        IPath path = calculate();
        isFinished = true;
        return path;
    }

    protected abstract IPath calculate();

    protected double distFromStart(PathNode n) {
        int xDiff = n.pos.getX() - start.getX();
        int yDiff = n.pos.getY() - start.getY();
        int zDiff = n.pos.getZ() - start.getZ();
        return Math.sqrt(xDiff * xDiff + yDiff * yDiff + zDiff * zDiff);
    }

    protected PathNode getNodeAtPosition(BlockPos pos) {
        //technically I think this could be map.computeIfAbsent(pos, pos -> new PathNode(pos, goal))
        //but this is so core to the pathfinder that I'm wary of the lambda performance, hmmm
        PathNode alr = map.get(pos);
        if (alr == null) {
            PathNode node = new PathNode(pos, goal);
            map.put(pos, node);
            return node;
        }
        return alr;
    }

    @Override
    public Path bestPathSoFar() {
        if (startNode == null || bestSoFar[0] == null) {
            return null;
        }
        return new Path(startNode, bestSoFar[0], goal);
    }

    @Override
    public Path pathToMostRecentNodeConsidered() {
        return mostRecentConsidered == null ? null : new Path(startNode, mostRecentConsidered, goal);
    }

    @Override
    public final boolean isFinished() {
        return isFinished;
    }

    @Override
    public final Goal getGoal() {
        return goal;
    }

    @Override
    public final BlockPos getStart() {
        return start;
    }
}
