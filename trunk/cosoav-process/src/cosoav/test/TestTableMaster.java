package cosoav.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import buildersoft.utils.BSDataUtils;
import cosoav.beans.TimeConfig;

public class TestTableMaster extends BSDataUtils {
	TimeConfig tc = null;

	@Before
	public void setUp() throws Exception {
		if (this.tc == null) {
			this.tc = new TimeConfig();
			this.tc.setConnection(this.getConnection());
		}
	}

	@After
	public void tearDown() throws Exception {
		this.tc = null;
	}

	@Test
	public void testInsert() {
		try {
			this.tc.setTypePlain("DEL");
			this.tc.setMins(3);

			this.tc.insert();
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(true);
	}

	@Test
	public void testUpdate() {
		try {
			this.tc.setId(55);
			this.tc.setTypePlain("DEZ");
			this.tc.setMins(4);

			tc.update();
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(true);
	}

	public void testSave() {
		try {
			this.tc.setId(999);
			this.tc.setTypePlain("DELME");
			this.tc.setMins(2);

			this.tc.save();
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

		assertTrue(true);
	}

	@Test
	public void testDelete() {
		try {

			tc.setId(7);

			tc.delete();
			assertTrue(tc.getTypePlain() == null && tc.getId() == null);
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}

	@Test
	public void testSearch() {
		try {
			this.tc.setId(1);

			Boolean found = this.tc.search();

			assertTrue(found);
		} catch (SecurityException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchFieldException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (InvocationTargetException e) {
			e.printStackTrace();
			fail(e.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			fail(e.getMessage());
		}

	}
}
