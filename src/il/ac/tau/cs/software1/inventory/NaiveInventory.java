package il.ac.tau.cs.software1.inventory;

/*
 * A simple array-based Inventory (Question 6).
 */
public class NaiveInventory implements Inventory {

	private static final int INITIAL_CAPACITY = 100;

	Collectible[] collectibles;
	int currentCount;

	public NaiveInventory() {
		collectibles = new Collectible[INITIAL_CAPACITY];
		currentCount = 0;
	}

	@Override
	public void collect(Collectible collectible) {
		if (currentCount == collectibles.length) {
			collectibles = java.util.Arrays.copyOf(collectibles, collectibles.length * 2);
		}
		collectibles[currentCount++] = collectible;
	}

	@Override
	public float getTotalWeight() {
		float totalWeight = 0.f;
		for (int i = 0; i < currentCount; i++) {
			totalWeight += collectibles[i].getWeight();
		}
		return totalWeight;
	}

	@Override
	public int getCurrentCount() {
		return currentCount;
	}

	// Returns the array of Collectibles that is stored in the field of this class
	public Collectible[] getCollArr() {
		return collectibles;
	}
}
