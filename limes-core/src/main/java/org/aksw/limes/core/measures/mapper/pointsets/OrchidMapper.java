/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.aksw.limes.core.measures.mapper.pointsets;

import org.aksw.limes.core.datastrutures.Point;
import org.aksw.limes.core.io.cache.Cache;
import org.aksw.limes.core.io.cache.Instance;
import org.aksw.limes.core.io.mapping.Mapping;
import org.aksw.limes.core.measures.mapper.Mapper;
import org.aksw.limes.core.measures.mapper.PropertyFetcher;

import java.util.*;
import java.util.regex.Pattern;

import org.aksw.limes.core.measures.measure.MeasureFactory;
import org.aksw.limes.core.measures.measure.MeasureFactory.MeasureType;
import org.aksw.limes.core.measures.measure.pointsets.IPointsetsMeasure;

/**
 *
 * @author ngonga
 */
public class OrchidMapper extends Mapper {

	IPointsetsMeasure m = null;

	/**
	 * Computes a mapping using the setMeasure distance
	 *
	 * @param source
	 *            Source cache
	 * @param target
	 *            Target cache
	 * @param sourceVar
	 *            Variable for the source dataset
	 * @param targetVar
	 *            Variable for the target dataset
	 * @param expression
	 *            Expression to process. Leads to termination if the expression
	 *            is not atomic
	 * @param threshold
	 *            Similarity threshold. Is transformed internally into a
	 *            distance threshold theta with threshold = 1/(1+theta)
	 * @return A mapping which contains uris whose polygons are such that their
	 *         distance is below the set threshold
	 */
	public Mapping getMapping(Cache source, Cache target, String sourceVar, String targetVar, String expression,
			double threshold) {
		List<String> properties = PropertyFetcher.getProperties(expression, threshold);

		// get sets of polygons from properties
		Set<Polygon> sourcePolygons = getPolygons(source, properties.get(0));
		Set<Polygon> targetPolygons = getPolygons(target, properties.get(1));
		float theta = (1 / (float) threshold) - 1;
		MeasureType type = MeasureFactory.getTypeFromExpression(expression);
		GeoHR3 orchid = new GeoHR3(theta, GeoHR3.DEFAULT_GRANULARITY, type);
		return orchid.run(sourcePolygons, targetPolygons);
	}

	/**
	 * Computes polygons out of strings in the WKT format. Currently works for
	 * LINESTRING, POINT, POLYGON
	 *
	 * @param c
	 *            Cache from which the data is to be fetched
	 * @param property
	 *            Property to use
	 * @return Set of polygons. Each polygon contains the uri to which it
	 *         matches
	 */
	public Set<Polygon> getPolygons(Cache c, String property) {
		Polygon p;
		Set<Polygon> polygons = new HashSet<Polygon>();
		for (Instance instance : c.getAllInstances()) {
			p = new Polygon(instance.getUri());
			TreeSet<String> values = instance.getProperty(property);
			if (instance.getUri().contains("dbpedia")) {
				String value = values.first();
				value = value.replace(",", "");
				values = new TreeSet<String>();
				values.add(value);
			}

			for (String v : values) {
				List<Point> points = getPoints(v);
				for (Point point : points) {
					p.add(point);
				}
			}
			polygons.add(p);
		}
		return polygons;
	}

	public static List<Point> getPoints(String rawValue) {
		if (!(rawValue.contains("(") && rawValue.contains(")"))) {
			return new ArrayList<Point>();
		}
		String s = rawValue.substring(rawValue.indexOf("(") + 1, rawValue.lastIndexOf(")"));
		s = s.replaceAll(Pattern.quote("("), "");
		s = s.replaceAll(Pattern.quote(")"), "");
		s = s.replaceAll(Pattern.quote("  "), "");
		s = s.replaceAll(Pattern.quote(" ,"), ",");
		s = s.replaceAll(Pattern.quote(", "), ",");

		String split[] = s.split(",");
		List<Point> result = new ArrayList<Point>();
		for (int i = 0; i < split.length; i++) {
			String[] coords = split[i].split(" ");
			for (int j = 0; j < coords.length; j = j + 2) {
				List<Double> coordinates = new ArrayList<Double>();
				try {
					coordinates.add(Double.parseDouble(coords[j].replaceAll(" ", "")));
					coordinates.add(Double.parseDouble(coords[j + 1].replaceAll(" ", "")));
					Point p = new Point("", coordinates);
					result.add(p);
				} catch (Exception e) {
					System.err.println(e);
				}
			}
		}
		return result;
	}

	/**
	 * Computes a polygon out of a WKT string
	 *
	 * @param rawValue
	 *            An WKT string
	 * @return A polygon
	 */
	public static Polygon getPolygon(String rawValue) {
		Polygon p = new Polygon("");
		List<Point> points = getPoints(rawValue);
		for (Point point : points) {
			p.add(point);
		}
		return p;
	}

	public String getName() {
		return "Orchid";
	}

	public double getRuntimeApproximation(int sourceSize, int targetSize, double threshold, Language language) {
		if (language.equals(Language.DE)) {
			// error = 667.22
			return 16.27 + 5.1 * sourceSize + 4.9 * targetSize - 23.44 * threshold;
		} else {
			// error = 5.45
			return 22 + 0.005 * (sourceSize + targetSize) - 56.4 * threshold;
		}
	}

	public double getMappingSizeApproximation(int sourceSize, int targetSize, double threshold, Language language) {
		if (language.equals(Language.DE)) {
			// error = 667.22
			return 2333 + 0.14 * sourceSize + 0.14 * targetSize - 3905 * threshold;
		} else {
			// error = 5.45
			return 0.006 * (sourceSize + targetSize) - 134.2 * threshold;
		}
	}


}