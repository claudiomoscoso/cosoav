package buildersoft.utils;

import java.util.ArrayList;
import java.util.List;

public class BSUtils {
	protected List<Object> array2List(Object... prms) {
		List<Object> out = new ArrayList<Object>();

		for (Object o : prms) {
			out.add(o);
		}

		return out;
	}
}
