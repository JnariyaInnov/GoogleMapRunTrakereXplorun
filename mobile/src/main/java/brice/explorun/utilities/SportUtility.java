package brice.explorun.utilities;

public class SportUtility
{
	public static final int WALKING = 0;
	public static final int RUNNING = 1;
	public static final int TRAIL = 2;

	// Average speeds for each sport (in km/h)
	private static final int WALKING_SPEED = 3;
	private static final int RUNNING_SPEED = 9;
	private static final int TRAIL_SPEED = 11;

	/**
	 * Function which returns the average speed of a sport
	 * @param sport Sport of the user
	 * @return The average speed of the sport selected by the user
	 */
	public static float getAverageSpeedFromSport(int sport)
	{
		float res;
		switch (sport)
		{
			case SportUtility.TRAIL:
				res = SportUtility.TRAIL_SPEED;
				break;

			case SportUtility.RUNNING:
				res = SportUtility.RUNNING_SPEED;
				break;

			default:
				res = SportUtility.WALKING_SPEED;
				break;
		}

		return res;
	}
}
