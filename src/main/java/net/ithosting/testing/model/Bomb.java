package net.ithosting.testing.model;

public class Bomb {
	private String name;
	private long   timeRemaining;
	
	public Bomb(String name, long timeRemaining)
	{
	    this.name = name;
	    this.timeRemaining = timeRemaining;
	}
	public Bomb()
	{
	    this("BigBomb", 90);
	}
	public long tick()
	{
	    return timeRemaining--;
	}
	public String getName()
	{
	    return name;
	}
	@Override
	public String toString()
	{
	    String retval = "A " + name + " with " + timeRemaining + " seconds remaining";
	    return retval;
	}
}
