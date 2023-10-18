package pt.ulisboa.tecnico.jol.maps;

import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.graalvm.collections.EconomicMap;
import org.graalvm.collections.Equivalence;
import org.openjdk.jol.info.ClassLayout;
import org.openjdk.jol.info.GraphLayout;
import org.openjdk.jol.vm.VM;

public class JOL_Maps {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static Map populateMap(Map map, int elems) {
		for (int i = 0; i < elems; i++) {
			map.put(i, -i);
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static EconomicMap populateMap(EconomicMap map, int elems) {
		for (int i = 0; i < elems; i++) {
			map.put(i, -i);
		}
		return map;
	}

	@SuppressWarnings({ "rawtypes" })
	public static void main(String[] args) throws Exception {
		out.println(VM.current().details());
		out.println(ClassLayout.parseClass(HashMap.class).toPrintable());
		out.println(ClassLayout.parseClass(ConcurrentHashMap.class).toPrintable());
		out.println(ClassLayout.parseClass(Class.forName("org.graalvm.collections.EconomicMapImpl")).toPrintable());

		out.println(String.format("HashMap with %s elems occupies %s bytes.", 0,
				GraphLayout.parseInstance(populateMap(new HashMap(), 0)).toFootprint()));
		out.println(String.format("ConcurrentHashMap with %s elems occupies %s bytes.", 0,
				GraphLayout.parseInstance(populateMap(new ConcurrentHashMap(), 0)).toFootprint()));
		out.println(String.format("EconomicMap with %s elems occupies %s bytes.", 0,
				GraphLayout.parseInstance(populateMap(EconomicMap.create(Equivalence.IDENTITY), 0)).toFootprint()));

		for (int elems : new int[]{1, 2, 4, 8, 16, 32, 64, 128, 512, 1024}) {
			out.println(String.format("HashMap with %s elems occupies %s bytes.",
					elems, GraphLayout.parseInstance(populateMap(new HashMap(), elems)).totalSize()));
			out.println(String.format("ConcurrentHashMap with %s elems occupies %s bytes.",
					elems, GraphLayout.parseInstance(populateMap(new ConcurrentHashMap(), elems)).totalSize()));
			out.println(String.format("EconomicMap with %s elems occupies %s bytes.",
					elems, GraphLayout.parseInstance(populateMap(EconomicMap.create(Equivalence.IDENTITY), elems)).totalSize()));
		}



	}

}
