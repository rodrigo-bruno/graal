import java.util.*;

public class App {

	String s1;
	String s2;
	Map<Integer, String> stringMap;

	public App(String s1, String s2) {
		this.s1 = s1;
		this.s2 = s2;
		this.stringMap = new HashMap<>();
	}

	private void buildMap() {
	    for (int i = 0; i < 256; i++) {
	        stringMap.put(i, String.valueOf(i));
        }
    }

	@Override
	public String toString() {
		return s1 + " " + s2;
	}

	public static void main(String[] args) throws Exception {
		String s1 = "Hellóã";
		String s2 = "World";
		App app = new App(s1, s1);
		app.buildMap();
		while(true) {
			System.out.println(app);
			System.out.println(app.stringMap.size());
			Thread.sleep(2000);
		}
	}
}

