package de.protokollmensch.eztutorial.models;

public class TutorialTask {
    private final String id;
    private final TaskType type;
    private final String description;
    private final String world;
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private final String command;

    public TutorialTask(
        String id,
        TaskType type,
        String description,
        String world,
        double x,
        double y,
        double z,
        double radius,
        String command
    ) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.command = command;
    }

    public String getId() {
        return id;
    }

    public TaskType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public String getWorld() {
        return world;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getRadius() {
        return radius;
    }

    public String getCommand() {
        return command;
    }
}

