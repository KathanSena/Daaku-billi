package utilities;
public class constants {


public static class playerConstants {
	public static final int IDLE = 0;
	public static final int RUNNING = 1;
	public static final int JUMPING = 2;
	public static final int FALLING = 3;
	public static final int GROUND = 4;
	public static final int HIT = 5;
	public static final int ATTACKING_1 = 6;
	public static final int ATTACKING_JUMP = 7;
	public static final int SWIMMING = 8;

	public static int GetCatAmount(int playerAction) {
		switch (playerAction) {
			case RUNNING:
				return 6;
			case IDLE:
				return 5;
			case HIT:
				return 4;
			case JUMPING:
			case ATTACKING_1:
			case ATTACKING_JUMP:
				return 3;
			case GROUND:
				return 2;
			case FALLING:
			default:
				return 1;
		}
	}
}
public static class Directions {
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
public static final int UP = 2;
    public static final int DOWN = 3;




}}
